package at.aau.webcrawler.config;

import java.util.ArrayList;
import java.util.List;

public class ArgumentParser {
  private static final int MIN_ARGUMENT_COUNT = 3;
  private static final int MAX_ARGUMENT_COUNT = 4;
  private static final int URL_ARGUMENT_INDEX = 0;
  private static final int DEPTH_ARGUMENT_INDEX = 1;
  private static final int DOMAINS_ARGUMENT_INDEX = 2;
  private static final int THREAD_COUNT_ARGUMENT_INDEX = 3;

  // AI-assisted: the validation structure of this method was discussed with AI.
  // The final implementation was manually adapted and tested.
  public CrawlerConfiguration parse(String[] args) {
    if (args.length < MIN_ARGUMENT_COUNT || args.length > MAX_ARGUMENT_COUNT) {
      throw new IllegalArgumentException("Expected 3 or 4 arguments: <urls> <depth> <domains> [threads]");
    }

    List<String> startUrls = parseCommaSeparatedValues(args[URL_ARGUMENT_INDEX]);
    int maxDepth = parseDepth(args[DEPTH_ARGUMENT_INDEX]);
    List<String> allowedDomains = parseCommaSeparatedValues(args[DOMAINS_ARGUMENT_INDEX]);
    int threadCount = parseThreadCount(args);

    if (startUrls.isEmpty()) {
      throw new IllegalArgumentException("At least one start URL is required");
    }

    if (allowedDomains.isEmpty()) {
      throw new IllegalArgumentException("At least one allowed domain is required");
    }

    return new CrawlerConfiguration(startUrls, maxDepth, allowedDomains, threadCount);
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

  private List<String> parseCommaSeparatedValues(String argument) {
    String[] parts = argument.split(",");
    List<String> values = new ArrayList<>();

    for (String part : parts) {
      String trimmedValue = part.trim();

      if (!trimmedValue.isEmpty()) {
        values.add(trimmedValue);
      }
    }

    return values;
  }

  private int parseThreadCount(String[] args) {
    if (args.length == MIN_ARGUMENT_COUNT) {
      return CrawlerDefaults.THREAD_COUNT;
    }

    try {
      int threadCount = Integer.parseInt(args[THREAD_COUNT_ARGUMENT_INDEX]);
      if (threadCount < CrawlerDefaults.MINIMUM_THREAD_COUNT) {
        throw new IllegalArgumentException("Thread count must be positive");
      }
      return threadCount;
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException("Thread count must be a valid integer", exception);
    }
  }
}
