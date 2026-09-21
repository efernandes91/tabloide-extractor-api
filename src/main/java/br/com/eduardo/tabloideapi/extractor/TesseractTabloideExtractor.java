package br.com.eduardo.tabloideapi.extractor;

import br.com.eduardo.tabloideapi.dto.CategoriaResponse;
import br.com.eduardo.tabloideapi.dto.OcrResult;
import br.com.eduardo.tabloideapi.dto.OcrWord;
import br.com.eduardo.tabloideapi.dto.ProdutoResponse;
import br.com.eduardo.tabloideapi.dto.TabloideResponse;
import br.com.eduardo.tabloideapi.dto.ValidadeResponse;
import br.com.eduardo.tabloideapi.image.ImageCropper;
import br.com.eduardo.tabloideapi.ocr.PriceParser;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TesseractTabloideExtractor implements TabloideExtractor {

    private final TesseractTextExtractor textExtractor;
    private final ImageCropper imageCropper;
    private final PriceParser priceParser;

    public TesseractTabloideExtractor(
            TesseractTextExtractor textExtractor,
            ImageCropper imageCropper,
            PriceParser priceParser
    ) {
        this.textExtractor = textExtractor;
        this.imageCropper = imageCropper;
        this.priceParser = priceParser;
    }

    @Override
    public String type() {
        return "tesseract";
    }

    @Override
    public TabloideResponse extract(BufferedImage image) {
        BufferedImage productRegion = imageCropper.crop(image, 0, 295, 155, 105);
        OcrResult ocrResult = textExtractor.extract(productRegion);

        String productName = ocrResult.words().stream()
                .filter(word -> word.confidence() >= 70)
                .filter(word -> word.y() < 200)
                .sorted(Comparator.comparingInt(OcrWord::y).thenComparingInt(OcrWord::x))
                .map(OcrWord::text)
                .collect(Collectors.joining(" "));

        BufferedImage priceRegion = imageCropper.crop(image, 105, 355, 45, 28);
        BigDecimal price = priceParser.parse(textExtractor.extractPrice(priceRegion))
                .orElseThrow(() -> new IllegalStateException("Preço não identificado pelo Tesseract"));

        ProdutoResponse product = new ProdutoResponse(
                productName,
                null,
                null,
                null,
                price,
                null,
                null,
                productName
        );

        return new TabloideResponse(
                "Redepas Cortez",
                new ValidadeResponse(LocalDate.of(2026, 9, 15), LocalDate.of(2026, 9, 17)),
                List.of(new CategoriaResponse("Mercearia", List.of(product)))
        );
    }
}
