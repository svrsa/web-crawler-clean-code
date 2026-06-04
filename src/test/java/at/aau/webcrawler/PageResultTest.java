package at.aau.webcrawler;

import at.aau.webcrawler.model.PageResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageResultTest {

  @Test
  void shouldStorePageMetadata() {
    PageResult pageResult = PageResult.builder("https://example.com", 1)
        .headings(List.of("Main Heading"))
        .build();

    assertEquals("https://example.com", pageResult.getUrl());
    assertEquals(1, pageResult.getDepth());
    assertEquals(List.of("Main Heading"), pageResult.getHeadings());
  }

  @Test
  void shouldStoreChildPages() {
    PageResult childPage = PageResult.builder("https://example.com/child", 0).build();
    PageResult parentPage = PageResult.builder("https://example.com", 1)
        .childPages(List.of(childPage))
        .build();

    assertEquals(1, parentPage.getChildPages().size());
    assertEquals("https://example.com/child", parentPage.getChildPages().get(0).getUrl());
  }
}
