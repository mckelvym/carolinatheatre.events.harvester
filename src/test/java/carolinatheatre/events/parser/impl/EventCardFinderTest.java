package carolinatheatre.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventCardFinderTest {

    private EventCardFinder finder;

    @Test
    void extractEventUrl_withAbsoluteLink_returnsUrl() {
        String html = "<div class='eventCard event'>"
            + "<a href='https://carolinatheatre.org/event-slug/'>Event Link</a>"
            + "</div>";
        Document doc = Jsoup.parse(html);
        Element card = doc.selectFirst("div.eventCard");

        String url = finder.extractEventUrl(card);

        assertThat(url).isEqualTo("https://carolinatheatre.org/event-slug/");
    }

    @Test
    void extractEventUrl_withNestedLink_findsLink() {
        String html = "<div class='eventCard event'>"
            + "<div class='wrapper'>"
            + "<div class='content'>"
            + "<a href='https://carolinatheatre.org/nested-event/'>Nested Event</a>"
            + "</div></div></div>";
        Document doc = Jsoup.parse(html);
        Element card = doc.selectFirst("div.eventCard");

        String url = finder.extractEventUrl(card);

        assertThat(url).isEqualTo("https://carolinatheatre.org/nested-event/");
    }

    @Test
    void extractEventUrl_withNoLink_returnsEmptyString() {
        String html = "<div class='eventCard event'><p>No link</p></div>";
        Document doc = Jsoup.parse(html);
        Element card = doc.selectFirst("div.eventCard");

        String url = finder.extractEventUrl(card);

        assertThat(url).isEmpty();
    }

    @Test
    void extractEventUrl_withValidLink_returnsAbsoluteUrl() {
        String html = "<div class='eventCard event'>"
            + "<a href='/event-slug/'>Event Link</a>"
            + "</div>";
        Document doc = Jsoup.parse(html, "https://carolinatheatre.org/");
        Element card = doc.selectFirst("div.eventCard");

        String url = finder.extractEventUrl(card);

        assertThat(url).isEqualTo("https://carolinatheatre.org/event-slug/");
    }

    @Test
    void findEventCards_filtersOutElementsWithBaseUrlOnly() {
        String html = "<div class='eventCard event'>"
            + "<a href='https://carolinatheatre.org/'>Base URL</a>"
            + "</div>"
            + "<div class='eventCard event'>"
            + "<a href='https://carolinatheatre.org/event/'>Valid Event</a>"
            + "</div>";
        Document doc = Jsoup.parse(html);

        List<Element> cards = finder.findEventElements(doc);

        assertThat(cards).hasSize(1);
    }

    @Test
    void findEventCards_filtersOutElementsWithBlankHref() {
        String html = "<div class='eventCard event'>"
            + "<a href=''>Empty Link</a>"
            + "</div>"
            + "<div class='eventCard event'>"
            + "<a href='https://carolinatheatre.org/valid-event/'>Valid Event</a>"
            + "</div>";
        Document doc = Jsoup.parse(html);

        List<Element> cards = finder.findEventElements(doc);

        assertThat(cards).hasSize(1);
    }

    @Test
    void findEventCards_filtersOutElementsWithEventsPath() {
        String html = "<div class='eventCard event'>"
            + "<a href='https://carolinatheatre.org/events/'>Events Page</a>"
            + "</div>"
            + "<div class='eventCard event'>"
            + "<a href='https://carolinatheatre.org/special-show/'>Valid Event</a>"
            + "</div>";
        Document doc = Jsoup.parse(html);

        List<Element> cards = finder.findEventElements(doc);

        assertThat(cards).hasSize(1);
    }

    @Test
    void findEventCards_filtersOutElementsWithoutLinks() {
        String html = "<div class='eventCard event'>"
            + "<p>No link here</p>"
            + "</div>"
            + "<div class='eventCard event'>"
            + "<a href='https://carolinatheatre.org/valid-event/'>Valid Event</a>"
            + "</div>";
        Document doc = Jsoup.parse(html);

        List<Element> cards = finder.findEventElements(doc);

        assertThat(cards).hasSize(1);
    }

    @Test
    void findEventCards_withMixedValidAndInvalidElements() {
        String html = "<div class='eventCard event'>"
            + "<a href='https://carolinatheatre.org/events/'>Invalid - Events Path</a>"
            + "</div>"
            + "<div class='eventCard event'>"
            + "<a href='https://carolinatheatre.org/concert/'>Valid Event 1</a>"
            + "</div>"
            + "<div class='eventCard event'></div>"
            + "<div class='eventCard event'>"
            + "<a href='https://carolinatheatre.org/theater-show/'>Valid Event 2</a>"
            + "</div>"
            + "<div class='eventCard event'>"
            + "<a href=''>Empty Link</a>"
            + "</div>";
        Document doc = Jsoup.parse(html);

        List<Element> cards = finder.findEventElements(doc);

        assertThat(cards).hasSize(2);
    }

    @Test
    void findEventCards_withNoElements_returnsEmptyList() {
        String html = "<div class='container'><p>No events</p></div>";
        Document doc = Jsoup.parse(html);

        List<Element> cards = finder.findEventElements(doc);

        assertThat(cards).isEmpty();
    }

    @Test
    void findEventCards_withValidCards_findsMultipleElements() {
        String html = "<div class='eventCard event'>"
            + "<a href='https://carolinatheatre.org/event-one/'>Event One</a>"
            + "</div>"
            + "<div class='eventCard event'>"
            + "<a href='https://carolinatheatre.org/event-two/'>Event Two</a>"
            + "</div>";
        Document doc = Jsoup.parse(html);

        List<Element> cards = finder.findEventElements(doc);

        assertThat(cards).hasSize(2);
    }

    @Test
    void findEventElements_filtersOutNonCarolinaTheatreUrls() {
        String html = "<div class='eventCard event'>"
            + "<a href='https://example.com/event/'>External Link</a>"
            + "</div>"
            + "<div class='eventCard event'>"
            + "<a href='https://carolinatheatre.org/movie-night/'>Valid Event</a>"
            + "</div>";
        Document doc = Jsoup.parse(html);

        List<Element> cards = finder.findEventElements(doc);

        assertThat(cards).hasSize(1);
    }

    @Test
    void findEventElements_preservesCardOrder() {
        String html = "<div class='eventCard event'>"
            + "<a href='https://carolinatheatre.org/first/'>First</a>"
            + "</div>"
            + "<div class='eventCard event'>"
            + "<a href='https://carolinatheatre.org/second/'>Second</a>"
            + "</div>"
            + "<div class='eventCard event'>"
            + "<a href='https://carolinatheatre.org/third/'>Third</a>"
            + "</div>";
        Document doc = Jsoup.parse(html);

        List<Element> cards = finder.findEventElements(doc);

        assertThat(cards).hasSize(3);
        assertThat(finder.extractEventUrl(cards.get(0)))
            .isEqualTo("https://carolinatheatre.org/first/");
        assertThat(finder.extractEventUrl(cards.get(1)))
            .isEqualTo("https://carolinatheatre.org/second/");
        assertThat(finder.extractEventUrl(cards.get(2)))
            .isEqualTo("https://carolinatheatre.org/third/");
    }

    @Test
    void findEventElements_withEmptyDocument_returnsEmptyList() {
        Document doc = Jsoup.parse("");

        List<Element> cards = finder.findEventElements(doc);

        assertThat(cards).isEmpty();
    }

    @Test
    void findEventElements_withRealWorldStructure() {
        String html = "<div class='eventCard event'>"
            + "<div class='eventCard__image'>"
            + "<img src='event1.jpg'/>"
            + "</div>"
            + "<div class='card__info'>"
            + "<p class='card__title'><a href='https://carolinatheatre"
            + ".org/summer-concert/'>Summer Concert</a></p>"
            + "<div class='event__dateBox'>"
            + "<span class='month'>JUN</span><span class='day'>15</span>"
            + "</div>"
            + "</div></div>"
            + "<div class='eventCard event'>"
            + "<div class='eventCard__image'>"
            + "<img src='event2.jpg'/>"
            + "</div>"
            + "<div class='card__info'>"
            + "<p class='card__title'><a href='https://carolinatheatre.org/film-festival/'>Film "
            + "Festival</a></p>"
            + "<div class='event__dateBox'>"
            + "<span class='month'>JUL</span><span class='day'>20</span>"
            + "</div>"
            + "</div></div>";
        Document doc = Jsoup.parse(html);

        List<Element> cards = finder.findEventElements(doc);

        assertThat(cards).hasSize(2);
        assertThat(finder.extractEventUrl(cards.get(0)))
            .isEqualTo("https://carolinatheatre.org/summer-concert/");
        assertThat(finder.extractEventUrl(cards.get(1)))
            .isEqualTo("https://carolinatheatre.org/film-festival/");
    }

    @BeforeEach
    void setUp() {
        finder = new EventCardFinder();
    }
}
