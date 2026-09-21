package br.com.eduardo.tabloideapi.extractor;

import br.com.eduardo.tabloideapi.dto.TabloideResponse;

import java.awt.image.BufferedImage;

public interface TabloideExtractor {

    String type();

    TabloideResponse extract(BufferedImage image);
}
