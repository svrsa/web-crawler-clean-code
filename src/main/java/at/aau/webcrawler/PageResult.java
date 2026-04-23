package at.aau.webcrawler;

import java.util.List;

public class PageResult {
  private final String url;
  private final int depth;
  private final List<String> headings;
  private final List<String> links;

  public PageResult(String url, int depth, List<String> headings, List<String> links) {
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

  public List<String> getLinks() {
    return links;
  }
}