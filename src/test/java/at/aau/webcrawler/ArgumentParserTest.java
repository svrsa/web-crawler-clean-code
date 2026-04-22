package at.aau.webcrawler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArgumentParserTest {

  @Test
  void shouldParseValidArguments() {
    ArgumentParser parser = new ArgumentParser();
    String[] args = {"https://example.com", "2", "example.com,example.org"};

    CrawlerConfiguration configuration = parser.parse(args);

    assertEquals("https://example.com", configuration.getStartUrl());
    assertEquals(2, configuration.getMaxDepth());
    assertEquals(2, configuration.getAllowedDomains().size());
  }

  @Test
  void shouldThrowExceptionWhenArgumentCountIsInvalid() {
    ArgumentParser parser = new ArgumentParser();
    String[] args = {"https://example.com", "2"};

    assertThrows(IllegalArgumentException.class, () -> parser.parse(args));
  }
}