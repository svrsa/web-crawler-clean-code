package at.aau.webcrawler;

import java.util.List;

public class CrawlerConfiguration {
  private final String startUrl;
  private final int maxDepth;
  private final List<String> allowedDomains;

  public CrawlerConfiguration(String startUrl, int maxDepth, List<String> allowedDomains) {
    this.startUrl = startUrl;
    this.maxDepth = maxDepth;
    this.allowedDomains = allowedDomains;
  }

  public String getStartUrl() {
    return startUrl;
  }

  public int getMaxDepth() {
    return maxDepth;
  }

  public List<String> getAllowedDomains() {
    return allowedDomains;
  }
}