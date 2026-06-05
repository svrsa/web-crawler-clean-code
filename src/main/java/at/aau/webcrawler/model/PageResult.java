package at.aau.webcrawler.model;

import java.util.ArrayList;
import java.util.List;

public class PageResult {
  private final String url;
  private final int depth;
  private final List<String> headings;
  private final List<LinkResult> links;
  private final List<PageResult> childPages;
  private final String errorMessage;

  private PageResult(Builder builder) {
    this.url = builder.url;
    this.depth = builder.depth;
    this.headings = List.copyOf(builder.headings);
    this.links = List.copyOf(builder.links);
    this.childPages = List.copyOf(builder.childPages);
    this.errorMessage = builder.errorMessage;
  }

  public static Builder builder(String url, int depth) {
    return new Builder(url, depth);
  }

  /**
   * Creates a {@code PageResult} that represents a failed page load.
   * Callers don't need to deal with a nullable error message anywhere —
   * use {@link #hasError()} to discriminate.
   */
  public static PageResult error(String url, int depth, String errorMessage) {
    return new Builder(url, depth).errorMessage(errorMessage).build();
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

  public boolean hasError() {
    return errorMessage != null;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public static class Builder {
    private final String url;
    private final int depth;
    private List<String> headings = List.of();
    private List<LinkResult> links = List.of();
    private List<PageResult> childPages = List.of();
    private String errorMessage;

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

    private Builder errorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
      return this;
    }

    public PageResult build() {
      return new PageResult(this);
    }
  }
}
