package br.com.eduardo.tabloideapi.image;

import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;

@Component
public class ImageCropper {

    public BufferedImage crop(
            BufferedImage image,
            int x,
            int y,
            int width,
            int height
    ) {

        return image.getSubimage(
                x,
                y,
                width,
                height
        );
    }
}