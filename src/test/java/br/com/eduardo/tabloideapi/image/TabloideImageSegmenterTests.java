package br.com.eduardo.tabloideapi.image;

import br.com.eduardo.tabloideapi.config.TabloideProperties;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TabloideImageSegmenterTests {

    private final TabloideImageSegmenter segmenter =
            new TabloideImageSegmenter(new TabloideProperties(null, null, null));

    @Test
    void ampliaCabecalhoEDivideCorpoComSobreposicao() {
        BufferedImage image = new BufferedImage(500, 700, BufferedImage.TYPE_INT_RGB);

        BufferedImage header = segmenter.header(image);
        List<BufferedImage> tiles = segmenter.bodyTiles(image);

        assertThat(header.getWidth()).isEqualTo(1500);
        assertThat(header.getHeight()).isEqualTo(420);
        assertThat(tiles).hasSize(4);
        assertThat(tiles).allSatisfy(tile -> {
            assertThat(tile.getWidth()).isEqualTo(1000);
            assertThat(tile.getHeight()).isBetween(300, 360);
        });
    }

    @Test
    void subdivideRegiaoNoMaiorEixoComSobreposicao() {
        BufferedImage region = new BufferedImage(1000, 400, BufferedImage.TYPE_INT_RGB);

        List<BufferedImage> subdivisions = segmenter.splitForRetry(region);

        assertThat(subdivisions).hasSize(2);
        assertThat(subdivisions).allSatisfy(tile -> {
            assertThat(tile.getWidth()).isEqualTo(580);
            assertThat(tile.getHeight()).isEqualTo(400);
        });
    }
}
