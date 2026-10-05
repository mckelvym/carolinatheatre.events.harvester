package carolinatheatre.events.config;

import java.time.Duration;

/**
 * Configuration interface for event scraping operations.
 * Provides site-specific URLs, timeouts, and filtering parameters.
 */
public interface ScraperConfiguration {

    /**
     * Returns the base URL of the events page to scrape.
     *
     * @return Base events URL
     */
    String getBaseUrl();

    /**
     * Gets the RSS feed description.
     *
     * @return the feed description
     */
    String getFeedDescription();

    /**
     * Gets the RSS feed link.
     *
     * @return the feed link
     */
    String getFeedLink();

    /**
     * Gets the RSS feed title.
     *
     * @return the feed title
     */
    String getFeedTitle();

    /**
     * Gets the timeout duration for page loads.
     *
     * @return the timeout duration
     */
    Duration getPageLoadTimeout();

    /**
     * Returns the number of days to retain events before filtering them out.
     *
     * @return Days to keep events
     */
    int getRetentionDays();

    /**
     * Returns the user agent string for HTTP requests.
     *
     * @return User agent string
     */
    String getUserAgent();
}
