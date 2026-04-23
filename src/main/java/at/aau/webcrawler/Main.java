package at.aau.webcrawler;

import java.util.List;

public class Main {
  public static void main(String[] args) {
    ArgumentParser argumentParser = new ArgumentParser();
    CrawlerConfiguration configuration = argumentParser.parse(args);

    CrawlerService crawlerService = new CrawlerService();
    List<PageResult> results = crawlerService.crawl(
        configuration.getStartUrl(),
        configuration.getMaxDepth(),
        configuration.getAllowedDomains()
    );

    for (PageResult pageResult : results) {
      System.out.println("URL: " + pageResult.getUrl());
      System.out.println("Depth: " + pageResult.getDepth());
      System.out.println("Headings: " + pageResult.getHeadings());

      System.out.println("Links:");
      for (LinkResult link : pageResult.getLinks()) {
        System.out.println("- " + link.getUrl() + " (broken: " + link.isBroken() + ")");
      }

      System.out.println();
    }
  }
}