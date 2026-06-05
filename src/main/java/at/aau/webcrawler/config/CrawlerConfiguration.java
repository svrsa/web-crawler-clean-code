package at.aau.webcrawler.config;

import java.util.List;

public class CrawlerConfiguration {
  private final List<String> startUrls;
  private final int maxDepth;
  private final List<String> allowedDomains;
  private final int threadCount;

  public CrawlerConfiguration(
      List<String> startUrls,
      int maxDepth,
      List<String> allowedDomains,
      int threadCount
  ) {
    this.startUrls = List.copyOf(startUrls);
    this.maxDepth = maxDepth;
    this.allowedDomains = List.copyOf(allowedDomains);
    this.threadCount = threadCount;
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

  public int getThreadCount() {
    return threadCount;
  }
}
