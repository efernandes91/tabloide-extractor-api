package br.com.eduardo.tabloideapi.image;

import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;

@Component
public class ImagePreprocessor {

    public BufferedImage preprocess(BufferedImage original) {

        int scale = 3;

        BufferedImage resized = new BufferedImage(
                original.getWidth() * scale,
                original.getHeight() * scale,
                BufferedImage.TYPE_BYTE_GRAY
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

        return resized;
    }
}