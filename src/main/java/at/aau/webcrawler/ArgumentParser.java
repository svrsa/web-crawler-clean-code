package at.aau.webcrawler;

import java.util.List;

public class ArgumentParser {
  public CrawlerConfiguration parse(String[] args) {
    if (args.length != 3) {
      throw new IllegalArgumentException("Expected exactly 3 arguments: <url> <depth> <domains>");
    }

    String startUrl = args[0];
    int maxDepth = Integer.parseInt(args[1]);
    List<String> allowedDomains = List.of(args[2].split(","));

    return new CrawlerConfiguration(startUrl, maxDepth, allowedDomains);
  }
}