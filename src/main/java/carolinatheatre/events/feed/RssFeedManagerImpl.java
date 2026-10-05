package carolinatheatre.events.feed;

import static carolinatheatre.events.feed.RssElementNames.CHANNEL;
import static carolinatheatre.events.feed.RssElementNames.DESCRIPTION;
import static carolinatheatre.events.feed.RssElementNames.ENCLOSURE;
import static carolinatheatre.events.feed.RssElementNames.ENCODING_UTF8;
import static carolinatheatre.events.feed.RssElementNames.EVENT_NAMESPACE_URI;
import static carolinatheatre.events.feed.RssElementNames.EV_ENDDATE;
import static carolinatheatre.events.feed.RssElementNames.EV_STARTDATE;
import static carolinatheatre.events.feed.RssElementNames.GUID;
import static carolinatheatre.events.feed.RssElementNames.IMAGE_JPEG_TYPE;
import static carolinatheatre.events.feed.RssElementNames.INDENT_AMOUNT;
import static carolinatheatre.events.feed.RssElementNames.IS_PERMALINK_ATTR;
import static carolinatheatre.events.feed.RssElementNames.ITEM;
import static carolinatheatre.events.feed.RssElementNames.LANGUAGE;
import static carolinatheatre.events.feed.RssElementNames.LANGUAGE_VALUE;
import static carolinatheatre.events.feed.RssElementNames.LAST_BUILD_DATE;
import static carolinatheatre.events.feed.RssElementNames.LINK;
import static carolinatheatre.events.feed.RssElementNames.PUB_DATE;
import static carolinatheatre.events.feed.RssElementNames.RSS;
import static carolinatheatre.events.feed.RssElementNames.RSS_VERSION;
import static carolinatheatre.events.feed.RssElementNames.TITLE;
import static carolinatheatre.events.feed.RssElementNames.TRUE_VALUE;
import static carolinatheatre.events.feed.RssElementNames.TYPE_ATTR;
import static carolinatheatre.events.feed.RssElementNames.URL_ATTR;
import static carolinatheatre.events.feed.RssElementNames.VERSION_ATTR;
import static carolinatheatre.events.feed.RssElementNames.XMLNS_EV_ATTR;
import static carolinatheatre.events.feed.RssElementNames.XSLT_INDENT_PROPERTY;
import static java.util.Objects.requireNonNull;

import carolinatheatre.events.config.ScraperConfiguration;
import carolinatheatre.events.domain.EventItem;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * RSS feed manager implementation.
 * Handles reading existing feeds and generating new RSS 2.0 XML.
 */
public class RssFeedManagerImpl implements RssFeedManager {

    private static final Logger LOG = LoggerFactory.getLogger(RssFeedManagerImpl.class);
    private final ScraperConfiguration config;
    private final EventFilter eventFilter;
    private final XmlSecurityConfigurer securityConfigurer;

    /**
     * Creates a new RssFeedManagerImpl.
     *
     * @param config Scraper configuration
     * @throws NullPointerException if config is null
     */
    public RssFeedManagerImpl(ScraperConfiguration config) {
        this.config = requireNonNull(config, "config must not be null");
        this.eventFilter = new EventFilter(config);
        this.securityConfigurer = new XmlSecurityConfigurer();
    }

