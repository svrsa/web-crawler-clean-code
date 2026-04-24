package at.aau.webcrawler;

import at.aau.webcrawler.config.ArgumentParser;
import at.aau.webcrawler.config.CrawlerConfiguration;
import at.aau.webcrawler.crawler.CrawlerService;
import at.aau.webcrawler.model.LinkResult;
import at.aau.webcrawler.model.PageResult;

public class Main {
  public static void main(String[] args) {
    ArgumentParser argumentParser = new ArgumentParser();
    CrawlerConfiguration configuration = argumentParser.parse(args);

    CrawlerService crawlerService = new CrawlerService();
    PageResult pageResult = crawlerService.crawlPage(
        configuration.getStartUrl(),
        configuration.getMaxDepth(),
        configuration.getAllowedDomains()
    );

    printPageResult(pageResult);
  }

  private static void printPageResult(PageResult pageResult) {
    System.out.println("URL: " + pageResult.getUrl());
    System.out.println("Depth: " + pageResult.getDepth());
    System.out.println("Headings: " + pageResult.getHeadings());

    System.out.println("Links:");
    for (LinkResult link : pageResult.getLinks()) {
      System.out.println("- " + link.getUrl() + " (broken: " + link.isBroken() + ")");
    }

    System.out.println();

    for (PageResult childPage : pageResult.getChildPages()) {
      printPageResult(childPage);
    }
  }
}