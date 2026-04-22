package at.aau.webcrawler;

public class Main {
  public static void main(String[] args) {
    ArgumentParser argumentParser = new ArgumentParser();
    CrawlerConfiguration configuration = argumentParser.parse(args);

    System.out.println("URL: " + configuration.getStartUrl());
    System.out.println("Depth: " + configuration.getMaxDepth());
    System.out.println("Domains: " + configuration.getAllowedDomains());
  }
}