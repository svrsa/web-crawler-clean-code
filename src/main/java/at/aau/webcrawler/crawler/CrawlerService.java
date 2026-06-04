package at.aau.webcrawler.crawler;

import at.aau.webcrawler.model.LinkResult;
import at.aau.webcrawler.model.PageResult;
import at.aau.webcrawler.parser.HtmlParser;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrawlerService {
  private static final int REQUEST_TIMEOUT_MS = 5000;

  private final Set<String> visitedUrls = new HashSet<>();
  private final DomainFilter domainFilter = new DomainFilter();
  private final HtmlDocumentFetcher documentFetcher;
  private final HtmlParser htmlParser;
  private final LinkStatusChecker linkStatusChecker;

  public CrawlerService() {
    this(
        new HtmlDocumentFetcher(REQUEST_TIMEOUT_MS),
        new HtmlParser(),
        new LinkStatusChecker(REQUEST_TIMEOUT_MS)
    );
  }

  public CrawlerService(
      HtmlDocumentFetcher documentFetcher,
      HtmlParser htmlParser,
      LinkStatusChecker linkStatusChecker
  ) {
    this.documentFetcher = documentFetcher;
    this.htmlParser = htmlParser;
    this.linkStatusChecker = linkStatusChecker;
  }

  public Document loadDocument(String url) {
    return documentFetcher.fetch(url)
        .orElseThrow(() -> new DocumentFetchException("Could not load document from URL: " + url));
  }

  public boolean hasVisited(String url) {
    return visitedUrls.contains(url);
  }

  public void markAsVisited(String url) {
    visitedUrls.add(url);
  }

  public boolean isBrokenLink(String url) {
    return linkStatusChecker.isBroken(url);
  }

  // AI-assisted: the recursive structure of this method was discussed with AI.
  // The final implementation was manually adapted, reviewed, and tested.
  public PageResult crawlPage(String url, int depth, List<String> allowedDomains) {
    if (!domainFilter.isAllowed(url, allowedDomains)) {
      throw new IllegalArgumentException("URL is not in an allowed domain: " + url);
    }

    if (hasVisited(url)) {
      throw new IllegalArgumentException("URL has already been visited: " + url);
    }

    markAsVisited(url);

    PageResult pageResult = analyzeSinglePage(url, depth);

    if (depth > 0) {
      for (LinkResult link : pageResult.getLinks()) {
        if (!link.isBroken()
            && domainFilter.isAllowed(link.getUrl(), allowedDomains)
            && !hasVisited(link.getUrl())) {
          pageResult.addChildPage(crawlPage(link.getUrl(), depth - 1, allowedDomains));
        }
      }
    }

    return pageResult;
  }

  // AI-assisted: the basic structure of this method was refined with AI support.
  // The final implementation was manually adapted and validated.
  private PageResult analyzeSinglePage(String url, int depth) {
    Document document = loadDocument(url);
    List<String> headings = htmlParser.extractHeadings(document);
    List<String> extractedLinks = htmlParser.extractLinks(document);
    List<LinkResult> links = analyzeLinks(extractedLinks);

    return new PageResult(url, depth, headings, links, new ArrayList<>());
  }

  private List<LinkResult> analyzeLinks(List<String> extractedLinks) {
    List<LinkResult> links = new ArrayList<>();
    Set<String> uniqueLinks = new HashSet<>();

    for (String extractedLink : extractedLinks) {
      if (uniqueLinks.add(extractedLink)) {
        boolean broken = isBrokenLink(extractedLink);
        links.add(new LinkResult(extractedLink, broken));
      }
    }

    return links;
  }
}
