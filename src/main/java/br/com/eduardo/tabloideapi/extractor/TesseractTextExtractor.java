package br.com.eduardo.tabloideapi.extractor;

import br.com.eduardo.tabloideapi.dto.OcrResult;
import br.com.eduardo.tabloideapi.dto.OcrWord;
import br.com.eduardo.tabloideapi.image.ImagePreprocessor;
import br.com.eduardo.tabloideapi.image.PriceImagePreprocessor;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class TesseractTextExtractor implements TextExtractor {

    private final ImagePreprocessor imagePreprocessor;
    private final PriceImagePreprocessor priceImagePreprocessor;

    public TesseractTextExtractor(ImagePreprocessor imagePreprocessor,
                                  PriceImagePreprocessor priceImagePreprocessor) {

        this.imagePreprocessor = imagePreprocessor;
        this.priceImagePreprocessor = priceImagePreprocessor;
    }

    @Override
    public OcrResult extract(BufferedImage image) {

        try {
            BufferedImage processedImage =
                    imagePreprocessor.preprocess(image);

            Tesseract tesseract = new Tesseract();

            tesseract.setDatapath("./tessdata");
            tesseract.setLanguage("por");

            tesseract.setPageSegMode(
                    ITessAPI.TessPageSegMode.PSM_SPARSE_TEXT
            );

            String rawText =
                    tesseract.doOCR(processedImage);

            List<Word> words = tesseract.getWords(
                    processedImage,
                    ITessAPI.TessPageIteratorLevel.RIL_WORD
            );

            List<OcrWord> ocrWords = words.stream()
                    .map(word -> {
                        Rectangle box = word.getBoundingBox();

                        return new OcrWord(
                                word.getText(),
                                box.x,
                                box.y,
                                box.width,
                                box.height,
                                word.getConfidence()
                        );
                    })
                    .toList();

            return new OcrResult(
                    rawText,
                    ocrWords
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao processar imagem",
                    e
            );
        }
    }

    public String extractPrice(BufferedImage image) {

        try {
            BufferedImage processedImage =
                    priceImagePreprocessor.preprocess(image);

            ImageIO.write(
                    processedImage,
                    "png",
                    new File("debug-price.png")
            );

            Tesseract tesseract = new Tesseract();

            tesseract.setDatapath("./tessdata");
            tesseract.setLanguage("por");

            tesseract.setPageSegMode(
                    ITessAPI.TessPageSegMode.PSM_SINGLE_LINE
            );

            tesseract.setVariable(
                    "tessedit_char_whitelist",
                    "0123456789,."
            );

            return tesseract.doOCR(processedImage);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao processar preço",
                    e
            );
        }
    }


}