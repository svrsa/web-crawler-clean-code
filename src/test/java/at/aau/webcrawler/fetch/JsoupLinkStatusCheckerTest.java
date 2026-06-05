package at.aau.webcrawler.fetch;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JsoupLinkStatusCheckerTest {

  @Test
  void shouldTreatNonHttpUrlsAsBroken() {
    LinkStatusChecker checker = new JsoupLinkStatusChecker();

    assertTrue(checker.isBroken("mailto:hello@example.com"));
    assertTrue(checker.isBroken("javascript:void(0)"));
    assertTrue(checker.isBroken(""));
  }

  @Test
  void shouldTreatUnreachableHostAsBroken() {
    LinkStatusChecker checker = new JsoupLinkStatusChecker(500);

    assertTrue(checker.isBroken("http://localhost:1/no-server"));
  }
}
