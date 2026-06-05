package at.aau.webcrawler.config;

import java.util.List;

public class CrawlerConfiguration {
  private final List<String> startUrls;
  private final int maxDepth;
  private final List<String> allowedDomains;

  public CrawlerConfiguration(List<String> startUrls, int maxDepth, List<String> allowedDomains) {
    this.startUrls = List.copyOf(startUrls);
    this.maxDepth = maxDepth;
    this.allowedDomains = List.copyOf(allowedDomains);
  }

  public List<String> getStartUrls() {
    return startUrls;
  }

  public int getMaxDepth() {
    return maxDepth;
  }

  public List<String> getAllowedDomains() {
    return allowedDomains;
  }
}
