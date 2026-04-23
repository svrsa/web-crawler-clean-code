package at.aau.webcrawler;

import java.util.List;

public class PageResult {
  private final String url;
  private final int depth;
  private final List<String> headings;
  private final List<LinkResult> links;

  public PageResult(String url, int depth, List<String> headings, List<LinkResult> links) {
    this.url = url;
    this.depth = depth;
    this.headings = headings;
    this.links = links;
  }

  public String getUrl() {
    return url;
  }

  public int getDepth() {
    return depth;
  }

  public List<String> getHeadings() {
    return headings;
  }

  public List<LinkResult> getLinks() {
    return links;
  }
}