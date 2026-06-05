package at.aau.webcrawler.crawler;

import at.aau.webcrawler.fetch.JsoupLinkStatusChecker;
import at.aau.webcrawler.fetch.JsoupPageFetcher;
import at.aau.webcrawler.fetch.LinkStatusChecker;
import at.aau.webcrawler.fetch.PageContent;
import at.aau.webcrawler.fetch.PageFetcher;
import at.aau.webcrawler.fetch.PageLoadException;
import at.aau.webcrawler.model.LinkResult;
import at.aau.webcrawler.model.PageResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebCrawler {

  private final int maxDepth;
  private final List<String> allowedDomains;
  private final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
  private final DomainFilter domainFilter = new DomainFilter();
  private final PageFetcher pageFetcher;
  private final LinkStatusChecker linkStatusChecker;

  public WebCrawler(int maxDepth, List<String> allowedDomains) {
    this(maxDepth, allowedDomains, new JsoupPageFetcher(), new JsoupLinkStatusChecker());
  }

  public WebCrawler(
      int maxDepth,
      List<String> allowedDomains,
      PageFetcher pageFetcher,
      LinkStatusChecker linkStatusChecker
  ) {
    this.maxDepth = maxDepth;
    this.allowedDomains = List.copyOf(allowedDomains);
    this.pageFetcher = pageFetcher;
    this.linkStatusChecker = linkStatusChecker;
  }

  public PageResult crawl(String startUrl) {
    return crawlPage(startUrl, maxDepth);
  }

  // AI-assisted: the recursive structure of this method was discussed with AI.
  // The final implementation was manually adapted, reviewed, and tested.
  private PageResult crawlPage(String url, int depth) {
    if (!domainFilter.isAllowed(url, allowedDomains)) {
      throw new IllegalArgumentException("URL is not in an allowed domain: " + url);
    }

    if (!tryMarkAsVisited(url)) {
      throw new IllegalArgumentException("URL has already been visited: " + url);
    }

    PageContent content;
    try {
      content = pageFetcher.fetch(url);
    } catch (PageLoadException exception) {
      return PageResult.error(url, depth, exception.getMessage());
    }

    List<LinkResult> links = analyzeLinks(content.links());
    List<PageResult> childPages = collectChildPages(links, depth);

    return PageResult.builder(url, depth)
        .headings(content.headings())
        .links(links)
        .childPages(childPages)
        .build();
  }

  private List<PageResult> collectChildPages(List<LinkResult> links, int depth) {
    List<PageResult> childPages = new ArrayList<>();
    if (depth <= 0) {
      return childPages;
    }
    for (LinkResult link : links) {
      if (shouldFollow(link)) {
        childPages.add(crawlPage(link.getUrl(), depth - 1));
      }
    }
    return childPages;
  }

  private boolean shouldFollow(LinkResult link) {
    return !link.isBroken()
        && domainFilter.isAllowed(link.getUrl(), allowedDomains)
        && !hasVisited(link.getUrl());
  }

  private boolean hasVisited(String url) {
    return visitedUrls.contains(url);
  }

  private boolean tryMarkAsVisited(String url) {
    return visitedUrls.add(url);
  }

  private List<LinkResult> analyzeLinks(List<String> extractedLinks) {
    List<LinkResult> links = new ArrayList<>();
    Set<String> uniqueLinks = new HashSet<>();

    for (String extractedLink : extractedLinks) {
      if (uniqueLinks.add(extractedLink)) {
        boolean broken = linkStatusChecker.isBroken(extractedLink);
        links.add(new LinkResult(extractedLink, broken));
      }
    }

    return links;
  }
}
