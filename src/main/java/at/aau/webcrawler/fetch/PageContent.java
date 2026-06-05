package at.aau.webcrawler.fetch;

import java.util.List;

/**
 * Immutable, library-agnostic representation of a parsed web page.
 * Lives on this side of the boundary so the rest of the application
 * never depends on a specific HTML parser (e.g. jsoup) directly.
 */
public record PageContent(List<String> headings, List<String> links) {
  public PageContent {
    headings = List.copyOf(headings);
    links = List.copyOf(links);
  }
}
