package at.aau.webcrawler.crawler;

import at.aau.webcrawler.model.LinkResult;

import java.util.List;

record CrawledPage(
    String url,
    int depth,
    List<String> headings,
    List<LinkResult> links,
    String errorMessage
) {
  static CrawledPage success(String url, int depth, List<String> headings, List<LinkResult> links) {
    return new CrawledPage(url, depth, List.copyOf(headings), List.copyOf(links), null);
  }

  static CrawledPage error(String url, int depth, String errorMessage) {
    return new CrawledPage(url, depth, List.of(), List.of(), errorMessage);
  }

  boolean hasError() {
    return errorMessage != null;
  }
}
