package carolinatheatre.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.Month;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for DateExtractor.
 * Tests date extraction from day/month structure with year inference.
 */
class DateExtractorTest {

    private DateExtractor extractor;

    @Test
    void extract_LocalDate_withAllMonths_parsesCorrectly() {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        for (int i = 0; i < months.length; i++) {
            String html = String.format("""
                <div>
                    <div class="event__dateBox">
                        <span class="day">1</span>
                        <span class="month">%s</span>
                    </div>
                </div>
                """, months[i]);
            Element element = Jsoup.parse(html).body();

            LocalDate result = extractor.extractLocalDate(element);

            assertThat(result).isNotNull();
            assertThat(result.getMonth()).isEqualTo(Month.values()[i]);
        }
    }

    @Test
    void extract_LocalDate_withDay32_returnsNull() {
        String html = """
            <div>
                <div class="event__dateBox">
                    <span class="day">32</span>
                    <span class="month">Dec</span>
                </div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isNull();
    }

    @Test
    void extract_LocalDate_withDayZero_returnsNull() {
        String html = """
            <div>
                <div class="event__dateBox">
                    <span class="day">0</span>
                    <span class="month">Dec</span>
                </div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isNull();
    }

    @Test
    void extract_LocalDate_withEmptyDay_returnsNull() {
        String html = """
            <div>
                <div class="event__dateBox">
                    <span class="day"></span>
                    <span class="month">Dec</span>
                </div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isNull();
    }

    @Test
    void extract_LocalDate_withEmptyMonth_returnsNull() {
        String html = """
            <div>
                <div class="event__dateBox">
                    <span class="day">15</span>
                    <span class="month"></span>
                </div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isNull();
    }

    @Test
    void extract_LocalDate_withFullMonthName_parsesCorrectly() {
        String html = """
            <div>
                <div class="event__dateBox">
                    <span class="day">25</span>
                    <span class="month">December</span>
                </div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isNotNull();
        assertThat(result.getDayOfMonth()).isEqualTo(25);
        assertThat(result.getMonth()).isEqualTo(Month.DECEMBER);
    }

    @Test
    void extract_LocalDate_withInvalidDay_returnsNull() {
        String html = """
            <div>
                <div class="event__dateBox">
                    <span class="day">invalid</span>
                    <span class="month">Dec</span>
                </div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isNull();
    }

    @Test
    void extract_LocalDate_withInvalidMonth_returnsNull() {
        String html = """
            <div>
                <div class="event__dateBox">
                    <span class="day">15</span>
                    <span class="month">InvalidMonth</span>
                </div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isNull();
    }

    @Test
    void extract_LocalDate_withMissingDaySpan_returnsNull() {
        String html = """
            <div>
                <div class="event__dateBox">
                    <span class="month">Dec</span>
                </div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isNull();
    }

    @Test
    void extract_LocalDate_withMissingMonthSpan_returnsNull() {
        String html = """
            <div>
                <div class="event__dateBox">
                    <span class="day">15</span>
                </div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isNull();
    }

    @Test
    void extract_LocalDate_withMixedCaseMonth_parsesCorrectly() {
        String html = """
            <div>
                <div class="event__dateBox">
                    <span class="day">5</span>
                    <span class="month">FeBrUaRy</span>
                </div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isNotNull();
        assertThat(result.getMonth()).isEqualTo(Month.FEBRUARY);
    }

    @Test
    void extract_LocalDate_withMultipleDateBoxes_usesFirst() {
        String html = """
            <div>
                <div class="event__dateBox">
                    <span class="day">15</span>
                    <span class="month">Dec</span>
                </div>
                <div class="event__dateBox">
                    <span class="day">20</span>
                    <span class="month">Jan</span>
                </div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isNotNull();
        assertThat(result.getDayOfMonth()).isEqualTo(15);
        assertThat(result.getMonth()).isEqualTo(Month.DECEMBER);
    }

    @Test
    void extract_LocalDate_withNestedDateBox_findsCorrectly() {
        String html = """
            <div>
                <div class="outer">
                    <div class="event__dateBox">
                        <span class="day">20</span>
                        <span class="month">Mar</span>
                    </div>
                </div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isNotNull();
        assertThat(result.getDayOfMonth()).isEqualTo(20);
        assertThat(result.getMonth()).isEqualTo(Month.MARCH);
    }

    @Test
    void extract_LocalDate_withNoDateBox_returnsNull() {
        String html = "<div><p>No date box here</p></div>";
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isNull();
    }

    @Test
    void extract_LocalDate_withNullElement_throwsNullPointerException() {
        assertThatThrownBy(() -> extractor.extractLocalDate(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("element must not be null");
    }

    @Test
    void extract_LocalDate_withPastMonth_usesNextYear() {
        // This test assumes we're not in January
        LocalDate now = LocalDate.now();
        if (now.getMonthValue() > 1) {
            String html = """
                <div>
                    <div class="event__dateBox">
                        <span class="day">15</span>
                        <span class="month">Jan</span>
                    </div>
                </div>
                """;
            Element element = Jsoup.parse(html).body();

            LocalDate result = extractor.extractLocalDate(element);

            assertThat(result).isNotNull();
            assertThat(result.getYear()).isEqualTo(now.getYear() + 1);
        }
    }

    @Test
    void extract_LocalDate_withShortMonthName_parsesCorrectly() {
        String html = """
            <div>
                <div class="event__dateBox">
                    <span class="day">10</span>
                    <span class="month">Jan</span>
                </div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isNotNull();
        assertThat(result.getMonth()).isEqualTo(Month.JANUARY);
    }

    @Test
    void extract_LocalDate_withValidDayAndMonth_returnsLocalDate() {
        String html = """
            <div>
                <div class="event__dateBox">
                    <span class="day">15</span>
                    <span class="month">Dec</span>
                </div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isNotNull();
        assertThat(result.getDayOfMonth()).isEqualTo(15);
        assertThat(result.getMonth()).isEqualTo(Month.DECEMBER);
    }

    @Test
    void extract_LocalDate_withWhitespaceInDay_trimsAndParses() {
        String html = """
            <div>
                <div class="event__dateBox">
                    <span class="day">  15  </span>
                    <span class="month">Dec</span>
                </div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isNotNull();
        assertThat(result.getDayOfMonth()).isEqualTo(15);
    }

    @Test
    void extract_LocalDate_withWhitespaceInMonth_trimsAndParses() {
        String html = """
            <div>
                <div class="event__dateBox">
                    <span class="day">15</span>
                    <span class="month">  Dec  </span>
                </div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isNotNull();
        assertThat(result.getMonth()).isEqualTo(Month.DECEMBER);
    }

    @Test
    void extract_LocalDate_withYearInference_usesCurrentYear() {
        String html = """
            <div>
                <div class="event__dateBox">
                    <span class="day">15</span>
                    <span class="month">Dec</span>
                </div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractLocalDate(element);

        assertThat(result).isNotNull();
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        // If December has passed this year, expect next year
        if (Month.DECEMBER.getValue() < currentMonth) {
            assertThat(result.getYear()).isEqualTo(currentYear + 1);
        } else {
            assertThat(result.getYear()).isEqualTo(currentYear);
        }
    }

    @BeforeEach
    void setUp() {
        extractor = new DateExtractor();
    }
}
