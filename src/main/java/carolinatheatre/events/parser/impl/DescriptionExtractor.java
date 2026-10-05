package carolinatheatre.events.parser.impl;

import static carolinatheatre.events.parser.impl.CssSelectors.CARD_INFO;
import static carolinatheatre.events.parser.impl.CssSelectors.EVENT_CATEGORIES;
import static carolinatheatre.events.parser.impl.CssSelectors.PARAGRAPH;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Extracts event description using multiple strategies.
 * Builds description from category, time, and venue information.
 */
public final class DescriptionExtractor {

    // Description formatting constants
    private static final String CATEGORY_PREFIX = "Category: ";
    private static final String DEFAULT_DESCRIPTION = "Event at Carolina Theatre of Durham";
    private static final String PIPE_SEPARATOR = " | ";

    /**
     * Extracts the description from an event element.
     * Combines category, time, and venue information.
     *
     * @param element Root element containing event details
     * @return Extracted description
     * @throws NullPointerException if element is null
     */
    public String extract(Element element) {
        requireNonNull(element, "element must not be null");
        List<String> parts = new ArrayList<>();

        // Extract category
        Element categoryElement = element.selectFirst(EVENT_CATEGORIES);
        if (categoryElement != null && !categoryElement.text().isBlank()) {
            parts.add(CATEGORY_PREFIX + categoryElement.text().trim());
        }

        // Extract card info (time and venue)
        Element cardInfo = element.selectFirst(CARD_INFO);
        if (cardInfo != null) {
            Elements infoParagraphs = cardInfo.select(PARAGRAPH);
            for (Element p : infoParagraphs) {
                String text = p.text().trim();
                if (!text.isBlank()) {
                    parts.add(text);
                }
            }
        }

        String description = String.join(PIPE_SEPARATOR, parts);
        return description.isBlank() ? DEFAULT_DESCRIPTION : description;
    }
}
