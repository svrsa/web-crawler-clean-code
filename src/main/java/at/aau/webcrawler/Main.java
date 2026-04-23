package at.aau.webcrawler;

public class Main {
  public static void main(String[] args) {
    ArgumentParser argumentParser = new ArgumentParser();
    CrawlerConfiguration configuration = argumentParser.parse(args);

    CrawlerService crawlerService = new CrawlerService();
    PageResult pageResult = crawlerService.analyzePage(
        configuration.getStartUrl(),
        configuration.getMaxDepth(),
        configuration.getAllowedDomains()
    );

    System.out.println("URL: " + pageResult.getUrl());
    System.out.println("Depth: " + pageResult.getDepth());
    System.out.println("Headings: " + pageResult.getHeadings());

    System.out.println("Links:");
    for (LinkResult link : pageResult.getLinks()) {
      System.out.println("- " + link.getUrl() + " (broken: " + link.isBroken() + ")");
    }
  }
}