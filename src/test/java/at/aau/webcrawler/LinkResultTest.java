package at.aau.webcrawler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LinkResultTest {

  @Test
  void shouldStoreLinkData() {
    LinkResult linkResult = new LinkResult("https://example.com", false);

    assertEquals("https://example.com", linkResult.getUrl());
    assertFalse(linkResult.isBroken());
  }
}