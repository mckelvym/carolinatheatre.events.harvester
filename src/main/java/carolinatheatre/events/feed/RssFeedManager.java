package carolinatheatre.events.feed;

import carolinatheatre.events.domain.EventItem;
import java.util.List;
import java.util.Set;

/**
 * Interface for managing RSS feed operations.
 */
public interface RssFeedManager {

    /**
     * Generates an RSS feed from events.
     *
     * @param filePath         Output file path
     * @param newEvents        New events to add
     * @param existingFilePath Existing RSS file to merge with
     * @throws Exception if generation fails
     */
    void generateFeed(String filePath, List<EventItem> newEvents,
                      String existingFilePath)
        throws Exception;

    /**
     * Loads existing event GUIDs from the RSS file.
     *
     * @param filePath Path to RSS file
     * @return Set of existing GUIDs
     * @throws Exception if loading fails
     */
    Set<String> loadExistingGuids(String filePath)
        throws Exception;
}
