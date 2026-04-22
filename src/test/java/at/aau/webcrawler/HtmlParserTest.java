package at.aau.webcrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HtmlParserTest {

  @Test
  void shouldExtractAllHeadings() {
    HtmlParser parser = new HtmlParser();
    Document document = Jsoup.parse("""
                <html>
                    <body>
                        <h1>Main Title</h1>
                        <h2>Section Title</h2>
                        <h3>Subsection Title</h3>
                    </body>
                </html>
                """);

    List<String> headings = parser.extractHeadings(document);

    assertEquals(3, headings.size());
    assertEquals("Main Title", headings.get(0));
    assertEquals("Section Title", headings.get(1));
    assertEquals("Subsection Title", headings.get(2));
  }
}