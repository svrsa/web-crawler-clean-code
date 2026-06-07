package at.aau.webcrawler.fetch;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * jsoup-backed implementation of {@link PageFetcher}.
 * Keeps jsoup-specific HTML loading and parsing behind the fetch boundary.
 */
public class JsoupPageFetcher implements PageFetcher {
  private static final int DEFAULT_TIMEOUT_MS = 5000;

  private final int requestTimeoutMs;

  public JsoupPageFetcher() {
    this(DEFAULT_TIMEOUT_MS);
  }

  public JsoupPageFetcher(int requestTimeoutMs) {
    this.requestTimeoutMs = requestTimeoutMs;
  }

  @Override
  public PageContent fetch(String url) throws PageLoadException {
    try {
      Document document = Jsoup.connect(url).timeout(requestTimeoutMs).get();
      return new PageContent(extractHeadings(document), extractLinks(document));
    } catch (IOException exception) {
      throw new PageLoadException("Could not load page: " + url + " (" + exception.getMessage() + ")", exception);
    }
  }

  private List<String> extractHeadings(Document document) {
    List<String> headings = new ArrayList<>();
    document.select("h1, h2, h3, h4, h5, h6").forEach(element ->
        headings.add(toMarkdownHeading(element)));
    return headings;
  }

  private List<String> extractLinks(Document document) {
    List<String> links = new ArrayList<>();
    document.select("a[href]").forEach(element ->
        links.add(element.attr("abs:href")));
    return links;
  }

  // AI-assisted: the markdown-prefix mapping for heading levels was discussed with AI.
  // The final implementation was manually adapted, reviewed, and tested.
  private String toMarkdownHeading(Element element) {
    return switch (element.tagName()) {
      case "h1" -> "# " + element.text();
      case "h2" -> "## " + element.text();
      case "h3" -> "### " + element.text();
      case "h4" -> "#### " + element.text();
      case "h5" -> "##### " + element.text();
      case "h6" -> "###### " + element.text();
      default   -> element.text();
    };
  }
}
