package carolinatheatre.events.parser.impl;

import static java.util.Objects.requireNonNull;

import carolinatheatre.events.domain.EventItem;
import carolinatheatre.events.parser.EventParser;
import java.time.LocalDate;
import java.util.Optional;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses and extracts event fields.
 */
public final class EventParserImpl implements EventParser {

    private static final Logger LOG = LoggerFactory.getLogger(
        EventParserImpl.class);
    private final DateExtractor dateExtractor;
    private final DescriptionExtractor descriptionExtractor;
    private final ImageExtractor imageExtractor;
    private final TitleExtractor titleExtractor;

    /**
     * Creates a new instance with default extractors.
     */
    public EventParserImpl() {
        this.titleExtractor = new TitleExtractor();
        this.dateExtractor = new DateExtractor();
        this.descriptionExtractor = new DescriptionExtractor();
        this.imageExtractor = new ImageExtractor();
    }

    @Override
    public Optional<EventItem> parseEvent(Document doc, String eventUrl) {
        requireNonNull(doc, "doc must not be null");
        requireNonNull(eventUrl, "eventUrl must not be null");
        LOG.info("Parsing event: {}", eventUrl);

        // Try to find event card on the detail page
        Element eventCard = doc.selectFirst("div.eventCard");
        if (eventCard == null) {
            // If no event card, try to parse from main content
            eventCard = doc.selectFirst("body");
        }

        if (eventCard == null) {
            LOG.warn("Could not find event content for: {}", eventUrl);
            return Optional.empty();
        }

        String title = titleExtractor.extract(eventCard);
        LocalDate date = dateExtractor.extractLocalDate(eventCard);
        String description = descriptionExtractor.extract(eventCard);
        String imageUrl = imageExtractor.extract(eventCard);

        if (title.isBlank() || date == null) {
            LOG.warn("Missing required fields (title or date) for: {}", eventUrl);
            return Optional.empty();
        }

        EventItem event = new EventItem(
            eventUrl, title, eventUrl, description, date, null, imageUrl, null);
        LOG.info("Successfully parsed event: {}", title);
        return Optional.of(event);
    }
}
