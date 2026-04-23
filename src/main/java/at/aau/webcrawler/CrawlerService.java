package at.aau.webcrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CrawlerService {

  public Document loadDocument(String url) {
    try {
      return Jsoup.connect(url).get();
    } catch (IOException exception) {
      throw new IllegalArgumentException("Could not load document from URL: " + url, exception);
    }
  }

  public PageResult analyzePage(String url, int depth) {
    Document document = loadDocument(url);
    HtmlParser htmlParser = new HtmlParser();

    List<String> headings = htmlParser.extractHeadings(document);
    List<String> extractedLinks = htmlParser.extractLinks(document);
    List<LinkResult> links = new ArrayList<>();

    for (String extractedLink : extractedLinks) {
      links.add(new LinkResult(extractedLink, false));
    }

    return new PageResult(url, depth, headings, links);
  }
}