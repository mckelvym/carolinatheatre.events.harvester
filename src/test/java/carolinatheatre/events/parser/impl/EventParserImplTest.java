package carolinatheatre.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;

import carolinatheatre.events.domain.EventItem;
import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for EventParserImpl.
 * Tests integration of all extractors and event parsing logic.
 */
class EventParserImplTest {

    private EventParserImpl parser;

    private static LocalDate expectedDate(Month month, int day) {
        LocalDate candidate = LocalDate.of(LocalDate.now().getYear(), month, day);
        return candidate.isBefore(LocalDate.now()) ? candidate.plusYears(1) : candidate;
    }

    @Test
    void parseEvent_withBlankTitle_returnsEmpty() {
        String html = """
            <html>
            <body>
                <div class="eventCard">
                    <h1>   </h1>
                    <div class="event__dateBox">
                        <span class="day">15</span>
                        <span class="month">Dec</span>
                    </div>
                </div>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);
        String eventUrl = "https://carolinatheatre.org/events/event/";

        Optional<EventItem> result = parser.parseEvent(doc, eventUrl);

        assertThat(result).isEmpty();
    }

    @Test
    void parseEvent_withCompleteEvent_returnsEventItem() {
        String html = """
            <html>
            <body>
                <div class="eventCard event">
                    <div class="eventCard__image">
                        <img src="https://example.com/nutcracker.jpg"/>
                    </div>
                    <p class="card__title">The Nutcracker</p>
                    <div class="event__dateBox">
                        <span class="day">15</span>
                        <span class="month">Mar</span>
                    </div>
                    <p class="event__categories">Ballet</p>
                    <div class="card__info">
                        <p>A classic holiday ballet performance</p>
                    </div>
                </div>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);
        String eventUrl = "https://carolinatheatre.org/events/nutcracker/";

