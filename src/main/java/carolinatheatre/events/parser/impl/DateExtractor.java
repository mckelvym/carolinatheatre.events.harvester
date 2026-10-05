package carolinatheatre.events.parser.impl;

import static carolinatheatre.events.parser.impl.CssSelectors.DATE_BOX;
import static carolinatheatre.events.parser.impl.CssSelectors.DAY_SPAN;
import static carolinatheatre.events.parser.impl.CssSelectors.MONTH_SPAN;
import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.Month;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts event date from Carolina Theatre event cards.
 */
public final class DateExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(DateExtractor.class);
    private final DateParser dateParser;

    public DateExtractor() {
        this.dateParser = new DateParser();
    }

    /**
     * Extracts the date from an event element.
     *
     * @param eventElement Root element containing event details
     * @return Extracted LocalDate, or null if not found
     * @throws NullPointerException if element is null
     */
    public LocalDate extractLocalDate(Element eventElement) {
        requireNonNull(eventElement, "element must not be null");
        Element dateBox = eventElement.selectFirst(DATE_BOX);
        if (dateBox == null) {
            LOG.warn("Could not find event dateBox");
            return null;
        }

        Element dayElement = dateBox.selectFirst(DAY_SPAN);
        Element monthElement = dateBox.selectFirst(MONTH_SPAN);

        if (dayElement == null || monthElement == null) {
            LOG.warn("Could not find day or month in event dateBox");
            return null;
        }

        try {
            int day = Integer.parseInt(dayElement.text().trim());
            String monthText = monthElement.text().trim();
            Month month = dateParser.parseMonth(monthText);

            if (month == null) {
                LOG.warn("Could not parse month: {}", monthText);
                return null;
            }

            int year = dateParser.inferYear(month);
            return LocalDate.of(year, month, day);

        } catch (Exception e) {
            LOG.warn("Error parsing date", e);
            return null;
        }
    }
}
