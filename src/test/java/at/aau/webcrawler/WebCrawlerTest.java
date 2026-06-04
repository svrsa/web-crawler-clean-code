package at.aau.webcrawler;

import at.aau.webcrawler.crawler.HtmlDocumentFetcher;
import at.aau.webcrawler.crawler.LinkStatusChecker;
import at.aau.webcrawler.crawler.WebCrawler;
import at.aau.webcrawler.model.LinkResult;
import at.aau.webcrawler.model.PageResult;
import at.aau.webcrawler.parser.HtmlParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebCrawlerTest {

  @Test
  void shouldExtractPageDataFromStartUrl() {
    FakeDocumentFetcher documentFetcher = new FakeDocumentFetcher()
        .addPage("https://example.com", """
            <html>
              <body>
                <h1>Start</h1>
                <a href="https://example.com/about">About</a>
              </body>
            </html>
            """);

    PageResult result = createCrawler(0, documentFetcher).crawl("https://example.com");

    assertEquals("https://example.com", result.getUrl());
    assertEquals(0, result.getDepth());
    assertEquals(List.of("# Start"), result.getHeadings());
    assertEquals(List.of("https://example.com/about"), linkUrls(result));
  }

  @Test
  void shouldCrawlAllowedLinksWithinDepth() {
    FakeDocumentFetcher documentFetcher = new FakeDocumentFetcher()
        .addPage("https://example.com", """
            <html><body><a href="https://example.com/about">About</a></body></html>
            """)
        .addPage("https://example.com/about", """
            <html><body><h2>About</h2></body></html>
            """);

    PageResult result = createCrawler(1, documentFetcher).crawl("https://example.com");

    assertEquals(1, result.getChildPages().size());
    assertEquals("https://example.com/about", result.getChildPages().get(0).getUrl());
    assertEquals(0, result.getChildPages().get(0).getDepth());
  }

  @Test
  void shouldStopAtConfiguredDepth() {
    FakeDocumentFetcher documentFetcher = new FakeDocumentFetcher()
        .addPage("https://example.com", """
            <html><body><a href="https://example.com/about">About</a></body></html>
            """)
        .addPage("https://example.com/about", """
            <html><body><a href="https://example.com/team">Team</a></body></html>
            """)
        .addPage("https://example.com/team", """
            <html><body><h2>Team</h2></body></html>
            """);

    PageResult result = createCrawler(1, documentFetcher).crawl("https://example.com");

    assertEquals(1, result.getChildPages().size());
    assertTrue(result.getChildPages().get(0).getChildPages().isEmpty());
    assertEquals(0, documentFetcher.fetchCount("https://example.com/team"));
  }

  @Test
  void shouldNotFollowDisallowedDomains() {
    FakeDocumentFetcher documentFetcher = new FakeDocumentFetcher()
        .addPage("https://example.com", """
            <html><body><a href="https://other.com/page">Other</a></body></html>
            """)
        .addPage("https://other.com/page", """
            <html><body><h2>Other</h2></body></html>
            """);

    PageResult result = createCrawler(1, documentFetcher).crawl("https://example.com");

    assertTrue(result.getChildPages().isEmpty());
    assertEquals(0, documentFetcher.fetchCount("https://other.com/page"));
  }

  @Test
  void shouldNotFollowBrokenLinks() {
    FakeDocumentFetcher documentFetcher = new FakeDocumentFetcher()
        .addPage("https://example.com", """
            <html><body><a href="https://example.com/missing">Missing</a></body></html>
            """)
        .addPage("https://example.com/missing", """
            <html><body><h2>Missing</h2></body></html>
            """);
    FakeLinkStatusChecker linkStatusChecker = new FakeLinkStatusChecker()
        .markBroken("https://example.com/missing");

    PageResult result = createCrawler(1, documentFetcher, linkStatusChecker).crawl("https://example.com");

    assertTrue(result.getLinks().get(0).isBroken());
    assertTrue(result.getChildPages().isEmpty());
    assertEquals(0, documentFetcher.fetchCount("https://example.com/missing"));
  }

  @Test
  void shouldVisitDuplicateLinksOnlyOnce() {
    FakeDocumentFetcher documentFetcher = new FakeDocumentFetcher()
        .addPage("https://example.com", """
            <html>
              <body>
                <a href="https://example.com/about">About</a>
                <a href="https://example.com/about">About again</a>
              </body>
            </html>
            """)
        .addPage("https://example.com/about", """
            <html><body><h2>About</h2></body></html>
            """);

    PageResult result = createCrawler(1, documentFetcher).crawl("https://example.com");

    assertEquals(1, result.getLinks().size());
    assertEquals(1, result.getChildPages().size());
    assertEquals(1, documentFetcher.fetchCount("https://example.com/about"));
  }

  @Test
  void shouldRejectDisallowedStartUrl() {
    WebCrawler webCrawler = new WebCrawler(0, List.of("example.com"));

    assertThrows(
        IllegalArgumentException.class,
        () -> webCrawler.crawl("https://other.com")
    );
  }

  private WebCrawler createCrawler(int maxDepth, FakeDocumentFetcher documentFetcher) {
    return createCrawler(maxDepth, documentFetcher, new FakeLinkStatusChecker());
  }

  private WebCrawler createCrawler(
      int maxDepth,
      FakeDocumentFetcher documentFetcher,
      FakeLinkStatusChecker linkStatusChecker
  ) {
    return new WebCrawler(
        maxDepth,
        List.of("example.com"),
        documentFetcher,
        new HtmlParser(),
        linkStatusChecker
    );
  }

  private List<String> linkUrls(PageResult pageResult) {
    return pageResult.getLinks().stream()
        .map(LinkResult::getUrl)
        .toList();
  }

  private static class FakeDocumentFetcher extends HtmlDocumentFetcher {
    private final Map<String, Document> documents = new HashMap<>();
    private final Map<String, Integer> fetchCounts = new HashMap<>();

    FakeDocumentFetcher() {
      super(0);
    }

    FakeDocumentFetcher addPage(String url, String html) {
      documents.put(url, Jsoup.parse(html, url));
      return this;
    }

    @Override
    public Optional<Document> fetch(String url) {
      fetchCounts.merge(url, 1, Integer::sum);
      return Optional.ofNullable(documents.get(url));
    }

    int fetchCount(String url) {
      return fetchCounts.getOrDefault(url, 0);
    }
  }

  private static class FakeLinkStatusChecker extends LinkStatusChecker {
    private final List<String> brokenUrls = new java.util.ArrayList<>();

    FakeLinkStatusChecker() {
      super(0);
    }

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
