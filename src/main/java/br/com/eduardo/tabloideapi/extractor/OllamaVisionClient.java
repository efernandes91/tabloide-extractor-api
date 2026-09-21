package br.com.eduardo.tabloideapi.extractor;

import br.com.eduardo.tabloideapi.config.TabloideProperties;
import br.com.eduardo.tabloideapi.exception.TabloideExtractionException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Component
public class OllamaVisionClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final TabloideProperties.Ollama properties;

    public OllamaVisionClient(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            TabloideProperties properties
    ) {
        this.properties = properties.ollama();
        this.restClient = restClientBuilder
                .baseUrl(this.properties.baseUrl())
                .build();
        this.objectMapper = objectMapper;
    }

    public <T> T extract(
            BufferedImage image,
            String prompt,
            String jsonSchema,
            int maxTokens,
            Class<T> responseType
    ) {
        try {
            JsonNode schema = objectMapper.readTree(jsonSchema);
            OllamaRequest request = new OllamaRequest(
                    properties.model(),
                    false,
                    false,
                    schema,
                    List.of(new Message("user", prompt, List.of(toBase64(image)))),
                    new Options(0, properties.contextLength(), maxTokens),
                    "2m"
            );

            OllamaResponse response = restClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(OllamaResponse.class);

            if (response == null
                    || response.message() == null
                    || response.message().content() == null
                    || response.message().content().isBlank()) {
                throw new TabloideExtractionException("O Ollama retornou uma resposta vazia");
            }

            return objectMapper.readValue(response.message().content(), responseType);
        } catch (RestClientException e) {
            throw new TabloideExtractionException(
                    "Não foi possível acessar o Ollama em " + properties.baseUrl(),
                    e
            );
        } catch (IOException e) {
            throw new TabloideExtractionException(
                    "Não foi possível processar a resposta estruturada do Ollama",
                    e
            );
        }
    }

    private String toBase64(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (!ImageIO.write(image, "png", output)) {
                throw new IOException("Não foi possível codificar a imagem como PNG");
            }
            return Base64.getEncoder().encodeToString(output.toByteArray());
        } finally {
            image.flush();
        }
    }

    private record OllamaRequest(
            String model,
            boolean stream,
            boolean think,
            JsonNode format,
            List<Message> messages,
            Options options,
            @JsonProperty("keep_alive") String keepAlive
    ) {
    }

    private record Message(
            String role,
            String content,
            List<String> images
    ) {
    }

    private record Options(
            double temperature,
            @JsonProperty("num_ctx") int contextLength,
            @JsonProperty("num_predict") int maxTokens
    ) {
    }

    private record OllamaResponse(ResponseMessage message) {
    }

    private record ResponseMessage(String content) {
    }
}
