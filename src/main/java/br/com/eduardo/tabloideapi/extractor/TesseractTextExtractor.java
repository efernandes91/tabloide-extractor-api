package br.com.eduardo.tabloideapi.extractor;

import br.com.eduardo.tabloideapi.dto.OcrResult;
import br.com.eduardo.tabloideapi.dto.OcrWord;
import br.com.eduardo.tabloideapi.image.ImagePreprocessor;
import br.com.eduardo.tabloideapi.image.PriceImagePreprocessor;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

@Component
public class TesseractTextExtractor {

    private final ImagePreprocessor imagePreprocessor;
    private final PriceImagePreprocessor priceImagePreprocessor;

    public TesseractTextExtractor(ImagePreprocessor imagePreprocessor,
                                  PriceImagePreprocessor priceImagePreprocessor) {

        this.imagePreprocessor = imagePreprocessor;
        this.priceImagePreprocessor = priceImagePreprocessor;
    }

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
