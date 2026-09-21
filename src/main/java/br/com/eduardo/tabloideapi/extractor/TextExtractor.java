package br.com.eduardo.tabloideapi.extractor;

import br.com.eduardo.tabloideapi.dto.OcrResult;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;

public interface TextExtractor {

    OcrResult extract(BufferedImage image);
}