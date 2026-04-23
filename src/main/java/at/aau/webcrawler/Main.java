package at.aau.webcrawler;

import org.jsoup.nodes.Document;

import java.util.List;

public class Main {
  public static void main(String[] args) {
    ArgumentParser argumentParser = new ArgumentParser();
    CrawlerConfiguration configuration = argumentParser.parse(args);

    CrawlerService crawlerService = new CrawlerService();
    HtmlParser htmlParser = new HtmlParser();

    Document document = crawlerService.loadDocument(configuration.getStartUrl());

    List<String> headings = htmlParser.extractHeadings(document);
    List<String> links = htmlParser.extractLinks(document);

    System.out.println("URL: " + configuration.getStartUrl());
    System.out.println("Depth: " + configuration.getMaxDepth());
    System.out.println("Domains: " + configuration.getAllowedDomains());
    System.out.println("Headings: " + headings);
    System.out.println("Links: " + links);
  }
}