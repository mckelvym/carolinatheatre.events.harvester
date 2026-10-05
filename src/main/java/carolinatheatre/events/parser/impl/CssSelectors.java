package carolinatheatre.events.parser.impl;

/**
 * Constants for CSS selectors used in HTML parsing.
 *
 * <p>This class centralizes all CSS selector strings used throughout the
 * Carolina Theatre event parser implementation to avoid magic strings and improve
 * maintainability.
 *
 * <p>Selectors are organized by their domain: event card detection, title
 * extraction, date extraction, description extraction, and image extraction.
 */
public final class CssSelectors {

    // Event card container detection
    public static final String EVENT_CARD_SELECTOR = "div.eventCard.event";
    public static final String LINK_WITH_HREF = "a[href]";

    // Title extraction selectors
    public static final String CARD_TITLE = "p.card__title";
    public static final String HEADINGS = "h1, h2, h3, h4, h5, h6";
    public static final String ANCHOR_TAG = "a";

    // Date extraction selectors
    public static final String DATE_BOX = "div.event__dateBox";
    public static final String DAY_SPAN = "span.day";
    public static final String MONTH_SPAN = "span.month";

    // Description extraction selectors
    public static final String EVENT_CATEGORIES = "p.event__categories";
    public static final String CARD_INFO = "div.card__info";
    public static final String PARAGRAPH = "p";

    // Image extraction selectors
    public static final String EVENT_CARD_IMAGE = "div.eventCard__image";
    public static final String IMG_TAG = "img";

    // Page loading selectors
    /**
     * Selector for the card slider container on events page.
     */
    public static final String CARD_SLIDER_CONTAINER = ".cardSlider";

    /**
     * Selector for event card elements (for page load wait).
     */
    public static final String EVENT_CARD_WAIT = ".eventCard";

    private CssSelectors() {
        // Utility class - prevent instantiation
    }
}
