# Carolina Theatre Events Harvester

This Java application scrapes upcoming events from the [Carolina Theatre](https://carolinatheatre.org/events/) in Raleigh, NC and generates an RSS feed. It uses Selenium WebDriver with headless Chrome to handle JavaScript-rendered content and JSoup for HTML parsing. The application produces an incremental RSS feed that appends new events to an existing feed file while filtering out old entries.

Feed exported to https://github.com/mckelvym/carolinatheatre.events.rss

## Build and Run

Build the application:

```bash
./gradlew build
```

Build the image:

```bash
source scripts/version.sh && ./gradlew jib -Djib.to.image=$IMAGE:$VERSION
```

Run with default output file (events.xml):

```bash
./gradlew run
```

Run with custom output file:

```bash
./gradlew run -Pargs='my-events.xml'
```

## Docker

The project uses Jib for containerization. Run with Docker:

```bash
./scripts/run.sh
```

This pulls and runs the latest Docker image from GitHub Container Registry.

## How It Works

The application follows a three-phase workflow:

1. Load Existing Feed - Reads the existing RSS file and extracts all GUIDs to avoid duplicates
2. Scrape Events - Loads the events page, discovers event URLs, and parses each event page for title, date, venue, and image information
3. Generate RSS Feed - Creates a new RSS 2.0 XML document with new events, imports existing events from the old feed, filters out events older than 7 days, and writes the result to the output file

Events are identified by their URL to ensure proper deduplication across runs.

## Architecture

The application uses a modular SOLID design with clear separation of concerns:

- Domain layer: EventItem for event data
- Config layer: Site-specific configuration
- WebDriver layer: Chrome automation and page loading
- Scraper layer: Event discovery
- Parser layer: Multi-strategy field extraction
- Feed layer: XXE-protected RSS generation

## Configuration

Event retention period: 7 days
Page load timeout: 10 seconds
Target URL: https://carolinatheatre.org/events/

## Output

The generated RSS feed includes:

- Event title
- Event description (category, time, venue)
- Event link (also used as GUID)
- Event image (when available)
- Publication date

Events are identified by their URL for deduplication.
