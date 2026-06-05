package at.aau.webcrawler.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Optional;

public class HtmlDocumentFetcher {
  private final int requestTimeoutMs;

  public HtmlDocumentFetcher(int requestTimeoutMs) {
    this.requestTimeoutMs = requestTimeoutMs;
  }

  public Optional<Document> fetch(String url) {
    try {
      return Optional.of(Jsoup.connect(url).timeout(requestTimeoutMs).get());
    } catch (IOException exception) {
      return Optional.empty();
    }
  }
}
