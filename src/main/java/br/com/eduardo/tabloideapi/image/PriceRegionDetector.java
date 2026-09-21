package br.com.eduardo.tabloideapi.image;

import org.springframework.stereotype.Component;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

@Component
public class PriceRegionDetector {

    public List<ImageRegion> detect(BufferedImage image) {

        int width = image.getWidth();
        int height = image.getHeight();

        boolean[][] visited = new boolean[height][width];

        List<ImageRegion> regions = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                if (visited[y][x]) {
                    continue;
                }

                if (!isBlue(image.getRGB(x, y))) {
                    visited[y][x] = true;
                    continue;
                }

                ImageRegion region =
                        findConnectedRegion(
                                image,
                                x,
                                y,
                                visited
                        );

                if (isPossiblePriceRegion(region, image)
                        && hasYellowPixels(image, region)) {

                    regions.add(region);
                }
            }
        }

        return regions;
    }

    private ImageRegion findConnectedRegion(
            BufferedImage image,
            int startX,
            int startY,
            boolean[][] visited
    ) {

        Queue<int[]> queue = new ArrayDeque<>();

        queue.add(new int[]{startX, startY});
        visited[startY][startX] = true;

        int minX = startX;
        int maxX = startX;

        int minY = startY;
        int maxY = startY;

        while (!queue.isEmpty()) {

            int[] point = queue.poll();

            int x = point[0];
            int y = point[1];

            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);

            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);

            visitNeighbor(
                    image,
                    x + 1,
                    y,
                    visited,
                    queue
            );

            visitNeighbor(
                    image,
                    x - 1,
                    y,
                    visited,
                    queue
            );

            visitNeighbor(
                    image,
                    x,
                    y + 1,
                    visited,
                    queue
            );

            visitNeighbor(
                    image,
                    x,
                    y - 1,
                    visited,
                    queue
            );
        }

        return new ImageRegion(
                minX,
                minY,
                maxX - minX + 1,
                maxY - minY + 1
        );
    }

    private void visitNeighbor(
            BufferedImage image,
            int x,
            int y,
            boolean[][] visited,
            Queue<int[]> queue
    ) {

        if (x < 0
                || y < 0
                || x >= image.getWidth()
                || y >= image.getHeight()) {
            return;
        }

        if (visited[y][x]) {
            return;
        }

        visited[y][x] = true;

        if (isBlue(image.getRGB(x, y))) {
            queue.add(new int[]{x, y});
        }
    }

    private boolean isBlue(int rgb) {

        Color color = new Color(rgb);

        float[] hsb = Color.RGBtoHSB(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                null
        );

        float hue = hsb[0];
        float saturation = hsb[1];
        float brightness = hsb[2];

        return hue >= 0.52f
                && hue <= 0.72f
                && saturation >= 0.35f
                && brightness >= 0.15f;
    }

    private boolean isPossiblePriceRegion(
            ImageRegion region,
            BufferedImage image
    ) {

        int minWidth = 30;
        int maxWidth = image.getWidth() / 3;

        int minHeight = 15;
        int maxHeight = image.getHeight() / 6;

        double aspectRatio =
                (double) region.width() / region.height();

        return region.width() >= minWidth
                && region.width() <= maxWidth
                && region.height() >= minHeight
                && region.height() <= maxHeight
                && aspectRatio >= 1.2
                && aspectRatio <= 5.0;
    }

    private boolean hasYellowPixels(
            BufferedImage image,
            ImageRegion region
    ) {

        int yellowPixels = 0;
        int totalPixels = region.width() * region.height();

        for (int y = region.y();
             y < region.y() + region.height();
             y++) {

            for (int x = region.x();
                 x < region.x() + region.width();
                 x++) {

                Color color = new Color(
                        image.getRGB(x, y)
                );

                boolean yellow =
                        color.getRed() > 170
                                && color.getGreen() > 130
                                && color.getBlue() < 140;

                if (yellow) {
                    yellowPixels++;
                }
            }
        }

        double yellowRatio =
                (double) yellowPixels / totalPixels;

        return yellowRatio >= 0.03;
    }
}