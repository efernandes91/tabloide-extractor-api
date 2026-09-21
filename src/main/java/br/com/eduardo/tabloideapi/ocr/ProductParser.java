package br.com.eduardo.tabloideapi.ocr;

import br.com.eduardo.tabloideapi.dto.OcrWord;
import br.com.eduardo.tabloideapi.dto.ProductCandidate;
import br.com.eduardo.tabloideapi.dto.ProdutoResponse;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProductParser {

    public Optional<ProdutoResponse> parse(ProductCandidate candidate) {

        if (candidate.words().size() < 2) {
            return Optional.empty();
        }

        String nome = candidate.words().stream()
                .sorted(
                        Comparator
                                .comparingInt(OcrWord::y)
                                .thenComparingInt(OcrWord::x)
                )
                .map(OcrWord::text)
                .collect(Collectors.joining(" "));

        return Optional.of(
                new ProdutoResponse(
                        nome,
                        null,
                        null,
                        null,
                        candidate.price(),
                        null,
                        null,
                        nome
                )
        );
    }
}
