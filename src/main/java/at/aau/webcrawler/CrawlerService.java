package at.aau.webcrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrawlerService {
  private final Set<String> visitedUrls = new HashSet<>();
  private final DomainFilter domainFilter = new DomainFilter();

  public Document loadDocument(String url) {
    try {
      return Jsoup.connect(url).get();
    } catch (IOException exception) {
      throw new IllegalArgumentException("Could not load document from URL: " + url, exception);
    }
  }

  public boolean hasVisited(String url) {
    return visitedUrls.contains(url);
  }

  public void markAsVisited(String url) {
    visitedUrls.add(url);
  }

  public boolean isBrokenLink(String url) {
    try {
      int statusCode = Jsoup.connect(url).ignoreHttpErrors(true).execute().statusCode();
      return statusCode >= 400;
    } catch (IOException exception) {
      return true;
    }
  }

  public PageResult analyzePage(String url, int depth, List<String> allowedDomains) {
    if (!domainFilter.isAllowed(url, allowedDomains)) {
      throw new IllegalArgumentException("URL is not in an allowed domain: " + url);
    }

    if (hasVisited(url)) {
      throw new IllegalArgumentException("URL has already been visited: " + url);
    }

    markAsVisited(url);

    Document document = loadDocument(url);
    HtmlParser htmlParser = new HtmlParser();

    List<String> headings = htmlParser.extractHeadings(document);
    List<String> extractedLinks = htmlParser.extractLinks(document);
    List<LinkResult> links = new ArrayList<>();
    List<PageResult> childPages = new ArrayList<>();

    for (String extractedLink : extractedLinks) {
      boolean broken = isBrokenLink(extractedLink);
      links.add(new LinkResult(extractedLink, broken));
    }

    if (depth > 0) {
      for (LinkResult link : links) {
        if (!link.isBroken()
            && domainFilter.isAllowed(link.getUrl(), allowedDomains)
            && !hasVisited(link.getUrl())) {
          childPages.add(analyzePage(link.getUrl(), depth - 1, allowedDomains));
        }
      }
    }

    return new PageResult(url, depth, headings, links, childPages);
  }
}