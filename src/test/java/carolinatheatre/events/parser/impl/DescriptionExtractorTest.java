package carolinatheatre.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DescriptionExtractorTest {

    private DescriptionExtractor extractor;

    @Test
    void extract_withBlankCategory_ignoresCategory() {
        String html = "<div>"
            + "<p class=\"event__categories\">   </p>"
            + "<div class=\"card__info\">"
            + "<p>7:00 PM</p>"
            + "</div>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("7:00 PM");
    }

    @Test
    void extract_withBlankParagraphsInCardInfo_ignoresBlankParagraphs() {
        String html = "<div>"
            + "<div class=\"card__info\">"
            + "<p>7:00 PM</p>"
            + "<p>   </p>"
            + "<p>Durham</p>"
            + "</div>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("7:00 PM | Durham");
    }

    @Test
    void extract_withCardInfoOnly_returnsCardInfo() {
        String html = "<div>"
            + "<div class=\"card__info\">"
            + "<p>8:30 PM</p>"
            + "<p>Main Stage</p>"
            + "</div>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("8:30 PM | Main Stage");
    }

    @Test
    void extract_withCategoryAndCardInfo_returnsCombinedDescription() {
        String html = "<div>"
            + "<p class=\"event__categories\">Film</p>"
            + "<div class=\"card__info\">"
            + "<p>7:00 PM</p>"
            + "<p>Durham, NC</p>"
            + "</div>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("Category: Film | 7:00 PM | Durham, NC");
    }

    @Test
    void extract_withCategoryOnly_returnsCategoryWithPrefix() {
        String html = "<div>"
            + "<p class=\"event__categories\">Music</p>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("Category: Music");
    }

    @Test
    void extract_withComplexStructure_extractsAllParts() {
        String html = "<div>"
            + "<p class=\"event__categories\">Special Event</p>"
            + "<div class=\"card__info\">"
            + "<p>Saturday, January 20</p>"
            + "<p>Doors: 6:30 PM</p>"
            + "<p>Show: 7:30 PM</p>"
            + "<p>Carolina Theatre</p>"
            + "</div>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo(
            "Category: Special Event | Saturday, January 20 | Doors: 6:30 PM | Show: 7:30 PM | "
                + "Carolina Theatre");
    }

    @Test
    void extract_withEmptyCardInfo_returnsCategoryOnly() {
        String html = "<div>"
            + "<p class=\"event__categories\">Theater</p>"
            + "<div class=\"card__info\"></div>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("Category: Theater");
    }

    @Test
    void extract_withMultipleParagraphsInCardInfo_joinsAllParts() {
        String html = "<div>"
            + "<div class=\"card__info\">"
            + "<p>Friday, Dec 15</p>"
            + "<p>9:00 PM</p>"
            + "<p>Studio Theatre</p>"
            + "</div>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("Friday, Dec 15 | 9:00 PM | Studio Theatre");
    }

    @Test
    void extract_withNoMatchingElements_returnsDefaultDescription() {
        String html = "<div><span>Some text</span></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("Event at Carolina Theatre of Durham");
    }

    @Test
    void extract_withNullElement_throwsNullPointerException() {
        assertThatThrownBy(() -> extractor.extract(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("element must not be null");
    }

    @Test
    void extract_withWhitespace_returnsTrimmedDescription() {
        String html = "<div>"
            + "<p class=\"event__categories\">  Comedy  </p>"
            + "<div class=\"card__info\">"
            + "<p>  6:00 PM  </p>"
            + "</div>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("Category: Comedy | 6:00 PM");
    }

    @BeforeEach
    void setUp() {
        extractor = new DescriptionExtractor();
    }
}
