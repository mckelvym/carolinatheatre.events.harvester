package carolinatheatre.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ImageExtractorTest {

    private ImageExtractor extractor;

    @Test
    void extract_withAbsoluteUrl_returnsAbsoluteUrl() {
        String html = "<div>"
            + "<div class=\"eventCard__image\">"
            + "<img src=\"https://carolinatheatre.org/images/event.jpg\" />"
            + "</div>"
            + "</div>";
        Element element = Jsoup.parse(html, "https://carolinatheatre.org").body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("https://carolinatheatre.org/images/event.jpg");
    }

    @Test
    void extract_withBlankSrcAttribute_returnsNull() {
        String html = "<div>"
            + "<div class=\"eventCard__image\">"
            + "<img src=\"\" />"
            + "</div>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isNull();
    }

    @Test
    void extract_withContainerButNoImgTag_returnsNull() {
        String html = "<div>"
            + "<div class=\"eventCard__image\">"
            + "<p>No image here</p>"
            + "</div>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isNull();
    }

    @Test
    void extract_withEmptyImageContainer_returnsNull() {
        String html = "<div>"
            + "<div class=\"eventCard__image\"></div>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isNull();
    }

    @Test
    void extract_withImgTagAttributes_extractsSrcAttribute() {
        String html = "<div>"
            + "<div class=\"eventCard__image\">"
            + "<img src=\"/images/event.jpg\" alt=\"Event poster\" class=\"poster\" />"
            + "</div>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("/images/event.jpg");
    }

    @Test
    void extract_withMultipleImages_returnsFirstImage() {
        String html = "<div>"
            + "<div class=\"eventCard__image\">"
            + "<img src=\"/images/first.jpg\" />"
            + "<img src=\"/images/second.jpg\" />"
            + "</div>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("/images/first.jpg");
    }

    @Test
    void extract_withNoImageContainer_returnsNull() {
        String html = "<div><p>Some content</p></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isNull();
    }

    @Test
    void extract_withNullElement_throwsNullPointerException() {
        assertThatThrownBy(() -> extractor.extract(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("element must not be null");
    }

    @Test
    void extract_withRelativeUrl_convertsToAbsoluteUrl() {
        String html = "<div>"
            + "<div class=\"eventCard__image\">"
            + "<img src=\"/images/poster.png\" />"
            + "</div>"
            + "</div>";
        Element element = Jsoup.parse(html, "https://carolinatheatre.org").body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("https://carolinatheatre.org/images/poster.png");
    }

    @Test
    void extract_withValidImageInContainer_returnsImageUrl() {
        String html = "<div>"
            + "<div class=\"eventCard__image\">"
            + "<img src=\"/images/event.jpg\" />"
            + "</div>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("/images/event.jpg");
    }

    @Test
    void extract_withWhitespaceSrcAttribute_returnsNull() {
        String html = "<div>"
            + "<div class=\"eventCard__image\">"
            + "<img src=\"   \" />"
            + "</div>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isNull();
    }

    @BeforeEach
    void setUp() {
        extractor = new ImageExtractor();
    }
}
