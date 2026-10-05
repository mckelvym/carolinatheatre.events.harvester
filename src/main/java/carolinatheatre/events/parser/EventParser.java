package carolinatheatre.events.parser;

import carolinatheatre.events.domain.EventItem;
import java.util.Optional;
import org.jsoup.nodes.Document;

/**
 * Interface for parsing event information from HTML elements.
 */
public interface EventParser {

    /**
     * Parses an event from the given HTML document.
     *
     * @param doc      JSoup document containing event page
     * @param eventUrl URL of the event page
     * @return Optional containing parsed EventItem, or empty if parsing fails
     */
    Optional<EventItem> parseEvent(Document doc, String eventUrl);
}
