package br.com.eduardo.tabloideapi.extractor;

import br.com.eduardo.tabloideapi.config.TabloideProperties;
import br.com.eduardo.tabloideapi.dto.VisionOffers;
import br.com.eduardo.tabloideapi.exception.OllamaResponseTruncatedException;
import br.com.eduardo.tabloideapi.exception.TabloideExtractionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OllamaVisionClientTests {

    private static final String JSON_SCHEMA = "{\"type\":\"object\"}";

    private MockRestServiceServer server;
    private OllamaVisionClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        TabloideProperties properties = new TabloideProperties(
                "local-vision",
                new TabloideProperties.Ollama(
                        "http://localhost:11434",
                        "qwen3-vl:4b-instruct",
                        8192,
                        512,
                        1800
                ),
                null
        );

        client = new OllamaVisionClient(builder, new ObjectMapper(), properties);
    }

    @Test
    void detectaRespostaTruncadaAntesDeInterpretarOJson() {
        respondWith("""
                {
                  "message": {"content": "{\\\"ofertas\\\":[{\\\"nome\\\":\\\"incompleto"},
                  "done": true,
                  "done_reason": "length",
                  "eval_count": 1800
                }
                """);

        assertThatThrownBy(() -> extract())
                .isInstanceOf(OllamaResponseTruncatedException.class)
                .hasMessageContaining("limite de 1800 tokens");

        server.verify();
    }

    @Test
    void interpretaRespostaCompleta() {
        respondWith("""
                {
                  "message": {"content": "{\\\"ofertas\\\":[]}"},
                  "done": true,
                  "done_reason": "stop",
                  "eval_count": 8
                }
                """);

        VisionOffers result = extract();

        assertThat(result.ofertas()).isEmpty();
        server.verify();
    }

    @Test
    void converteJsonInvalidoEmFalhaControlada() {
        respondWith("""
                {
                  "message": {"content": "não é json"},
                  "done": true,
                  "done_reason": "stop",
                  "eval_count": 3
                }
                """);

        assertThatThrownBy(() -> extract())
                .isInstanceOf(TabloideExtractionException.class)
                .isNotInstanceOf(OllamaResponseTruncatedException.class)
                .hasMessage("O Ollama retornou um JSON inválido ou incompleto");

        server.verify();
    }

    private VisionOffers extract() {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        return client.extract(image, "extraia", JSON_SCHEMA, 1800, VisionOffers.class);
    }

    private void respondWith(String body) {
        server.expect(requestTo("http://localhost:11434/api/chat"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
    }
}
