package at.aau.webcrawler;

import java.util.ArrayList;
import java.util.List;

public class ArgumentParser {
  public CrawlerConfiguration parse(String[] args) {
    if (args.length != 3) {
      throw new IllegalArgumentException("Expected exactly 3 arguments: <url> <depth> <domains>");
    }

    String startUrl = args[0];
    int maxDepth = Integer.parseInt(args[1]);
    if (maxDepth < 0) {
      throw new IllegalArgumentException("Depth must not be negative");
    }

    String[] domainParts = args[2].split(",");
    List<String> allowedDomains = new ArrayList<>();

    for (String domainPart : domainParts) {
      allowedDomains.add(domainPart.trim());
    }

    return new CrawlerConfiguration(startUrl, maxDepth, allowedDomains);
  }
}