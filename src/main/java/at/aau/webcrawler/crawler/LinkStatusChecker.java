package at.aau.webcrawler.crawler;

import org.jsoup.Jsoup;

import java.io.IOException;

public class LinkStatusChecker {
  private static final int FIRST_HTTP_ERROR_STATUS_CODE = 400;

  private final int requestTimeoutMs;

  public LinkStatusChecker(int requestTimeoutMs) {
    this.requestTimeoutMs = requestTimeoutMs;
  }

  public boolean isBroken(String url) {
    if (!isHttpUrl(url)) {
      return true;
    }

    try {
      int statusCode = Jsoup.connect(url)
          .timeout(requestTimeoutMs)
          .ignoreHttpErrors(true)
          .execute()
          .statusCode();
      return statusCode >= FIRST_HTTP_ERROR_STATUS_CODE;
    } catch (IOException exception) {
      return true;
    }
  }

  private boolean isHttpUrl(String url) {
    return url.startsWith("http://") || url.startsWith("https://");
  }
}
