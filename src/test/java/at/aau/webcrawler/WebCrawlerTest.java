package at.aau.webcrawler;

import at.aau.webcrawler.crawler.WebCrawler;
import at.aau.webcrawler.config.CrawlerDefaults;
import at.aau.webcrawler.fetch.LinkStatusChecker;
import at.aau.webcrawler.fetch.PageContent;
import at.aau.webcrawler.fetch.PageFetcher;
import at.aau.webcrawler.fetch.PageLoadException;
import at.aau.webcrawler.model.LinkResult;
import at.aau.webcrawler.model.PageResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebCrawlerTest {
  private static final int TWO_START_URLS = 2;
  private static final int CONCURRENT_THREAD_COUNT = 2;
  private static final int FETCH_DELAY_MS = 100;

  @Test
  void shouldExtractPageDataFromStartUrl() {
    FakePageFetcher pageFetcher = new FakePageFetcher()
        .addPage("https://example.com",
            List.of("# Start"),
            List.of("https://example.com/about"));

    PageResult result = createCrawler(0, pageFetcher).crawl("https://example.com");

    assertEquals("https://example.com", result.getUrl());
    assertEquals(0, result.getDepth());
    assertEquals(List.of("# Start"), result.getHeadings());
    assertEquals(List.of("https://example.com/about"), linkUrls(result));
  }

  @Test
  void shouldCrawlAllowedLinksWithinDepth() {
    FakePageFetcher pageFetcher = new FakePageFetcher()
        .addPage("https://example.com", List.of(), List.of("https://example.com/about"))
        .addPage("https://example.com/about", List.of("## About"), List.of());

    PageResult result = createCrawler(1, pageFetcher).crawl("https://example.com");

    assertEquals(1, result.getChildPages().size());
    assertEquals("https://example.com/about", result.getChildPages().get(0).getUrl());
    assertEquals(0, result.getChildPages().get(0).getDepth());
  }

  @Test
  void shouldStopAtConfiguredDepth() {
    FakePageFetcher pageFetcher = new FakePageFetcher()
        .addPage("https://example.com", List.of(), List.of("https://example.com/about"))
        .addPage("https://example.com/about", List.of(), List.of("https://example.com/team"))
        .addPage("https://example.com/team", List.of("## Team"), List.of());

    PageResult result = createCrawler(1, pageFetcher).crawl("https://example.com");

    assertEquals(1, result.getChildPages().size());
    assertTrue(result.getChildPages().get(0).getChildPages().isEmpty());
    assertEquals(0, pageFetcher.fetchCount("https://example.com/team"));
  }

  @Test
  void shouldNotFollowDisallowedDomains() {
    FakePageFetcher pageFetcher = new FakePageFetcher()
        .addPage("https://example.com", List.of(), List.of("https://other.com/page"))
        .addPage("https://other.com/page", List.of("## Other"), List.of());

    PageResult result = createCrawler(1, pageFetcher).crawl("https://example.com");

    assertTrue(result.getChildPages().isEmpty());
    assertEquals(0, pageFetcher.fetchCount("https://other.com/page"));
  }

  @Test
  void shouldNotFollowBrokenLinks() {
    FakePageFetcher pageFetcher = new FakePageFetcher()
        .addPage("https://example.com", List.of(), List.of("https://example.com/missing"))
        .addPage("https://example.com/missing", List.of("## Missing"), List.of());
    FakeLinkStatusChecker linkStatusChecker = new FakeLinkStatusChecker()
        .markBroken("https://example.com/missing");

    PageResult result = createCrawler(1, pageFetcher, linkStatusChecker).crawl("https://example.com");

    assertTrue(result.getLinks().get(0).isBroken());
    assertTrue(result.getChildPages().isEmpty());
    assertEquals(0, pageFetcher.fetchCount("https://example.com/missing"));
  }

  @Test
  void shouldVisitDuplicateLinksOnlyOnce() {
    FakePageFetcher pageFetcher = new FakePageFetcher()
        .addPage("https://example.com",
            List.of(),
            List.of("https://example.com/about", "https://example.com/about"))
        .addPage("https://example.com/about", List.of("## About"), List.of());

    PageResult result = createCrawler(1, pageFetcher).crawl("https://example.com");

    assertEquals(1, result.getLinks().size());
    assertEquals(1, result.getChildPages().size());
    assertEquals(1, pageFetcher.fetchCount("https://example.com/about"));
  }

  @Test
  void shouldRejectAlreadyVisitedUrl() {
    FakePageFetcher pageFetcher = new FakePageFetcher()
        .addPage("https://example.com", List.of("# Start"), List.of());
    WebCrawler webCrawler = createCrawler(0, pageFetcher);

    webCrawler.crawl("https://example.com");

    assertThrows(
        IllegalArgumentException.class,
        () -> webCrawler.crawl("https://example.com")
    );
    assertEquals(1, pageFetcher.fetchCount("https://example.com"));
  }

  @Test
  void shouldCrawlMultipleStartUrlsInOneRun() {
    FakePageFetcher pageFetcher = new FakePageFetcher()
        .addPage("https://example.com", List.of("# First"), List.of())
        .addPage("https://example.org", List.of("# Second"), List.of());

    List<PageResult> results = createCrawler(
        0,
        List.of("example.com", "example.org"),
        pageFetcher,
        new FakeLinkStatusChecker(),
        CONCURRENT_THREAD_COUNT
    ).crawl(List.of("https://example.com", "https://example.org"));

    assertEquals(TWO_START_URLS, results.size());
    assertEquals("https://example.com", results.get(0).getUrl());
    assertEquals("https://example.org", results.get(1).getUrl());
    assertEquals(1, pageFetcher.fetchCount("https://example.com"));
    assertEquals(1, pageFetcher.fetchCount("https://example.org"));
  }

  @Test
  void shouldFetchStartUrlsConcurrently() {
    FakePageFetcher pageFetcher = new FakePageFetcher()
        .addPage("https://example.com", List.of("# First"), List.of())
        .addPage("https://example.org", List.of("# Second"), List.of())
        .delayFetchesBy(FETCH_DELAY_MS);

    createCrawler(
        0,
        List.of("example.com", "example.org"),
        pageFetcher,
        new FakeLinkStatusChecker(),
        CONCURRENT_THREAD_COUNT
    ).crawl(List.of("https://example.com", "https://example.org"));

    assertTrue(
        pageFetcher.maxConcurrentFetches() >= TWO_START_URLS,
        "At least two pages should have been fetched at the same time"
    );
  }

  @Test
  void shouldRejectDisallowedStartUrl() {
    WebCrawler webCrawler = new WebCrawler(0, List.of("example.com"), new FakePageFetcher(), new FakeLinkStatusChecker());

    assertThrows(
        IllegalArgumentException.class,
        () -> webCrawler.crawl("https://other.com")
    );
  }

  @Test
  void shouldRejectInvalidThreadCount() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new WebCrawler(0, List.of("example.com"), new FakePageFetcher(), new FakeLinkStatusChecker(), 0)
    );
  }

  // AI-assisted: the error-recovery test setup (failing fetcher + assertions on PageResult.hasError)
  // was discussed with AI. The final test structure was manually adapted and validated.
  @Test
  void shouldRecordPageLoadErrorWithoutCrashing() {
    FakePageFetcher pageFetcher = new FakePageFetcher()
        .failOn("https://example.com", "timeout after 5000ms");

    PageResult result = createCrawler(0, pageFetcher).crawl("https://example.com");

    assertTrue(result.hasError());
    assertEquals("timeout after 5000ms", result.getErrorMessage());
    assertTrue(result.getHeadings().isEmpty());
    assertTrue(result.getLinks().isEmpty());
    assertTrue(result.getChildPages().isEmpty());
  }

  @Test
  void shouldKeepSiblingResultsWhenSinglePageFails() {
    FakePageFetcher pageFetcher = new FakePageFetcher()
        .addPage("https://example.com",
            List.of("# Root"),
            List.of("https://example.com/ok", "https://example.com/broken-fetch"))
        .addPage("https://example.com/ok", List.of("## Ok"), List.of())
        .failOn("https://example.com/broken-fetch", "host unreachable");

    PageResult result = createCrawler(1, pageFetcher).crawl("https://example.com");

    assertFalse(result.hasError());
    assertEquals(TWO_START_URLS, result.getChildPages().size());
    PageResult ok = childByUrl(result, "https://example.com/ok");
    PageResult failed = childByUrl(result, "https://example.com/broken-fetch");
    assertFalse(ok.hasError());
    assertTrue(failed.hasError());
    assertEquals("host unreachable", failed.getErrorMessage());
  }

  private PageResult childByUrl(PageResult parent, String url) {
    return parent.getChildPages().stream()
        .filter(child -> child.getUrl().equals(url))
        .findFirst()
        .orElseThrow();
  }

  private WebCrawler createCrawler(int maxDepth, FakePageFetcher pageFetcher) {
    return createCrawler(maxDepth, pageFetcher, new FakeLinkStatusChecker());
  }

  private WebCrawler createCrawler(
      int maxDepth,
      FakePageFetcher pageFetcher,
      FakeLinkStatusChecker linkStatusChecker
  ) {
    return createCrawler(
        maxDepth,
        List.of("example.com"),
        pageFetcher,
        linkStatusChecker,
        CrawlerDefaults.THREAD_COUNT
    );
  }

  private WebCrawler createCrawler(
      int maxDepth,
      List<String> allowedDomains,
      FakePageFetcher pageFetcher,
      FakeLinkStatusChecker linkStatusChecker,
      int threadCount
  ) {
    return new WebCrawler(
        maxDepth,
        allowedDomains,
        pageFetcher,
        linkStatusChecker,
        threadCount
    );
  }

  private List<String> linkUrls(PageResult pageResult) {
    return pageResult.getLinks().stream()
        .map(LinkResult::getUrl)
        .toList();
  }

  private static class FakePageFetcher implements PageFetcher {
    private final Map<String, PageContent> pages = new ConcurrentHashMap<>();
    private final Map<String, String> failures = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> fetchCounts = new ConcurrentHashMap<>();
    private final AtomicInteger activeFetches = new AtomicInteger();
    private final AtomicInteger maxConcurrentFetches = new AtomicInteger();
    private long fetchDelayMs;

    FakePageFetcher addPage(String url, List<String> headings, List<String> links) {
      pages.put(url, new PageContent(new ArrayList<>(headings), new ArrayList<>(links)));
      return this;
    }

    FakePageFetcher failOn(String url, String message) {
      failures.put(url, message);
      return this;
    }

    FakePageFetcher delayFetchesBy(long fetchDelayMs) {
      this.fetchDelayMs = fetchDelayMs;
      return this;
    }

    @Override
    public PageContent fetch(String url) throws PageLoadException {
      int activeFetchCount = activeFetches.incrementAndGet();
      maxConcurrentFetches.updateAndGet(previous -> Math.max(previous, activeFetchCount));

      try {
        fetchCounts.computeIfAbsent(url, ignored -> new AtomicInteger()).incrementAndGet();
        sleepBeforeReturning();
        if (failures.containsKey(url)) {
          throw new PageLoadException(failures.get(url));
        }
        PageContent content = pages.get(url);
        if (content == null) {
          throw new PageLoadException("No page registered for " + url);
        }
        return content;
      } finally {
        activeFetches.decrementAndGet();
      }
    }

    private void sleepBeforeReturning() throws PageLoadException {
      if (fetchDelayMs <= 0) {
        return;
      }
      try {
        Thread.sleep(fetchDelayMs);
      } catch (InterruptedException exception) {
        Thread.currentThread().interrupt();
        throw new PageLoadException("Interrupted while fetching page", exception);
      }
    }

    int fetchCount(String url) {
      return fetchCounts.getOrDefault(url, new AtomicInteger()).get();
    }

    int maxConcurrentFetches() {
      return maxConcurrentFetches.get();
    }
  }

  private static class FakeLinkStatusChecker implements LinkStatusChecker {
    private final Set<String> brokenUrls = ConcurrentHashMap.newKeySet();

    FakeLinkStatusChecker markBroken(String url) {
      brokenUrls.add(url);
      return this;
    }

    @Override
    public boolean isBroken(String url) {
      return brokenUrls.contains(url);
    }
  }
}
