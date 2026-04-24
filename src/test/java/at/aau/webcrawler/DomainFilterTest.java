package at.aau.webcrawler;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainFilterTest {

  @Test
  void shouldAllowUrlFromAllowedDomain() {
    DomainFilter domainFilter = new DomainFilter();

    boolean allowed = domainFilter.isAllowed(
        "https://example.com/page",
        List.of("example.com", "example.org")
    );

    assertTrue(allowed);
  }

  @Test
  void shouldRejectUrlFromDisallowedDomain() {
    DomainFilter domainFilter = new DomainFilter();

    boolean allowed = domainFilter.isAllowed(
        "https://google.com/page",
        List.of("example.com", "example.org")
    );

    assertFalse(allowed);
  }

  @Test
  void shouldAllowSubdomainOfAllowedDomain() {
    DomainFilter domainFilter = new DomainFilter();

    boolean allowed = domainFilter.isAllowed(
        "https://www.example.com/page",
        List.of("example.com")
    );

    assertTrue(allowed);
  }
}