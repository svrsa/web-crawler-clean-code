package at.aau.webcrawler.crawler;

public class DocumentFetchException extends RuntimeException {
  public DocumentFetchException(String message) {
    super(message);
  }

  public DocumentFetchException(String message, Throwable cause) {
    super(message, cause);
  }
}
