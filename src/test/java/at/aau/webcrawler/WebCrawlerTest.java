package at.aau.webcrawler;

import at.aau.webcrawler.crawler.WebCrawler;
import at.aau.webcrawler.fetch.LinkStatusChecker;
import at.aau.webcrawler.fetch.PageContent;
import at.aau.webcrawler.fetch.PageFetcher;
import at.aau.webcrawler.fetch.PageLoadException;
import at.aau.webcrawler.model.LinkResult;
import at.aau.webcrawler.model.PageResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebCrawlerTest {

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
  void shouldRejectDisallowedStartUrl() {
    WebCrawler webCrawler = new WebCrawler(0, List.of("example.com"), new FakePageFetcher(), new FakeLinkStatusChecker());

    assertThrows(
        IllegalArgumentException.class,
        () -> webCrawler.crawl("https://other.com")
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
    assertEquals(2, result.getChildPages().size());
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
    return new WebCrawler(
        maxDepth,
        List.of("example.com"),
        pageFetcher,
        linkStatusChecker
    );
  }

  private List<String> linkUrls(PageResult pageResult) {
    return pageResult.getLinks().stream()
        .map(LinkResult::getUrl)
        .toList();
  }

  private static class FakePageFetcher implements PageFetcher {
    private final Map<String, PageContent> pages = new HashMap<>();
    private final Map<String, String> failures = new HashMap<>();
    private final Map<String, Integer> fetchCounts = new HashMap<>();

    FakePageFetcher addPage(String url, List<String> headings, List<String> links) {
      pages.put(url, new PageContent(new ArrayList<>(headings), new ArrayList<>(links)));
      return this;
    }

    FakePageFetcher failOn(String url, String message) {
      failures.put(url, message);
      return this;
    }

    @Override
    public PageContent fetch(String url) throws PageLoadException {
      fetchCounts.merge(url, 1, Integer::sum);
      if (failures.containsKey(url)) {
        throw new PageLoadException(failures.get(url));
      }
      PageContent content = pages.get(url);
      if (content == null) {
        throw new PageLoadException("No page registered for " + url);
      }
      return content;
    }

    int fetchCount(String url) {
      return fetchCounts.getOrDefault(url, 0);
    }
  }

  private static class FakeLinkStatusChecker implements LinkStatusChecker {
    private final Set<String> brokenUrls = new HashSet<>();

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
