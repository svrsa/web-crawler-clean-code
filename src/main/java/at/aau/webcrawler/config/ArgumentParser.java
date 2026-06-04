package at.aau.webcrawler.config;

import java.util.ArrayList;
import java.util.List;

public class ArgumentParser {
  private static final int EXPECTED_ARGUMENT_COUNT = 3;
  private static final int URL_ARGUMENT_INDEX = 0;
  private static final int DEPTH_ARGUMENT_INDEX = 1;
  private static final int DOMAINS_ARGUMENT_INDEX = 2;

  // AI-assisted: the validation structure of this method was discussed with AI.
  // The final implementation was manually adapted and tested.
  public CrawlerConfiguration parse(String[] args) {
    if (args.length != EXPECTED_ARGUMENT_COUNT) {
      throw new IllegalArgumentException("Expected exactly 3 arguments: <url> <depth> <domains>");
    }

    String startUrl = args[URL_ARGUMENT_INDEX];
    int maxDepth = parseDepth(args[DEPTH_ARGUMENT_INDEX]);

    String[] domainParts = args[DOMAINS_ARGUMENT_INDEX].split(",");
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

  private int parseDepth(String depthArgument) {
    try {
      int maxDepth = Integer.parseInt(depthArgument);

      if (maxDepth < 0) {
        throw new IllegalArgumentException("Depth must not be negative");
      }

      return maxDepth;
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException("Depth must be a valid integer", exception);
    }
  }
}
