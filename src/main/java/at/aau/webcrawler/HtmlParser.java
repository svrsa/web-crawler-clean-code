package at.aau.webcrawler;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

public class HtmlParser {
  public List<String> extractHeadings(Document document) {
    List<String> headings = new ArrayList<>();

    document.select("h1, h2, h3, h4, h5, h6").forEach(element -> headings.add(element.text()));

    return headings;
  }

  public List<String> extractLinks(Document document) {
    List<String> links = new ArrayList<>();

    document.select("a[href]").forEach(element -> links.add(element.attr("abs:href")));

    return links;
  }
}