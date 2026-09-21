package br.com.eduardo.tabloideapi.service;

import br.com.eduardo.tabloideapi.config.TabloideProperties;
import br.com.eduardo.tabloideapi.dto.TabloideResponse;
import br.com.eduardo.tabloideapi.exception.ProcessingBusyException;
import br.com.eduardo.tabloideapi.extractor.TabloideExtractor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TabloideService {

    private final Map<String, TabloideExtractor> extractors;
    private final String selectedExtractor;
    private final Semaphore processingPermit = new Semaphore(1, true);

    public TabloideService(
            List<TabloideExtractor> extractors,
            TabloideProperties properties
    ) {
        this.extractors = extractors.stream()
                .collect(Collectors.toUnmodifiableMap(
                        extractor -> extractor.type().toLowerCase(Locale.ROOT),
                        Function.identity()
                ));
        this.selectedExtractor = properties.extractor().toLowerCase(Locale.ROOT);
    }

    public TabloideResponse processar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Envie uma imagem não vazia no campo 'file'");
        }

        if (!processingPermit.tryAcquire()) {
            throw new ProcessingBusyException();
        }

        try {
            BufferedImage image = readImage(file);
            TabloideExtractor extractor = extractors.get(selectedExtractor);

            if (extractor == null) {
                throw new IllegalStateException(
                        "Extrator '%s' não configurado. Disponíveis: %s"
                                .formatted(selectedExtractor, extractors.keySet())
                );
            }

            try {
                return extractor.extract(image);
            } finally {
                image.flush();
            }
        } finally {
            processingPermit.release();
        }
    }

    private BufferedImage readImage(MultipartFile file) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new IllegalArgumentException("O arquivo enviado não é uma imagem válida");
            }
            return image;
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível ler a imagem enviada", e);
        }
    }
}
