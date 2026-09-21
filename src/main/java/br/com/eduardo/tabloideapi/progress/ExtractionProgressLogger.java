package br.com.eduardo.tabloideapi.progress;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@Component
public class ExtractionProgressLogger implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractionProgressLogger.class);
    private static final long HEARTBEAT_SECONDS = 15;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "tabloide-progress");
        thread.setDaemon(true);
        return thread;
    });

    public <T> T monitor(
            String extractionId,
            int initialPercent,
            int completedPercent,
            String stage,
            Supplier<T> operation
    ) {
        long startedAt = System.nanoTime();
        LOGGER.info("[{}] [{}%] {}", extractionId, initialPercent, stage);
        AtomicBoolean running = new AtomicBoolean(true);

        ScheduledFuture<?> heartbeat = scheduler.scheduleAtFixedRate(
                () -> {
                    if (running.get()) {
                        LOGGER.info(
                                "[{}] [{}%] {} — {} decorridos",
                                extractionId,
                                initialPercent,
                                stage,
                                formatElapsed(System.nanoTime() - startedAt)
                        );
                    }
                },
                HEARTBEAT_SECONDS,
                HEARTBEAT_SECONDS,
                TimeUnit.SECONDS
        );

        try {
            T result = operation.get();
            running.set(false);
            heartbeat.cancel(false);
            LOGGER.info(
                    "[{}] [{}%] {} concluído em {}",
                    extractionId,
                    completedPercent,
                    stage,
                    formatElapsed(System.nanoTime() - startedAt)
            );
            return result;
        } catch (RuntimeException exception) {
            running.set(false);
            heartbeat.cancel(false);
            LOGGER.warn(
                    "[{}] [{}%] {} interrompido após {}: {}",
                    extractionId,
                    initialPercent,
                    stage,
                    formatElapsed(System.nanoTime() - startedAt),
                    exception.getMessage()
            );
            throw exception;
        } finally {
            running.set(false);
            heartbeat.cancel(false);
        }
    }

    @PreDestroy
    @Override
    public void close() {
        scheduler.shutdownNow();
    }

    public static String formatElapsed(long nanoseconds) {
        long totalSeconds = Math.max(0, Duration.ofNanos(nanoseconds).toSeconds());
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        if (minutes == 0) {
            return "%ds".formatted(seconds);
        }
        return "%dm%02ds".formatted(minutes, seconds);
    }
}
