package at.aau.webcrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class CrawlerService {

  public Document loadDocument(String url) {
    try {
      return Jsoup.connect(url).get();
    } catch (IOException exception) {
      throw new IllegalArgumentException("Could not load document from URL: " + url, exception);
    }
  }
}