    private void addChannelMetadata(Document doc, Element channel) {
        addElement(doc, channel, TITLE, config.getFeedTitle());
        addElement(doc, channel, LINK, config.getFeedLink());
        addElement(doc, channel, DESCRIPTION, config.getFeedDescription());
        addElement(doc, channel, LANGUAGE, LANGUAGE_VALUE);
        addElement(doc, channel, LAST_BUILD_DATE,
            ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
    }

    /**
     * Adds a description element wrapped in CDATA, omitting it when empty.
     *
     * @param doc         the XML document
     * @param item        the item element to add to
     * @param description the description HTML or text
     */
    private void addDescriptionElement(Document doc, Element item,
                                       String description) {
        if (description.isEmpty()) {
            return;
        }
        Element element = doc.createElement(DESCRIPTION);
        element.appendChild(doc.createCDATASection(description));
        item.appendChild(element);
    }

    private void addElement(Document doc, Element parent, String name, String value) {
        Element element = doc.createElement(name);
        element.setTextContent(value);
        parent.appendChild(element);
    }

    /**
     * Adds the machine-readable event dates (RSS Event module) used for retention.
     *
     * @param doc   the XML document
     * @param item  the item element to add to
     * @param event the event whose dates to add
     */
    private void addEventDateElements(Document doc, Element item,
                                      EventItem event) {
        Element startDate = doc.createElement(EV_STARTDATE);
        startDate.setTextContent(event.eventDateStart().toString());
        item.appendChild(startDate);
        if (event.eventDateEnd() != null) {
            Element endDate = doc.createElement(EV_ENDDATE);
            endDate.setTextContent(event.eventDateEnd().toString());
            item.appendChild(endDate);
        }
    }

    private void addEventItem(Document doc, Element channel, EventItem event) {
        Element item = doc.createElement(ITEM);

        // Title includes event date in ISO format
        String titleWithDate = event.title() + " (" + event.eventDateStart().toString() + ")";
        addElement(doc, item, TITLE, titleWithDate);
        addElement(doc, item, LINK, event.link());
        addDescriptionElement(doc, item, event.sanitizedDescription());

        // pubDate is the harvest time in RFC-1123 format
        String harvestTime = ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        addElement(doc, item, PUB_DATE, harvestTime);

        addGuidElement(doc, item, event);

        addEventDateElements(doc, item, event);

        if (event.hasImage()) {
            Element enclosure = doc.createElement(ENCLOSURE);
            enclosure.setAttribute(URL_ATTR, event.imageUrl());
            enclosure.setAttribute(TYPE_ATTR, IMAGE_JPEG_TYPE);
            item.appendChild(enclosure);
        }

        channel.appendChild(item);
    }

    /**
     * Adds the item GUID, which is always the event URL and therefore a permalink.
     *
     * @param doc   the XML document
     * @param item  the item element to add to
     * @param event the event whose GUID to add
     */
    private void addGuidElement(Document doc, Element item, EventItem event) {
        Element guid = doc.createElement(GUID);
        guid.setAttribute(IS_PERMALINK_ATTR, TRUE_VALUE);
        guid.setTextContent(event.guid());
        item.appendChild(guid);
    }

    @Override
    public void generateFeed(String filePath, List<EventItem> newEvents, String existingFilePath)
        throws Exception {
        requireNonNull(filePath, "filePath must not be null");
        requireNonNull(newEvents, "newEvents must not be null");
        requireNonNull(existingFilePath, "existingFilePath must not be null");

        LOG.info("Generating RSS feed with {} new events", newEvents.size());

        // Create XML document
        DocumentBuilder builder =
            securityConfigurer.createSecureDocumentBuilderFactory().newDocumentBuilder();
        Document doc = builder.newDocument();

        // Create RSS root element
        Element rss = doc.createElement(RSS);
        rss.setAttribute(VERSION_ATTR, RSS_VERSION);
        rss.setAttribute(XMLNS_EV_ATTR, EVENT_NAMESPACE_URI);
        doc.appendChild(rss);

        // Create channel element
        Element channel = doc.createElement(CHANNEL);
        rss.appendChild(channel);

        // Add channel metadata
        addChannelMetadata(doc, channel);

        // Add new events (sorted by eventDateStart descending), skipping any past retention
        List<EventItem> sortedEvents = new ArrayList<>(newEvents);
        sortedEvents.sort(Comparator.comparing(EventItem::eventDateStart).reversed());

        // Add new event items
        for (EventItem event : sortedEvents) {
            if (eventFilter.shouldKeep(event)) {
                addEventItem(doc, channel, event);
            }
        }

        importExistingEvents(doc, channel, new File(existingFilePath));

        // Write to file
        writeXmlToFile(doc, filePath);
        LOG.info("RSS feed written to: {}", filePath);
    }

    /**
     * Imports items from the existing feed, dropping those past the retention period.
     *
     * <p>Errors are logged rather than thrown so a scheduled run still publishes new events.
     *
     * @param doc              the new feed document
     * @param channel          the channel to append items to
     * @param existingFeedFile the existing feed file (may not exist)
     */
    private void importExistingEvents(Document doc, Element channel,
                                      File existingFeedFile) {
        if (!existingFeedFile.exists()) {
            return;
        }
        try {
            DocumentBuilder builder =
                securityConfigurer.createSecureDocumentBuilderFactory().newDocumentBuilder();
            NodeList items = builder.parse(existingFeedFile).getElementsByTagName(ITEM);
            int imported = 0;
            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                if (eventFilter.shouldKeep(item)) {
                    Node importedNode = doc.importNode(item, true);
                    removeWhitespaceNodes(importedNode);
                    channel.appendChild(importedNode);
                    imported++;
                }
            }
            LOG.info("Imported {} existing events, dropped {} past retention",
                imported, items.getLength() - imported);
        } catch (Exception e) {
            LOG.error("Failed to import existing events from {}: {}",
                existingFeedFile, e.getMessage(), e);
        }
    }

    @Override
    public Set<String> loadExistingGuids(String filePath)
        throws Exception {
        requireNonNull(filePath, "filePath must not be null");
        Set<String> guids = new HashSet<>();
        File file = new File(filePath);

        if (!file.exists()) {
            LOG.info("No existing RSS file found at: {}", filePath);
            return guids;
        }

        DocumentBuilder builder =
            securityConfigurer.createSecureDocumentBuilderFactory().newDocumentBuilder();
        Document doc = builder.parse(file);

        NodeList items = doc.getElementsByTagName(ITEM);
        for (int i = 0; i < items.getLength(); i++) {
            Node item = items.item(i);
            NodeList children = item.getChildNodes();

            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if (GUID.equals(child.getNodeName())) {
                    String guid = child.getTextContent();
                    if (guid != null && !guid.isBlank()) {
                        guids.add(guid.trim());
                    }
                }
            }
        }

        LOG.info("Loaded {} existing GUIDs from {}", guids.size(), filePath);
        return guids;
    }

    /**
     * Removes whitespace-only text nodes from a DOM tree.
     *
     * <p>This is necessary to ensure proper indentation when writing XML.
     *
     * @param node The root node to clean
     */
    private void removeWhitespaceNodes(Node node) {
        Deque<Node> stack = new ArrayDeque<>();
        stack.push(node);

        while (!stack.isEmpty()) {
            Node current = stack.pop();
            NodeList children = current.getChildNodes();

            for (int i = children.getLength() - 1; i >= 0; i--) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.TEXT_NODE) {
                    if (child.getTextContent().trim().isEmpty()) {
                        current.removeChild(child);
                    }
                } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                    stack.push(child);
                }
            }
        }
    }

    private void writeXmlToFile(Document doc, String filePath)
        throws TransformerException, IOException {
        Transformer transformer =
            securityConfigurer.createSecureTransformerFactory().newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, ENCODING_UTF8);
        transformer.setOutputProperty(XSLT_INDENT_PROPERTY, INDENT_AMOUNT);

        DOMSource source = new DOMSource(doc);
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            StreamResult result = new StreamResult(fos);
            transformer.transform(source, result);
        }
    }
}
