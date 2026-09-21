package br.com.eduardo.tabloideapi.image;

import org.springframework.stereotype.Component;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

@Component
public class PriceImagePreprocessor {

    public BufferedImage preprocess(BufferedImage original) {

        int scale = 6;

        BufferedImage resized = new BufferedImage(
                original.getWidth() * scale,
                original.getHeight() * scale,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D graphics = resized.createGraphics();

        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );

        graphics.drawImage(
                original,
                0,
                0,
                resized.getWidth(),
                resized.getHeight(),
                null
        );

        graphics.dispose();

        BufferedImage binary = new BufferedImage(
                resized.getWidth(),
                resized.getHeight(),
                BufferedImage.TYPE_BYTE_BINARY
        );

        for (int y = 0; y < resized.getHeight(); y++) {
            for (int x = 0; x < resized.getWidth(); x++) {

                Color color = new Color(
                        resized.getRGB(x, y)
                );

                boolean yellow =
                        color.getRed() > 170
                                && color.getGreen() > 130
                                && color.getBlue() < 140;

                binary.setRGB(
                        x,
                        y,
                        yellow
                                ? Color.BLACK.getRGB()
                                : Color.WHITE.getRGB()
                );
            }
        }

        int padding = 20;

        BufferedImage padded = new BufferedImage(
                binary.getWidth() + padding * 2,
                binary.getHeight() + padding * 2,
                BufferedImage.TYPE_BYTE_BINARY
        );

        Graphics2D paddedGraphics = padded.createGraphics();

        paddedGraphics.setColor(Color.WHITE);
        paddedGraphics.fillRect(
                0,
                0,
                padded.getWidth(),
                padded.getHeight()
        );

        paddedGraphics.drawImage(
                binary,
                padding,
                padding,
                null
        );

        paddedGraphics.dispose();

        return padded;
    }
}