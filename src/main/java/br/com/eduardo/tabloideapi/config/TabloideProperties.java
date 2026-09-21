package br.com.eduardo.tabloideapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tabloide")
public record TabloideProperties(
        String extractor,
        Ollama ollama,
        Image image
) {

    public TabloideProperties {
        extractor = extractor == null ? "local-vision" : extractor;
        ollama = ollama == null ? new Ollama(null, null, null, null, null) : ollama;
        image = image == null ? new Image(null, null, null, null) : image;
    }

    public record Ollama(
            String baseUrl,
            String model,
            Integer contextLength,
            Integer headerMaxTokens,
            Integer offersMaxTokens
    ) {

        public Ollama {
            baseUrl = baseUrl == null ? "http://localhost:11434" : baseUrl;
            model = model == null ? "qwen3-vl:4b-instruct" : model;
            contextLength = contextLength == null ? 8192 : contextLength;
            headerMaxTokens = headerMaxTokens == null ? 512 : headerMaxTokens;
            offersMaxTokens = offersMaxTokens == null ? 1800 : offersMaxTokens;
        }
    }

    public record Image(
            Double headerRatio,
            Integer headerScale,
            Integer bodyTiles,
            Double tileOverlap
    ) {

        public Image {
            headerRatio = headerRatio == null ? 0.20 : headerRatio;
            headerScale = headerScale == null ? 3 : headerScale;
            bodyTiles = bodyTiles == null ? 2 : bodyTiles;
            tileOverlap = tileOverlap == null ? 0.12 : tileOverlap;
        }
    }
}
