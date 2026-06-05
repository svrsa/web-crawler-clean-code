package at.aau.webcrawler.crawler;

import at.aau.webcrawler.fetch.LinkStatusChecker;
import at.aau.webcrawler.fetch.PageFetcher;
import at.aau.webcrawler.model.LinkResult;
import at.aau.webcrawler.model.PageResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

class CrawlCoordinator {
  private static final Logger LOGGER = Logger.getLogger(CrawlCoordinator.class.getName());
  private static final int NO_SUBMITTED_TASKS = 0;

  private final int maxDepth;
  private final List<String> allowedDomains;
  private final PageFetcher pageFetcher;
  private final LinkStatusChecker linkStatusChecker;
  private final DomainFilter domainFilter;
  private final Set<String> visitedUrls;
  private final ExecutorCompletionService<CrawledPage> completionService;

  CrawlCoordinator(
      int maxDepth,
      List<String> allowedDomains,
      PageFetcher pageFetcher,
      LinkStatusChecker linkStatusChecker,
      DomainFilter domainFilter,
      Set<String> visitedUrls,
      ExecutorService executorService
  ) {
    this.maxDepth = maxDepth;
    this.allowedDomains = allowedDomains;
    this.pageFetcher = pageFetcher;
    this.linkStatusChecker = linkStatusChecker;
    this.domainFilter = domainFilter;
    this.visitedUrls = visitedUrls;
    this.completionService = new ExecutorCompletionService<>(executorService);
  }

  List<PageResult> crawl(List<String> startUrls) {
    List<String> rootUrls = new ArrayList<>();
    Map<String, CrawledPage> crawledPages = new HashMap<>();
    Map<String, List<String>> childUrlsByParent = new HashMap<>();
    int pendingTasks = submitStartPages(startUrls, rootUrls);

    while (pendingTasks > 0) {
      CrawledPage crawledPage = waitForCompletedPage();
      pendingTasks--;
      crawledPages.put(crawledPage.url(), crawledPage);
      pendingTasks += submitDiscoveredChildren(crawledPage, childUrlsByParent);
    }

    return buildRootResults(rootUrls, crawledPages, childUrlsByParent);
  }

  private int submitStartPages(List<String> startUrls, List<String> rootUrls) {
    int submittedTasks = 0;

    for (String startUrl : startUrls) {
      submitStartPage(startUrl);
      rootUrls.add(startUrl);
      submittedTasks++;
    }

    return submittedTasks;
  }

  private void submitStartPage(String startUrl) {
    validateAllowedUrl(startUrl);
    if (!tryMarkAsVisited(startUrl)) {
      throw new IllegalArgumentException("URL has already been visited: " + startUrl);
    }
    submitPage(startUrl, maxDepth);
  }

  private int submitDiscoveredChildren(
      CrawledPage crawledPage,
      Map<String, List<String>> childUrlsByParent
  ) {
    if (crawledPage.hasError() || crawledPage.depth() <= 0) {
      return NO_SUBMITTED_TASKS;
    }

    int submittedTasks = NO_SUBMITTED_TASKS;
    for (LinkResult link : crawledPage.links()) {
      if (shouldFollowAndMark(link)) {
        childUrlsByParent
            .computeIfAbsent(crawledPage.url(), url -> new ArrayList<>())
            .add(link.getUrl());
        submitPage(link.getUrl(), crawledPage.depth() - 1);
        submittedTasks++;
      }
    }

    return submittedTasks;
  }

  private void submitPage(String url, int depth) {
    completionService.submit(new CrawlTask(url, depth, pageFetcher, linkStatusChecker));
  }

  private CrawledPage waitForCompletedPage() {
    try {
      return completionService.take().get();
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      LOGGER.log(Level.SEVERE, "Crawler was interrupted", exception);
      throw new IllegalStateException("Crawler was interrupted", exception);
    } catch (ExecutionException exception) {
      LOGGER.log(Level.SEVERE, "Crawler task failed unexpectedly", exception);
      throw new IllegalStateException("Crawler task failed unexpectedly", exception);
    }
  }

  private List<PageResult> buildRootResults(
      List<String> rootUrls,
      Map<String, CrawledPage> crawledPages,
      Map<String, List<String>> childUrlsByParent
  ) {
    List<PageResult> rootResults = new ArrayList<>();

    for (String rootUrl : rootUrls) {
      rootResults.add(buildPageResult(rootUrl, crawledPages, childUrlsByParent));
    }

    return rootResults;
  }

  private PageResult buildPageResult(
      String url,
      Map<String, CrawledPage> crawledPages,
      Map<String, List<String>> childUrlsByParent
  ) {
    CrawledPage crawledPage = crawledPages.get(url);
    if (crawledPage.hasError()) {
      return PageResult.error(crawledPage.url(), crawledPage.depth(), crawledPage.errorMessage());
    }

    List<PageResult> childPages = new ArrayList<>();
    for (String childUrl : childUrlsByParent.getOrDefault(url, List.of())) {
      childPages.add(buildPageResult(childUrl, crawledPages, childUrlsByParent));
    }

    return PageResult.builder(crawledPage.url(), crawledPage.depth())
        .headings(crawledPage.headings())
        .links(crawledPage.links())
        .childPages(childPages)
        .build();
  }

  private void validateAllowedUrl(String url) {
    if (!domainFilter.isAllowed(url, allowedDomains)) {
      throw new IllegalArgumentException("URL is not in an allowed domain: " + url);
    }
  }

  private boolean shouldFollowAndMark(LinkResult link) {
    return !link.isBroken()
        && domainFilter.isAllowed(link.getUrl(), allowedDomains)
        && tryMarkAsVisited(link.getUrl());
  }

  private boolean tryMarkAsVisited(String url) {
    return visitedUrls.add(url);
  }

  static Set<String> createVisitedUrlSet() {
    return ConcurrentHashMap.newKeySet();
  }
}
