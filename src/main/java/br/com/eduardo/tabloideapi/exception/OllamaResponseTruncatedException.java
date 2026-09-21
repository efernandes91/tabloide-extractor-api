package br.com.eduardo.tabloideapi.exception;

public class OllamaResponseTruncatedException extends TabloideExtractionException {

    private final int tokenLimit;
    private final Integer generatedTokens;

    public OllamaResponseTruncatedException(int tokenLimit, Integer generatedTokens) {
        this(tokenLimit, generatedTokens, null);
    }

    public OllamaResponseTruncatedException(
            int tokenLimit,
            Integer generatedTokens,
            Throwable cause
    ) {
        super("O Ollama atingiu o limite de %d tokens antes de concluir o JSON"
                .formatted(tokenLimit), cause);
        this.tokenLimit = tokenLimit;
        this.generatedTokens = generatedTokens;
    }

    public int tokenLimit() {
        return tokenLimit;
    }

    public Integer generatedTokens() {
        return generatedTokens;
    }
}
