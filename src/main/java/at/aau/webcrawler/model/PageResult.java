package at.aau.webcrawler.model;

import java.util.ArrayList;
import java.util.List;

public class PageResult {
  private final String url;
  private final int depth;
  private final List<String> headings;
  private final List<LinkResult> links;
  private final List<PageResult> childPages;

  private PageResult(Builder builder) {
    this.url = builder.url;
    this.depth = builder.depth;
    this.headings = List.copyOf(builder.headings);
    this.links = List.copyOf(builder.links);
    this.childPages = List.copyOf(builder.childPages);
  }

  public static Builder builder(String url, int depth) {
    return new Builder(url, depth);
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

  public List<PageResult> getChildPages() {
    return childPages;
  }

  public static class Builder {
    private final String url;
    private final int depth;
    private List<String> headings = List.of();
    private List<LinkResult> links = List.of();
    private List<PageResult> childPages = List.of();

    private Builder(String url, int depth) {
      this.url = url;
      this.depth = depth;
    }

    public Builder headings(List<String> headings) {
      this.headings = new ArrayList<>(headings);
      return this;
    }

    public Builder links(List<LinkResult> links) {
      this.links = new ArrayList<>(links);
      return this;
    }

    public Builder childPages(List<PageResult> childPages) {
      this.childPages = new ArrayList<>(childPages);
      return this;
    }

    public PageResult build() {
      return new PageResult(this);
    }
  }
}
