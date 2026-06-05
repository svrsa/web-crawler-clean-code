package at.aau.webcrawler.fetch;

/**
 * Boundary toward the HTML retrieval and parsing library.
 * Implementations must translate library specifics into a {@link PageContent}
 * and signal failures via {@link PageLoadException}.
 */
public interface PageFetcher {
  PageContent fetch(String url) throws PageLoadException;
}
