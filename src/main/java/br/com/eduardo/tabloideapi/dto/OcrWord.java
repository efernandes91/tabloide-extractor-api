package br.com.eduardo.tabloideapi.dto;

public record OcrWord(
        String text,
        int x,
        int y,
        int width,
        int height,
        float confidence
) {

    public int centerX() {
        return x + width / 2;
    }

    public int centerY() {
        return y + height / 2;
    }

    public int right() {
        return x + width;
    }

    public int bottom() {
        return y + height;
    }
}