        Optional<EventItem> result = parser.parseEvent(doc, eventUrl);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.id()).isEqualTo(eventUrl);
        assertThat(event.title()).isEqualTo("The Nutcracker");
        assertThat(event.link()).isEqualTo(eventUrl);
        assertThat(event.description()).contains("classic holiday ballet");
        assertThat(event.eventDateStart()).isEqualTo(expectedDate(Month.MARCH, 15));
        assertThat(event.eventDateEnd()).isNull();
        assertThat(event.imageUrl()).isEqualTo("https://example.com/nutcracker.jpg");
        assertThat(event.location()).isNull();
    }

    @Test
    void parseEvent_withComplexDescription_parsesCorrectly() {
        String html = """
            <html>
            <body>
                <div class="eventCard">
                    <h1>Movie Night</h1>
                    <div class="event__dateBox">
                        <span class="day">15</span>
                        <span class="month">Dec</span>
                    </div>
                    <p class="event__categories">Film</p>
                    <div class="card__info">
                        <p>Join us for a special screening</p>
                        <p>Doors open at 7pm</p>
                    </div>
                </div>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);
        String eventUrl = "https://carolinatheatre.org/events/movie/";

        Optional<EventItem> result = parser.parseEvent(doc, eventUrl);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.description()).contains("Category: Film");
        assertThat(event.description()).contains("special screening");
        assertThat(event.description()).contains("Doors open");
    }

    @Test
    void parseEvent_withMissingOptionalFields_returnsEventWithDefaults() {
        String html = """
            <html>
            <body>
                <div class="eventCard">
                    <h1>Simple Event</h1>
                    <div class="event__dateBox">
                        <span class="day">25</span>
                        <span class="month">Feb</span>
                    </div>
                </div>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);
        String eventUrl = "https://carolinatheatre.org/events/simple/";

        Optional<EventItem> result = parser.parseEvent(doc, eventUrl);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.title()).isEqualTo("Simple Event");
        assertThat(event.description()).isEqualTo("Event at Carolina Theatre of Durham");
        assertThat(event.imageUrl()).isNullOrEmpty();
    }

    @Test
    void parseEvent_withMultipleDateFormats_parsesCorrectly() {
        String[][] testCases = {
            {"15", "March"},
            {"1", "Feb"},
            {"28", "May"}
        };
        LocalDate[] expectedDates = {
            expectedDate(Month.MARCH, 15),
            expectedDate(Month.FEBRUARY, 1),
            expectedDate(Month.MAY, 28)
        };

        for (int i = 0; i < testCases.length; i++) {
            String day = testCases[i][0];
            String month = testCases[i][1];
            LocalDate expectedDate = expectedDates[i];

            String html = String.format("""
                <html>
                <body>
                    <div class="eventCard">
                        <h1>Test Event</h1>
                        <div class="event__dateBox">
                            <span class="day">%s</span>
                            <span class="month">%s</span>
                        </div>
                    </div>
                </body>
                </html>
                """, day, month);
            Document doc = Jsoup.parse(html);
            String eventUrl = "https://carolinatheatre.org/events/event/";

            Optional<EventItem> result = parser.parseEvent(doc, eventUrl);

            assertThat(result).as("Date: " + day + " " + month).isPresent();
            assertThat(result.get().eventDateStart()).isEqualTo(expectedDate);
        }
    }

    @Test
    void parseEvent_withMultipleImages_usesFirst() {
        String html = """
            <html>
            <body>
                <div class="eventCard">
                    <h1>Test Event</h1>
                    <div class="event__dateBox">
                        <span class="day">15</span>
                        <span class="month">Dec</span>
                    </div>
                    <div class="eventCard__image">
                        <img src="https://example.com/first.jpg"/>
                        <img src="https://example.com/second.jpg"/>
                    </div>
                </div>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);
        String eventUrl = "https://carolinatheatre.org/events/event/";

        Optional<EventItem> result = parser.parseEvent(doc, eventUrl);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.imageUrl()).isEqualTo("https://example.com/first.jpg");
    }

    @Test
    void parseEvent_withNoEventCardOrBody_returnsEmpty() {
        String html = "<html></html>";
        Document doc = Jsoup.parse(html);
        String eventUrl = "https://carolinatheatre.org/events/event/";

        Optional<EventItem> result = parser.parseEvent(doc, eventUrl);

        assertThat(result).isEmpty();
    }

    @Test
    void parseEvent_withNoEventCard_fallsBackToBody() {
        String html = """
            <html>
            <body>
                <h1>Concert Event</h1>
                <div class="event__dateBox">
                    <span class="day">20</span>
                    <span class="month">Jan</span>
                </div>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);
        String eventUrl = "https://carolinatheatre.org/events/concert/";

        Optional<EventItem> result = parser.parseEvent(doc, eventUrl);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.title()).isEqualTo("Concert Event");
        assertThat(event.eventDateStart()).isEqualTo(expectedDate(Month.JANUARY, 20));
    }

    @Test
    void parseEvent_withNullDate_returnsEmpty() {
        String html = """
            <html>
            <body>
                <div class="eventCard">
                    <h1>Event Without Date</h1>
                </div>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);
        String eventUrl = "https://carolinatheatre.org/events/event/";

        Optional<EventItem> result = parser.parseEvent(doc, eventUrl);

        assertThat(result).isEmpty();
    }

    @Test
    void parseEvent_withRelativeImageUrl_convertsToAbsolute() {
        String html = """
            <html>
            <body>
                <div class="eventCard">
                    <h1>Test Event</h1>
                    <div class="event__dateBox">
                        <span class="day">15</span>
                        <span class="month">Dec</span>
                    </div>
                    <div class="eventCard__image">
                        <img src="/images/event.jpg"/>
                    </div>
                </div>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html, "https://carolinatheatre.org/");
        String eventUrl = "https://carolinatheatre.org/events/event/";

        Optional<EventItem> result = parser.parseEvent(doc, eventUrl);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.imageUrl()).startsWith("https://");
    }

    @Test
    void parseEvent_withWhitespaceInFields_trimsWhitespace() {
        String html = """
            <html>
            <body>
                <div class="eventCard">
                    <h1>  Concert Night  </h1>
                    <div class="event__dateBox">
                        <span class="day">  15  </span>
                        <span class="month">  Dec  </span>
                    </div>
                    <p class="event__categories">  Music  </p>
                </div>
            </body>
            </html>
            """;
        Document doc = Jsoup.parse(html);
        String eventUrl = "https://carolinatheatre.org/events/event/";

        Optional<EventItem> result = parser.parseEvent(doc, eventUrl);

        assertThat(result).isPresent();
        EventItem event = result.get();
        assertThat(event.title()).isEqualTo("Concert Night");
        assertThat(event.description()).contains("Category: Music");
    }

    @BeforeEach
    void setUp() {
        parser = new EventParserImpl();
    }
}
