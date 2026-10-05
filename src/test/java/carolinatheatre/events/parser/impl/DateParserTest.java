package carolinatheatre.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for DateParser.
 * Verifies date parsing with multiple format strategies, month parsing, and year inference.
 */
class DateParserTest {

    private DateParser parser;

    @Test
    void inferYear_withCurrentMonth_returnsCurrentYear() {
        Month currentMonth = LocalDate.now().getMonth();
        int currentYear = Year.now().getValue();

        int result = parser.inferYear(currentMonth);

        assertThat(result).isEqualTo(currentYear);
    }

    // Tests for parse() method

    @Test
    void inferYear_withDecember_behavesCorrectly() {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = Year.now().getValue();

        // If current month is before December, it returns current year
        // If current month is December, it returns current year
        int expectedYear = currentYear;

        int result = parser.inferYear(Month.DECEMBER);

        assertThat(result).isEqualTo(expectedYear);
    }

    @Test
    void inferYear_withFutureMonth_returnsCurrentYear() {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = Year.now().getValue();

        // Find a month that is in the future
        Month futureMonth = Month.of(currentMonth == 12 ? 1 : currentMonth + 1);
        int expectedYear = currentMonth == 12 ? currentYear + 1 : currentYear;

        int result = parser.inferYear(futureMonth);

        assertThat(result).isEqualTo(expectedYear);
    }

    @Test
    void inferYear_withJanuary_behavesCorrectly() {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = Year.now().getValue();

        // If current month is January, it returns current year
        // If current month is after January, it returns next year
        int expectedYear = currentMonth == 1 ? currentYear : currentYear + 1;

        int result = parser.inferYear(Month.JANUARY);

        assertThat(result).isEqualTo(expectedYear);
    }

    @Test
    void inferYear_withPastMonth_returnsNextYear() {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = Year.now().getValue();

        // Find a month that is in the past
        Month pastMonth = Month.of(currentMonth == 1 ? 12 : currentMonth - 1);
        int expectedYear = currentMonth == 1 ? currentYear : currentYear + 1;

        int result = parser.inferYear(pastMonth);

        assertThat(result).isEqualTo(expectedYear);
    }

    @Test
    void parseMonth_withBlankString_returnsNull() {
        Month result = parser.parseMonth("   ");

        assertThat(result).isNull();
    }

    @Test
    void parseMonth_withDecemberFull_returnsMonth() {
        Month result = parser.parseMonth("December");

        assertThat(result).isEqualTo(Month.DECEMBER);
    }

    @Test
    void parseMonth_withDecemberShort_returnsMonth() {
        Month result = parser.parseMonth("Dec");

        assertThat(result).isEqualTo(Month.DECEMBER);
    }

    @Test
    void parseMonth_withEmptyString_returnsNull() {
        Month result = parser.parseMonth("");

        assertThat(result).isNull();
    }

    @Test
    void parseMonth_withFullName_returnsMonth() {
        Month result = parser.parseMonth("January");

        assertThat(result).isEqualTo(Month.JANUARY);
    }

    @Test
    void parseMonth_withInvalidName_returnsNull() {
        Month result = parser.parseMonth("NotAMonth");

        assertThat(result).isNull();
    }

    @Test
    void parseMonth_withLowercase_returnsMonth() {
        Month result = parser.parseMonth("february");

        assertThat(result).isEqualTo(Month.FEBRUARY);
    }

    @Test
    void parseMonth_withMixedCase_returnsMonth() {
        Month result = parser.parseMonth("aPrIL");

        assertThat(result).isEqualTo(Month.APRIL);
    }

    @Test
    void parseMonth_withNull_returnsNull() {
        Month result = parser.parseMonth(null);

        assertThat(result).isNull();
    }

    @Test
    void parseMonth_withNumber_returnsNull() {
        Month result = parser.parseMonth("12");

        assertThat(result).isNull();
    }

    @Test
    void parseMonth_withPartialName_returnsNull() {
        Month result = parser.parseMonth("Janu");

        assertThat(result).isNull();
    }

    @Test
    void parseMonth_withShortName_returnsMonth() {
        Month result = parser.parseMonth("Jan");

        assertThat(result).isEqualTo(Month.JANUARY);
    }

    // Tests for parseMonth() method

    @Test
    void parseMonth_withUppercase_returnsMonth() {
        Month result = parser.parseMonth("MARCH");

        assertThat(result).isEqualTo(Month.MARCH);
    }

    @Test
    void parseMonth_withWhitespace_trimsAndParses() {
        Month result = parser.parseMonth("  May  ");

        assertThat(result).isEqualTo(Month.MAY);
    }

    @Test
    void parse_withAbbreviatedMonthFormat_returnsLocalDate() {
        LocalDate result = parser.parse("Dec 15, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withAbbreviatedMonthTwoDigitDay_returnsLocalDate() {
        LocalDate result = parser.parse("Dec 05, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 5));
    }

    @Test
    void parse_withBlankString_returnsNull() {
        LocalDate result = parser.parse("   ");

        assertThat(result).isNull();
    }

    @Test
    void parse_withEmptyString_returnsNull() {
        LocalDate result = parser.parse("");

        assertThat(result).isNull();
    }

    @Test
    void parse_withFullMonthFormat_returnsLocalDate() {
        LocalDate result = parser.parse("December 15, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withFullMonthTwoDigitDay_returnsLocalDate() {
        LocalDate result = parser.parse("December 05, 2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 5));
    }

    @Test
    void parse_withInvalidDay_returnsNull() {
        LocalDate result = parser.parse("December 32, 2025");

        assertThat(result).isNull();
    }

    @Test
    void parse_withInvalidFormat_returnsNull() {
        LocalDate result = parser.parse("not a date");

        assertThat(result).isNull();
    }

    @Test
    void parse_withInvalidMonth_returnsNull() {
        LocalDate result = parser.parse("InvalidMonth 15, 2025");

        assertThat(result).isNull();
    }

    @Test
    void parse_withIsoLocalDateFormat_returnsLocalDate() {
        LocalDate result = parser.parse("2025-12-15");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withNull_returnsNull() {
        LocalDate result = parser.parse(null);

        assertThat(result).isNull();
    }

    @Test
    void parse_withPartialDate_returnsNull() {
        LocalDate result = parser.parse("December 2025");

        assertThat(result).isNull();
    }

    // Tests for inferYear() method

    @Test
    void parse_withRfc1123Format_returnsLocalDate() {
        LocalDate result = parser.parse("Mon, 15 Dec 2025 10:00:00 GMT");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withSingleDigitSlashFormat_returnsLocalDate() {
        LocalDate result = parser.parse("1/5/2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 1, 5));
    }

    @Test
    void parse_withTwoDigitSlashFormat_returnsLocalDate() {
        LocalDate result = parser.parse("12/15/2025");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void parse_withWhitespace_trimsAndParses() {
        LocalDate result = parser.parse("  December 15, 2025  ");

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @BeforeEach
    void setUp() {
        parser = new DateParser();
    }
}
