package at.aau.webcrawler;

public class LinkResult {
  private final String url;
  private final boolean broken;

  public LinkResult(String url, boolean broken) {
    this.url = url;
    this.broken = broken;
  }

  public String getUrl() {
    return url;
  }

  public boolean isBroken() {
    return broken;
  }
}