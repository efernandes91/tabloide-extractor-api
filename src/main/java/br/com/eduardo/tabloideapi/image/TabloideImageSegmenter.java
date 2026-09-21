package br.com.eduardo.tabloideapi.image;

import br.com.eduardo.tabloideapi.config.TabloideProperties;
import org.springframework.stereotype.Component;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@Component
public class TabloideImageSegmenter {

    private static final double RETRY_OVERLAP = 0.08;

    private final TabloideProperties.Image properties;

    public TabloideImageSegmenter(TabloideProperties properties) {
        this.properties = properties.image();
    }

    public BufferedImage header(BufferedImage image) {
        int headerHeight = headerHeight(image);
        BufferedImage header = copyRegion(image, 0, 0, image.getWidth(), headerHeight);
        return scale(header, properties.headerScale());
    }

    public List<BufferedImage> bodyTiles(BufferedImage image) {
        int bodyStart = headerHeight(image);
        int bodyHeight = image.getHeight() - bodyStart;
        int tileCount = Math.max(1, properties.bodyTiles());
        int coreHeight = (int) Math.ceil((double) bodyHeight / tileCount);
        int overlap = (int) Math.round(coreHeight * clamp(properties.tileOverlap(), 0.0, 0.45));

        List<BufferedImage> tiles = new ArrayList<>();

        for (int index = 0; index < tileCount; index++) {
            int coreStart = index * coreHeight;
            int start = Math.max(0, coreStart - overlap);
            int end = Math.min(bodyHeight, coreStart + coreHeight + overlap);

            if (end <= start) {
                continue;
            }

            BufferedImage tile = copyRegion(
                    image,
                    0,
                    bodyStart + start,
                    image.getWidth(),
                    end - start
            );

            tiles.add(scale(tile, 2));
        }

        return List.copyOf(tiles);
    }

    public List<BufferedImage> splitForRetry(BufferedImage image) {
        if (image.getWidth() < 2 && image.getHeight() < 2) {
            throw new IllegalArgumentException("A região é pequena demais para ser subdividida");
        }

        if (image.getWidth() >= image.getHeight() && image.getWidth() >= 2) {
            int midpoint = image.getWidth() / 2;
            int overlap = retryOverlap(image.getWidth(), midpoint);

            return List.of(
                    copyRegion(image, 0, 0, midpoint + overlap, image.getHeight()),
                    copyRegion(
                            image,
                            midpoint - overlap,
                            0,
                            image.getWidth() - midpoint + overlap,
                            image.getHeight()
                    )
            );
        }

        int midpoint = image.getHeight() / 2;
        int overlap = retryOverlap(image.getHeight(), midpoint);

        return List.of(
                copyRegion(image, 0, 0, image.getWidth(), midpoint + overlap),
                copyRegion(
                        image,
                        0,
                        midpoint - overlap,
                        image.getWidth(),
                        image.getHeight() - midpoint + overlap
                )
        );
    }

    private int headerHeight(BufferedImage image) {
        double ratio = clamp(properties.headerRatio(), 0.05, 0.45);
        return Math.max(1, Math.min(image.getHeight(), (int) Math.round(image.getHeight() * ratio)));
    }

    private BufferedImage copyRegion(
            BufferedImage source,
            int x,
            int y,
            int width,
            int height
    ) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = result.createGraphics();
        graphics.drawImage(source, 0, 0, width, height, x, y, x + width, y + height, null);
        graphics.dispose();
        return result;
    }

    private BufferedImage scale(BufferedImage source, int factor) {
        if (factor <= 1) {
            return source;
        }

        BufferedImage result = new BufferedImage(
                source.getWidth() * factor,
                source.getHeight() * factor,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D graphics = result.createGraphics();
        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );
        graphics.drawImage(source, 0, 0, result.getWidth(), result.getHeight(), null);
        graphics.dispose();
        source.flush();
        return result;
    }

    private double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private int retryOverlap(int dimension, int midpoint) {
        int requested = (int) Math.round(dimension * RETRY_OVERLAP);
        return Math.min(Math.max(0, requested), Math.max(0, midpoint - 1));
    }
}
