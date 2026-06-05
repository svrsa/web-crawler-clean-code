package at.aau.webcrawler;

import at.aau.webcrawler.config.ArgumentParser;
import at.aau.webcrawler.config.CrawlerConfiguration;
import at.aau.webcrawler.crawler.WebCrawler;
import at.aau.webcrawler.model.LinkResult;
import at.aau.webcrawler.model.PageResult;
import at.aau.webcrawler.writer.MarkdownWriter;

import java.util.ArrayList;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    ArgumentParser argumentParser = new ArgumentParser();
    CrawlerConfiguration configuration = argumentParser.parse(args);

    WebCrawler webCrawler = new WebCrawler(
        configuration.getMaxDepth(),
        configuration.getAllowedDomains()
    );
    List<PageResult> pageResults = new ArrayList<>();
    for (String startUrl : configuration.getStartUrls()) {
      pageResults.add(webCrawler.crawl(startUrl));
    }

    new MarkdownWriter().writeReport(pageResults);
    System.out.println("Report wurde als report.md gespeichert.");
    for (PageResult pageResult : pageResults) {
      printPageResult(pageResult);
    }
  }

  private static void printPageResult(PageResult pageResult) {
    System.out.println("URL: " + pageResult.getUrl());
    System.out.println("Depth: " + pageResult.getDepth());

    if (pageResult.hasError()) {
      System.out.println("Error: " + pageResult.getErrorMessage());
      System.out.println();
      return;
    }

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
