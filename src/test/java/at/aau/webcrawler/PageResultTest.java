package at.aau.webcrawler;

import at.aau.webcrawler.model.PageResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

  @Test
  void shouldHaveNoErrorByDefault() {
    PageResult pageResult = PageResult.builder("https://example.com", 0).build();

    assertFalse(pageResult.hasError());
  }

  @Test
  void shouldExposeErrorViaErrorFactory() {
    PageResult pageResult = PageResult.error("https://example.com", 2, "timeout after 5000ms");

    assertTrue(pageResult.hasError());
    assertEquals("timeout after 5000ms", pageResult.getErrorMessage());
    assertEquals("https://example.com", pageResult.getUrl());
    assertEquals(2, pageResult.getDepth());
    assertTrue(pageResult.getHeadings().isEmpty());
    assertTrue(pageResult.getLinks().isEmpty());
    assertTrue(pageResult.getChildPages().isEmpty());
  }
}
