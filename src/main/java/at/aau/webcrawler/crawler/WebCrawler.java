package at.aau.webcrawler.crawler;

import at.aau.webcrawler.fetch.JsoupLinkStatusChecker;
import at.aau.webcrawler.fetch.JsoupPageFetcher;
import at.aau.webcrawler.fetch.LinkStatusChecker;
import at.aau.webcrawler.fetch.PageFetcher;
import at.aau.webcrawler.config.CrawlerDefaults;
import at.aau.webcrawler.model.PageResult;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebCrawler {
  private final int maxDepth;
  private final List<String> allowedDomains;
  private final Set<String> visitedUrls = CrawlCoordinator.createVisitedUrlSet();
  private final DomainFilter domainFilter = new DomainFilter();
  private final PageFetcher pageFetcher;
  private final LinkStatusChecker linkStatusChecker;
  private final int threadCount;

  public WebCrawler(int maxDepth, List<String> allowedDomains) {
    this(maxDepth, allowedDomains, new JsoupPageFetcher(), new JsoupLinkStatusChecker());
  }

  public WebCrawler(int maxDepth, List<String> allowedDomains, int threadCount) {
    this(maxDepth, allowedDomains, new JsoupPageFetcher(), new JsoupLinkStatusChecker(), threadCount);
  }

  public WebCrawler(
      int maxDepth,
      List<String> allowedDomains,
      PageFetcher pageFetcher,
      LinkStatusChecker linkStatusChecker
  ) {
    this(maxDepth, allowedDomains, pageFetcher, linkStatusChecker, CrawlerDefaults.THREAD_COUNT);
  }

  public WebCrawler(
      int maxDepth,
      List<String> allowedDomains,
      PageFetcher pageFetcher,
      LinkStatusChecker linkStatusChecker,
      int threadCount
  ) {
    this.maxDepth = maxDepth;
    this.allowedDomains = List.copyOf(allowedDomains);
    this.pageFetcher = pageFetcher;
    this.linkStatusChecker = linkStatusChecker;
    this.threadCount = validateThreadCount(threadCount);
  }

  public PageResult crawl(String startUrl) {
    return crawl(List.of(startUrl)).get(0);
  }

  public List<PageResult> crawl(List<String> startUrls) {
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CrawlCoordinator coordinator = new CrawlCoordinator(
        maxDepth,
        allowedDomains,
        pageFetcher,
        linkStatusChecker,
        domainFilter,
        visitedUrls,
        executorService
    );

    try {
      return coordinator.crawl(startUrls);
    } finally {
      executorService.shutdownNow();
    }
  }

  private int validateThreadCount(int threadCount) {
    if (threadCount < CrawlerDefaults.MINIMUM_THREAD_COUNT) {
      throw new IllegalArgumentException("Thread count must be positive");
    }
    return threadCount;
  }
}
