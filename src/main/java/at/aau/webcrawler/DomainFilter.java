package at.aau.webcrawler;

import java.net.URI;
import java.util.List;

public class DomainFilter {

  public boolean isAllowed(String url, List<String> allowedDomains) {
    String host = URI.create(url).getHost();

    if (host == null) {
      return false;
    }

    for (String allowedDomain : allowedDomains) {
      if (host.equalsIgnoreCase(allowedDomain.trim())) {
        return true;
      }
    }

    return false;
  }
}