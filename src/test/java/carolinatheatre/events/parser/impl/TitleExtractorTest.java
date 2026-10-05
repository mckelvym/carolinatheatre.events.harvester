package carolinatheatre.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TitleExtractorTest {

    private TitleExtractor extractor;

    @Test
    void extract_withBlankCardTitle_fallsBackToHeading() {
        String html = "<div>"
            + "<p class=\"card__title\">   </p>"
            + "<h2>Heading Title</h2>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("Heading Title");
    }

    @Test
    void extract_withBlankHeading_returnsEmptyString() {
        String html = "<div>"
            + "<h2>  </h2>"
            + "<a href=\"/event\">Link Title</a>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEmpty();
    }

    @Test
    void extract_withBlankLink_returnsEmptyString() {
        String html = "<div><a href=\"/event\">  </a></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEmpty();
    }

    @Test
    void extract_withCardTitlePriority_ignoresHeadingAndLink() {
        String html = "<div>"
            + "<p class=\"card__title\">Card Title</p>"
            + "<h1>Heading Title</h1>"
            + "<a href=\"/event\">Link Title</a>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("Card Title");
    }

    @Test
    void extract_withCardTitle_returnsCardTitle() {
        String html = "<div><p class=\"card__title\">Movie Night</p></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("Movie Night");
    }

    @Test
    void extract_withHeadingAndNoCardTitle_returnsHeadingText() {
        String html = "<div><h2>Live Performance</h2></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("Live Performance");
    }

    @Test
    void extract_withHeadingPriority_ignoresLink() {
        String html = "<div>"
            + "<h2>Heading Title</h2>"
            + "<a href=\"/event\">Link Title</a>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("Heading Title");
    }

    @Test
    void extract_withLinkAndNoOtherElements_returnsLinkText() {
        String html = "<div><a href=\"/event/123\">Concert</a></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("Concert");
    }

    @Test
    void extract_withMultipleHeadings_returnsFirstHeading() {
        String html = "<div><h3>First Show</h3><h2>Second Show</h2></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("First Show");
    }

    @Test
    void extract_withNoMatchingElements_returnsEmptyString() {
        String html = "<div><p>Some text</p><span>More text</span></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEmpty();
    }

    @Test
    void extract_withNullElement_throwsNullPointerException() {
        assertThatThrownBy(() -> extractor.extract(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("element must not be null");
    }

    @Test
    void extract_withWhitespaceInTitle_returnsTrimmedTitle() {
        String html = "<div><p class=\"card__title\">  Theater Event  </p></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("Theater Event");
    }

    @BeforeEach
    void setUp() {
        extractor = new TitleExtractor();
    }
}
