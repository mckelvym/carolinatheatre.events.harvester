package carolinatheatre.events.parser.impl;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized date parsing utility
 */
public final class DateParser {

    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("M/d/yyyy", Locale.US),
        DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US),
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.RFC_1123_DATE_TIME
    };
    private static final Logger LOG = LoggerFactory.getLogger(DateParser.class);

    /**
     * Infers the year for a given month.
     * If the month has already passed this year, assumes next year.
     *
     * @param month the month to infer the year for
     * @return the inferred year
     */
    public int inferYear(Month month) {
        int currentYear = Year.now().getValue();
        int currentMonthValue = LocalDate.now().getMonthValue();
        return month.getValue() < currentMonthValue ? currentYear + 1 : currentYear;
    }

    /**
     * Parses a date string using multiple format strategies.
     *
     * @param dateStr the date string to parse
     * @return the parsed LocalDate, or null if parsing fails
     */
    public LocalDate parse(final String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }

        final String trimmed = dateStr.trim();

        for (final DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDate.parse(trimmed, formatter);
            } catch (final DateTimeParseException e) {
                // Try next formatter
            }
        }

        LOG.warn("Failed to parse date: {}", trimmed);
        return null;
    }

    /**
     * Parses a month name (short or full form) to a Month enum.
     *
     * @param monthText the month text to parse (e.g., "Jan", "January")
     * @return the parsed Month, or null if parsing fails
     */
    public Month parseMonth(String monthText) {
        if (monthText == null || monthText.isBlank()) {
            return null;
        }

        String trimmed = monthText.trim();
        for (Month month : Month.values()) {
            String shortName = month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            String fullName = month.getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            if (trimmed.equalsIgnoreCase(shortName) || trimmed.equalsIgnoreCase(fullName)) {
                return month;
            }
        }
        return null;
    }
}
