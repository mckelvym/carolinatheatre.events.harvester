package carolinatheatre.events;

import carolinatheatre.events.config.ScraperConfiguration;
import carolinatheatre.events.config.impl.ScraperConfigurationImpl;
import carolinatheatre.events.domain.EventItem;
import carolinatheatre.events.feed.RssFeedManager;
import carolinatheatre.events.feed.RssFeedManagerImpl;
import carolinatheatre.events.parser.EventParser;
import carolinatheatre.events.parser.impl.EventCardFinder;
import carolinatheatre.events.parser.impl.EventParserImpl;
import carolinatheatre.events.scraper.EventScraper;
import carolinatheatre.events.scraper.impl.EventScraperImpl;
import carolinatheatre.events.webdriver.ChromeDriverManager;
import carolinatheatre.events.webdriver.PageLoader;
import carolinatheatre.events.webdriver.WebDriverManager;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Main application for scraping and generating RSS feed.
 */
public final class EventsHarvesterApplication {

    private static final String DEFAULT_OUTPUT_FILE = "events.xml";
    private static final Logger LOG = LoggerFactory.getLogger(EventsHarvesterApplication.class);

    private EventsHarvesterApplication() {
        // utility
    }

    private static void configureLogging() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    /**
     * Main entry point.
     *
     * @param args Command line arguments (optional output file path)
     */
    public static void main(String[] args) {
        configureLogging();

        String outputFile = args.length > 0 ? args[0] : DEFAULT_OUTPUT_FILE;
        LOG.info("Starting Carolina Theatre Events Harvester");
        LOG.info("Output file: {}", outputFile);

        try {
            new EventsHarvesterApplication().run(outputFile);
            LOG.info("Harvesting completed successfully");
        } catch (Exception e) {
            LOG.error("Application failed", e);
            System.exit(1);
        }
    }

    private EventScraper createEventScraper(ScraperConfiguration config, PageLoader pageLoader) {
        EventParser eventParser = new EventParserImpl();

        return new EventScraperImpl(config, pageLoader, eventParser, new EventCardFinder());
    }

    /**
     * Runs the scraping workflow.
     *
     * @param outputFile Output RSS file path
     * @throws Exception If any step fails
     */
    public void run(String outputFile)
        throws Exception {
        // Phase 1: Initialize components
        ScraperConfiguration config = new ScraperConfigurationImpl();
        RssFeedManager feedManager = new RssFeedManagerImpl(config);

        // Phase 2: Load existing GUIDs
        LOG.info("Loading existing feed");
        Set<String> existingGuids = feedManager.loadExistingGuids(outputFile);
        LOG.info("Found {} existing events", existingGuids.size());

        // Phase 3: Scrape new events
        LOG.info("Starting event scraping");
        List<EventItem> newEvents;

        try (WebDriverManager driverManager = new ChromeDriverManager(config)) {
            PageLoader pageLoader = new PageLoader(driverManager.getDriver(),
                config.getPageLoadTimeout());
            EventScraper scraper = createEventScraper(config, pageLoader);
            newEvents = scraper.scrapeEvents(existingGuids);
        }

        LOG.info("Scraped {} new events", newEvents.size());

        // Phase 4: Generate RSS feed
        LOG.info("Generating RSS feed");
        feedManager.generateFeed(outputFile, newEvents, outputFile);

        LOG.info("RSS feed generation complete");
        LOG.info("Total events in feed: {}", existingGuids.size() + newEvents.size());
    }
}
