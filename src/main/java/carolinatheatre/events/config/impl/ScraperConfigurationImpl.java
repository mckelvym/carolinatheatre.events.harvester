package carolinatheatre.events.config.impl;

import carolinatheatre.events.config.ScraperConfiguration;
import java.time.Duration;

/**
 * Configuration implementation for Carolina Theatre event scraping.
 * Provides URLs and parameters specific to carolinatheatre.org.
 */
public class ScraperConfigurationImpl implements ScraperConfiguration {

    private static final String BASE_URL = "https://carolinatheatre.org/events/";
    private static final String FEED_DESCRIPTION =
        "Events and performances at Carolina Theatre of Durham";
    private static final String FEED_TITLE = "Carolina Theatre of Durham Events";
    private static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(10);
    private static final int RETENTION_DAYS = 7;
    private static final String USER_AGENT =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) "
            + "Chrome/142.0.0.0 Safari/537.36";

    @Override
    public String getBaseUrl() {
        return BASE_URL;
    }

    @Override
    public String getFeedDescription() {
        return FEED_DESCRIPTION;
    }

    @Override
    public String getFeedLink() {
        return getBaseUrl();
    }

    @Override
    public String getFeedTitle() {
        return FEED_TITLE;
    }

    @Override
    public Duration getPageLoadTimeout() {
        return PAGE_LOAD_TIMEOUT;
    }

    @Override
    public int getRetentionDays() {
        return RETENTION_DAYS;
    }

    @Override
    public String getUserAgent() {
        return USER_AGENT;
    }
}
