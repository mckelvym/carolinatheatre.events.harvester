package carolinatheatre.events.parser.impl;

import static carolinatheatre.events.parser.impl.CssSelectors.ANCHOR_TAG;
import static carolinatheatre.events.parser.impl.CssSelectors.CARD_TITLE;
import static carolinatheatre.events.parser.impl.CssSelectors.HEADINGS;
import static carolinatheatre.events.parser.impl.HtmlConstants.EMPTY;
import static java.util.Objects.requireNonNull;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts event title using multiple fallback strategies.
 * Tries various CSS selectors to find the title.
 */
public final class TitleExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(TitleExtractor.class);

    /**
     * Extracts the title from an event element.
     * Tries multiple strategies in order:
     * 1. card__title class
     * 2. Headings (h1-h6)
     * 3. First link text
     *
     * @param element Root element containing event details
     * @return Extracted title, or empty string if not found
     * @throws NullPointerException if element is null
     */
    public String extract(Element element) {
        requireNonNull(element, "element must not be null");
        // Strategy 1: Look for card__title
        Element titleElement = element.selectFirst(CARD_TITLE);
        if (titleElement != null && !titleElement.text().isBlank()) {
            return titleElement.text().trim();
        }

        // Strategy 2: Look for any heading
        Elements headings = element.select(HEADINGS);
        if (!headings.isEmpty()) {
            return requireNonNull(headings.first()).text().trim();
        }

        // Strategy 3: Look for first link text
        Element link = element.selectFirst(ANCHOR_TAG);
        if (link != null && !link.text().isBlank()) {
            return link.text().trim();
        }

        LOG.warn("Could not extract title from element");
        return EMPTY;
    }
}
