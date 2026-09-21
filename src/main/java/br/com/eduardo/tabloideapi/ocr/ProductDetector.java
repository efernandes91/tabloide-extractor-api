package br.com.eduardo.tabloideapi.ocr;

import br.com.eduardo.tabloideapi.dto.OcrPrice;
import br.com.eduardo.tabloideapi.dto.OcrWord;
import br.com.eduardo.tabloideapi.dto.ProductCandidate;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class ProductDetector {

    public List<ProductCandidate> detect(
            List<OcrPrice> prices,
            List<OcrWord> words
    ) {

        return prices.stream()
                .map(price -> new ProductCandidate(
                        price.value(),
                        findWordsNearPrice(price, words)
                ))
                .toList();
    }

    private List<OcrWord> findWordsNearPrice(
            OcrPrice price,
            List<OcrWord> words
    ) {

        int horizontalDistance = 300;
        int verticalDistance = 250;

        return words.stream()
                .filter(word -> {

                    int deltaX = Math.abs(
                            word.centerX() - price.x()
                    );

                    int deltaY =
                            price.y() - word.centerY();

                    return deltaX <= horizontalDistance
                            && deltaY > 0
                            && deltaY <= verticalDistance;
                })
                .sorted(
                        Comparator
                                .comparingInt(OcrWord::y)
                                .thenComparingInt(OcrWord::x)
                )
                .toList();
    }
}