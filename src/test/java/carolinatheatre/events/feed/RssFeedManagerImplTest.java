package carolinatheatre.events.feed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import carolinatheatre.events.config.ScraperConfiguration;
import carolinatheatre.events.domain.EventItem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Tests for RssFeedManagerImpl.
 */
class RssFeedManagerImplTest {

    private final XmlSecurityConfigurer securityConfigurer = new XmlSecurityConfigurer();
    private Path existingFeedFile;
    private Path feedFile;
    private RssFeedManagerImpl feedManager;
    private Path tempDir;

    private void addElement(
        final Document doc,
        final Element parent,
        final String name,
        final String value) {
        final Element element = doc.createElement(name);
        element.setTextContent(value);
        parent.appendChild(element);
    }

    private void createExistingFeed(final String pubDate)
        throws Exception {
        final DocumentBuilderFactory factory =
            securityConfigurer.createSecureDocumentBuilderFactory();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.newDocument();

        final Element rss = doc.createElement("rss");
        rss.setAttribute("version", "2.0");
        doc.appendChild(rss);

        final Element channel = doc.createElement("channel");
        rss.appendChild(channel);

        addElement(doc, channel, "title", "Test Feed");
        addElement(doc, channel, "link", "https://carolinatheatre.org/events/");
        addElement(doc, channel, "description", "Test Description");

        final Element item = doc.createElement("item");
        addElement(doc, item, "title", "Existing Event (2024-01-01)");
        addElement(doc, item, "link", "https://carolinatheatre.org/existing-event/");
        addElement(doc, item, "description", "Existing event description");
        addElement(doc, item, "pubDate", pubDate);

        final Element guid = doc.createElement("guid");
        guid.setAttribute("isPermaLink", "true");
        guid.setTextContent("https://carolinatheatre.org/existing-event/");
        item.appendChild(guid);

        channel.appendChild(item);

        final javax.xml.transform.TransformerFactory transformerFactory =
            securityConfigurer.createSecureTransformerFactory();
        final javax.xml.transform.Transformer transformer =
            transformerFactory.newTransformer();
        transformer.setOutputProperty(
            javax.xml.transform.OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(
            javax.xml.transform.OutputKeys.ENCODING, "UTF-8");

        final javax.xml.transform.dom.DOMSource source =
            new javax.xml.transform.dom.DOMSource(doc);
        final javax.xml.transform.stream.StreamResult result =
            new javax.xml.transform.stream.StreamResult(
                existingFeedFile.toFile());
        transformer.transform(source, result);
    }

    private String getElementText(final Element parent, final String tagName) {
        final NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return "";
    }

    @BeforeEach
    void setUp()
        throws Exception {
        final ScraperConfiguration config = new ScraperConfiguration() {
            @Override
            public String getBaseUrl() {
                return "https://carolinatheatre.org/events/";
            }

            @Override
            public String getFeedDescription() {
                return "Feed";
            }

            @Override
            public String getFeedLink() {
                return getBaseUrl();
            }

            @Override
            public String getFeedTitle() {
                return "Title";
            }

            @Override
            public Duration getPageLoadTimeout() {
                return Duration.ofSeconds(10);
            }

            @Override
            public int getRetentionDays() {
                return 7;
            }

            @Override
            public String getUserAgent() {
                return "Test User Agent";
            }
        };

        feedManager = new RssFeedManagerImpl(config);
        tempDir = Files.createTempDirectory("rss-test");
        feedFile = tempDir.resolve("events.xml");
        existingFeedFile = tempDir.resolve("existing.xml");
    }

    @AfterEach
    void tearDown()
        throws Exception {
        Files.deleteIfExists(feedFile);
        Files.deleteIfExists(existingFeedFile);
        Files.deleteIfExists(tempDir);
    }

    @Test
    void testPubDatePreservation()
        throws Exception {
        // Create an existing feed with a specific pubDate
        // Recent pubDate so the legacy item (no ev:startdate) is within retention
        final String existingPubDate = ZonedDateTime.now(ZoneOffset.UTC).withNano(0)
            .format(DateTimeFormatter.RFC_1123_DATE_TIME);
        createExistingFeed(existingPubDate);

        // Load existing GUIDs
        final Set<String> existingGuids =
            feedManager.loadExistingGuids(existingFeedFile.toString());
        assertEquals(1, existingGuids.size());

        // Create a new event with a different GUID
        final List<EventItem> newEvents = new ArrayList<>();
        final EventItem newEvent = new EventItem(
            "https://carolinatheatre.org/new-event/",
            "New Event Title",
            "https://carolinatheatre.org/new-event/",
            "New event description",
            LocalDate.now(),
            null,
            "https://example.com/new.jpg",
            null
        );
        newEvents.add(newEvent);

        // Generate feed merging new and existing events
        feedManager.generateFeed(
            feedFile.toString(),
            newEvents,
            existingFeedFile.toString()
        );

        // Verify the feed was created
        assertTrue(Files.exists(feedFile));

        // Parse the generated feed and verify pubDate preservation
        final DocumentBuilderFactory factory =
            securityConfigurer.createSecureDocumentBuilderFactory();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.parse(feedFile.toFile());

        final NodeList items = doc.getElementsByTagName("item");
        assertEquals(2, items.getLength(), "Should have 2 items (1 new + 1 existing)");

        // Find the existing event item and verify its pubDate is preserved
        boolean foundExistingEvent = false;
        for (int i = 0; i < items.getLength(); i++) {
            final Element item = (Element) items.item(i);
            final String guid = getElementText(item, "guid");

            if ("https://carolinatheatre.org/existing-event/".equals(guid)) {
                final String pubDate = getElementText(item, "pubDate");
                assertEquals(existingPubDate, pubDate,
                    "Existing event pubDate should be preserved");
                foundExistingEvent = true;
                break;
            }
        }

        assertTrue(foundExistingEvent, "Should find the existing event in generated feed");
    }
}
