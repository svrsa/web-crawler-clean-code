package at.aau.webcrawler;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageResultTest {

  @Test
  void shouldStoreChildPages() {
    PageResult childPage = new PageResult(
        "https://example.com/child",
        0,
        List.of("Child Heading"),
        List.of(),
        List.of()
    );

    PageResult parentPage = new PageResult(
        "https://example.com",
        1,
        List.of("Main Heading"),
        List.of(),
        List.of(childPage)
    );

    assertEquals("https://example.com", parentPage.getUrl());
    assertEquals(1, parentPage.getDepth());
    assertEquals(1, parentPage.getChildPages().size());
    assertEquals("https://example.com/child", parentPage.getChildPages().get(0).getUrl());
  }
}