package at.aau.webcrawler.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

public class HtmlParser {

  public List<String> extractHeadings(Document document) {
    List<String> headings = new ArrayList<>();

    document.select("h1, h2, h3, h4, h5, h6").forEach(element -> {
      String prefix = buildMarkdownPrefix(element);
      headings.add(prefix + element.text());
    });

    return headings;
  }

  public List<String> extractLinks(Document document) {
    List<String> links = new ArrayList<>();

    document.select("a[href]").forEach(element -> links.add(element.attr("abs:href")));

    return links;
  }

  private String buildMarkdownPrefix(Element element) {
    return switch (element.tagName()) {
      case "h1" -> "# ";
      case "h2" -> "## ";
      case "h3" -> "### ";
      case "h4" -> "#### ";
      case "h5" -> "##### ";
      case "h6" -> "###### ";
      default   -> "";
    };
  }
}