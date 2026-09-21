package br.com.eduardo.tabloideapi.exception;

public class TabloideExtractionException extends RuntimeException {

    public TabloideExtractionException(String message) {
        super(message);
    }

    public TabloideExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
