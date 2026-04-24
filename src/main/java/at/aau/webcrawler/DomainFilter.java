package at.aau.webcrawler;

import java.net.URI;
import java.util.List;

public class DomainFilter {

  public boolean isAllowed(String url, List<String> allowedDomains) {
    String host = URI.create(url).getHost();

    if (host == null) {
      return false;
    }

    String normalizedHost = host.toLowerCase();

    for (String allowedDomain : allowedDomains) {
      String normalizedDomain = allowedDomain.trim().toLowerCase();

      if (normalizedHost.equals(normalizedDomain)
          || normalizedHost.endsWith("." + normalizedDomain)) {
        return true;
      }
    }

    return false;
  }
}