package carolinatheatre.events.webdriver;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads web pages using Selenium and parses them with JSoup.
 * Waits for dynamic content to load before parsing.
 */
public record PageLoader(WebDriver driver, Duration timeout) {

    private static final Logger LOG = LoggerFactory.getLogger(PageLoader.class);

    /**
     * Creates a new PageLoader.
     *
     * @param driver  the WebDriver to use
     * @param timeout the page load timeout
     */
    public PageLoader(final WebDriver driver, final Duration timeout) {
        this.driver = requireNonNull(driver);
        this.timeout = requireNonNull(timeout);
    }

    /**
     * Loads a URL and returns the parsed HTML document.
     * Waits for each provided By locator in sequence before parsing.
     *
     * @param url      URL to load
     * @param locators By locators to wait for (in order)
     * @return Parsed JSoup document
     * @throws NullPointerException if url is null
     */
    public Document loadPage(final String url, final By... locators) {
        requireNonNull(url, "url must not be null");
        LOG.info("Loading page: {}", url);
        driver.get(url);

        final WebDriverWait wait = new WebDriverWait(driver, timeout);

        for (final By locator : locators) {
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            } catch (Exception e) {
                LOG.warn("Element not found after waiting: {}", locator);
            }
        }

        final String pageSource = driver.getPageSource();
        return Jsoup.parse(requireNonNull(pageSource));
    }
}
