package carolinatheatre.events.parser.impl;

import static carolinatheatre.events.parser.impl.CssSelectors.EVENT_CARD_SELECTOR;
import static carolinatheatre.events.parser.impl.CssSelectors.LINK_WITH_HREF;
import static carolinatheatre.events.parser.impl.HtmlConstants.EMPTY;
import static carolinatheatre.events.parser.impl.HtmlConstants.HREF_ATTR;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Finds event cards in the Carolina Theatre events page.
 * Extracts links to individual event pages.
 */
public final class EventCardFinder {

    // URL validation constants
    private static final String BASE_URL = "https://carolinatheatre.org/";
    private static final String EVENTS_PATH = "/events/";
    private static final Logger LOG = LoggerFactory.getLogger(EventCardFinder.class);

    /**
     * Extracts the event URL from an event card.
     *
     * @param card Event card element
     * @return Event URL, or empty string if not found
     */
    public String extractEventUrl(Element card) {
        Element link = card.selectFirst(LINK_WITH_HREF);
        return (link != null) ? link.absUrl(HREF_ATTR) : EMPTY;
    }

    /**
     * Finds all event cards on the page.
     *
     * @param doc JSoup document to search
     * @return List of event card Elements
     */
    public List<Element> findEventElements(Document doc) {
        Elements eventCards = doc.select(EVENT_CARD_SELECTOR);
        LOG.info("Found {} event cards", eventCards.size());

        List<Element> validCards = new ArrayList<>();
        for (Element card : eventCards) {
            Element link = card.selectFirst(LINK_WITH_HREF);
            if (link != null) {
                String href = link.absUrl(HREF_ATTR);
                if (!href.isBlank() && isValidEventUrl(href)) {
                    validCards.add(card);
                }
            }
        }

        return validCards;
    }

    private boolean isValidEventUrl(String url) {
        // Carolina Theatre event URLs are typically: https://carolinatheatre.org/[slug]/
        return url.startsWith(BASE_URL)
            && !url.equals(BASE_URL)
            && !url.contains(EVENTS_PATH);
    }
}
