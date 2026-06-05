package at.aau.webcrawler.fetch;

/**
 * Signals that a page could not be retrieved or parsed.
 * Checked on purpose so callers are forced to handle the failure
 * (e.g. by recording it in the report instead of crashing).
 */
public class PageLoadException extends Exception {
  public PageLoadException(String message) {
    super(message);
  }

  public PageLoadException(String message, Throwable cause) {
    super(message, cause);
  }
}
