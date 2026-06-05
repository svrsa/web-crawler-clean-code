package at.aau.webcrawler.fetch;

/**
 * Boundary toward the HTTP status check.
 * Implementations decide whether a given URL is reachable.
 */
public interface LinkStatusChecker {
  boolean isBroken(String url);
}
