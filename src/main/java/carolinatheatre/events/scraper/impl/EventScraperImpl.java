package carolinatheatre.events.scraper.impl;

import static carolinatheatre.events.parser.impl.CssSelectors.CARD_SLIDER_CONTAINER;
import static carolinatheatre.events.parser.impl.CssSelectors.EVENT_CARD_WAIT;
import static java.util.Objects.requireNonNull;

import carolinatheatre.events.config.ScraperConfiguration;
import carolinatheatre.events.domain.EventItem;
import carolinatheatre.events.parser.EventParser;
import carolinatheatre.events.parser.impl.EventCardFinder;
import carolinatheatre.events.scraper.EventScraper;
import carolinatheatre.events.webdriver.PageLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scrapes events using single-page discovery.
 * Implements the unified 3-phase flow: discover, filter, parse.
 */
public record EventScraperImpl(ScraperConfiguration config, PageLoader pageLoader,
                               EventParser eventParser,
                               EventCardFinder eventCardFinder) implements EventScraper {

    private static final Logger LOG = LoggerFactory.getLogger(EventScraperImpl.class);

    public EventScraperImpl {
        requireNonNull(config, "config must not be null");
        requireNonNull(pageLoader, "pageLoader must not be null");
        requireNonNull(eventParser, "eventParser must not be null");
        requireNonNull(eventCardFinder, "eventCardFinder must not be null");
    }

    /**
     * Phase 1: Discovers all event URLs from the events page.
     * For single page, this loads the page and extracts event cards.
     *
     * @param eventElementMap Map to store URL to Element mappings for later parsing
     * @return List of discovered event URLs
     */
    private List<String> discoverEventUrls(final Map<String, Element> eventElementMap) {
        final Document eventsPage = pageLoader.loadPage(config.getBaseUrl(),
            By.cssSelector(CARD_SLIDER_CONTAINER), By.cssSelector(EVENT_CARD_WAIT));

        final List<Element> eventElements = eventCardFinder.findEventElements(eventsPage);

        final List<String> urls = new ArrayList<>();
        for (final Element element : eventElements) {
            final String url = eventCardFinder.extractEventUrl(element);
            // Multi-date events share one URL (the GUID); keep the first occurrence
            if (eventElementMap.putIfAbsent(url, element) == null) {
                urls.add(url);
            }
        }

        return urls;
    }

    /**
     * Phase 2: Filters URLs to only those not in existingGuids.
     *
     * @param urls          All discovered URLs
     * @param existingGuids Set of existing event GUIDs
     * @return Filtered list of new URLs
     */
    private List<String> filterNewUrls(final List<String> urls, final Set<String> existingGuids) {
        return urls.stream().filter(url -> !existingGuids.contains(url)).toList();
    }

    /**
     * Parses a single event and adds it to results.
     *
     * @param url     Event URL
     * @param element Event HTML element
     * @param current Current event number (1-based)
     * @param total   Total number of events
     * @param results List to add parsed event to
     */
    private void parseAndAddEvent(final String url, final Element element, final int current,
                                  final int total, final List<EventItem> results) {
        final Optional<EventItem> eventOpt =
            eventParser.parseEvent(Jsoup.parse(element.outerHtml()), url);

        if (eventOpt.isPresent()) {
            final EventItem event = eventOpt.get();
            results.add(event);
            LOG.info("Event {}/{}: {} ({})", current, total, event.title(), event.eventDateStart());
        } else {
            LOG.warn("No event returned for: {}", url);
        }
    }

    /**
     * Phase 3: Parses events from the filtered URLs.
     *
     * @param urls            URLs to parse
     * @param eventElementMap Map of URL to Element for parsing
     * @return List of successfully parsed EventItem objects
     */
    private List<EventItem> parseEvents(final List<String> urls,
                                        final Map<String, Element> eventElementMap) {
        final List<EventItem> events = new ArrayList<>();
        int current = 0;
        for (final String url : urls) {
            current++;
            try {
                parseAndAddEvent(url, eventElementMap.get(url), current, urls.size(), events);
            } catch (final Exception e) {
                LOG.error("Failed to parse event from {}: {}", url, e.getMessage(), e);
            }
        }
        return events;
    }

    @Override
    public List<EventItem> scrapeEvents(final Set<String> existingGuids) {
        requireNonNull(existingGuids, "existingGuids must not be null");

        LOG.info("Starting event scraping from: {}", config.getBaseUrl());

        try {
            // PHASE 1: Discover event URLs
            final Map<String, Element> eventElementMap = new HashMap<>();
            final List<String> allEventLinks = discoverEventUrls(eventElementMap);
            LOG.info("Phase 1 complete: Discovered {} event links", allEventLinks.size());

            // PHASE 2: Filter to new URLs only
            final List<String> newEventLinks = filterNewUrls(allEventLinks, existingGuids);
            LOG.info("Phase 2 complete: {} new events after filtering", newEventLinks.size());

            // PHASE 3: Parse each event
            final List<EventItem> events = parseEvents(newEventLinks, eventElementMap);
            LOG.info("Phase 3 complete: Parsed {} events", events.size());

            return events;
        } catch (final Exception e) {
            LOG.error("Unable to parse events", e);
            return List.of();
        }
    }
}
