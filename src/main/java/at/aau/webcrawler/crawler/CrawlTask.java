package at.aau.webcrawler.crawler;

import at.aau.webcrawler.fetch.LinkStatusChecker;
import at.aau.webcrawler.fetch.PageContent;
import at.aau.webcrawler.fetch.PageFetcher;
import at.aau.webcrawler.fetch.PageLoadException;
import at.aau.webcrawler.model.LinkResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CrawlTask implements Callable<CrawledPage> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CrawlTask.class);

  private final String url;
  private final int depth;
  private final PageFetcher pageFetcher;
  private final LinkStatusChecker linkStatusChecker;

  CrawlTask(
      String url,
      int depth,
      PageFetcher pageFetcher,
      LinkStatusChecker linkStatusChecker
  ) {
    this.url = url;
    this.depth = depth;
    this.pageFetcher = pageFetcher;
    this.linkStatusChecker = linkStatusChecker;
  }

  @Override
  public CrawledPage call() {
    try {
      PageContent content = pageFetcher.fetch(url);
      List<LinkResult> links = analyzeLinks(content.links());
      return CrawledPage.success(url, depth, content.headings(), links);
    } catch (PageLoadException exception) {
      LOGGER.error("Could not crawl page: {}", url, exception);
      return CrawledPage.error(url, depth, exception.getMessage());
    }
  }

  private List<LinkResult> analyzeLinks(List<String> extractedLinks) {
    List<LinkResult> links = new ArrayList<>();
    Set<String> uniqueLinks = new HashSet<>();

    for (String extractedLink : extractedLinks) {
      if (uniqueLinks.add(extractedLink)) {
        boolean broken = linkStatusChecker.isBroken(extractedLink);
        links.add(new LinkResult(extractedLink, broken));
      }
    }

    return links;
  }
}
