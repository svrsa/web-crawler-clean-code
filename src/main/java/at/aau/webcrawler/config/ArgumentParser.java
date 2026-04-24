package at.aau.webcrawler.config;

import java.util.ArrayList;
import java.util.List;

public class ArgumentParser {
  // AI-assisted: the validation structure of this method was discussed with AI.
  // The final implementation was manually adapted and tested.
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
      String trimmedDomain = domainPart.trim();

      if (!trimmedDomain.isEmpty()) {
        allowedDomains.add(trimmedDomain);
      }
    }

    if (allowedDomains.isEmpty()) {
      throw new IllegalArgumentException("At least one allowed domain is required");
    }

    return new CrawlerConfiguration(startUrl, maxDepth, allowedDomains);
  }
}