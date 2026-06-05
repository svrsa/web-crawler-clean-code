package at.aau.webcrawler;

import at.aau.webcrawler.config.ArgumentParser;
import at.aau.webcrawler.config.CrawlerConfiguration;
import at.aau.webcrawler.crawler.WebCrawler;
import at.aau.webcrawler.model.LinkResult;
import at.aau.webcrawler.model.PageResult;
import at.aau.webcrawler.writer.MarkdownWriter;

import java.util.List;
import java.util.logging.Logger;

public class Main {
  private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

  public static void main(String[] args) {
    ArgumentParser argumentParser = new ArgumentParser();
    CrawlerConfiguration configuration = argumentParser.parse(args);

    WebCrawler webCrawler = new WebCrawler(
        configuration.getMaxDepth(),
        configuration.getAllowedDomains()
    );
    List<PageResult> pageResults = webCrawler.crawl(configuration.getStartUrls());

    new MarkdownWriter().writeReport(pageResults);
    LOGGER.info("Report wurde als report.md gespeichert.");
    for (PageResult pageResult : pageResults) {
      printPageResult(pageResult);
    }
  }

  private static void printPageResult(PageResult pageResult) {
    LOGGER.info("URL: " + pageResult.getUrl());
    LOGGER.info("Depth: " + pageResult.getDepth());

    if (pageResult.hasError()) {
      LOGGER.info("Error: " + pageResult.getErrorMessage());
      return;
    }

    LOGGER.info("Headings: " + pageResult.getHeadings());

    for (LinkResult link : pageResult.getLinks()) {
      LOGGER.info("Link: " + link.getUrl() + " (broken: " + link.isBroken() + ")");
    }

    for (PageResult childPage : pageResult.getChildPages()) {
      printPageResult(childPage);
    }
  }
}
