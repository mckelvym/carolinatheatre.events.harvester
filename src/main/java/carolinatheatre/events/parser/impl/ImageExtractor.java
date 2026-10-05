package carolinatheatre.events.parser.impl;

import static carolinatheatre.events.parser.impl.CssSelectors.EVENT_CARD_IMAGE;
import static carolinatheatre.events.parser.impl.CssSelectors.IMG_TAG;
import static carolinatheatre.events.parser.impl.HtmlConstants.SRC_ATTR;
import static java.util.Objects.requireNonNull;

import org.jsoup.nodes.Element;

/**
 * Extracts event image URL from Carolina Theatre event cards.
 * Looks for images in eventCard__image container.
 */
public final class ImageExtractor {
    /**
     * Extracts the image URL from an event element.
     * Looks for img tag in eventCard__image container.
     *
     * @param element Root element containing event details
     * @return Extracted image URL, or null if not found
     * @throws NullPointerException if element is null
     */
    public String extract(Element element) {
        requireNonNull(element, "element must not be null");
        Element imageContainer = element.selectFirst(EVENT_CARD_IMAGE);
        if (imageContainer == null) {
            return null;
        }

        Element img = imageContainer.selectFirst(IMG_TAG);
        if (img == null) {
            return null;
        }

        String imageUrl = img.absUrl(SRC_ATTR);
        if (imageUrl.isBlank()) {
            imageUrl = img.attr(SRC_ATTR);
        }

        return imageUrl.isBlank() ? null : imageUrl;
    }
}
