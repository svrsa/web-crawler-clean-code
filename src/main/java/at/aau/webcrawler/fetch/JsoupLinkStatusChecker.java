package at.aau.webcrawler.fetch;

import org.jsoup.Jsoup;

import java.io.IOException;

/**
 * jsoup-backed implementation of {@link LinkStatusChecker}.
 * Performs a lightweight HTTP request and inspects the status code.
 */
public class JsoupLinkStatusChecker implements LinkStatusChecker {
  private static final int DEFAULT_TIMEOUT_MS = 5000;
  private static final int FIRST_HTTP_ERROR_STATUS_CODE = 400;

  private final int requestTimeoutMs;

  public JsoupLinkStatusChecker() {
    this(DEFAULT_TIMEOUT_MS);
  }

  public JsoupLinkStatusChecker(int requestTimeoutMs) {
    this.requestTimeoutMs = requestTimeoutMs;
  }

  @Override
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
