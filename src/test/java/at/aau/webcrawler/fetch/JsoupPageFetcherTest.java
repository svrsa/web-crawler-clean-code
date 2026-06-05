package at.aau.webcrawler.fetch;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsoupPageFetcherTest {

  private HttpServer server;
  private int port;

  // AI-assisted: the embedded HttpServer setup with dynamic port assignment
  // was discussed with AI. The handler structure was manually implemented and tested.
  @BeforeEach
  void startServer() throws IOException {
    server = HttpServer.create(new InetSocketAddress(0), 0);
    port = server.getAddress().getPort();
    server.start();
  }

  @AfterEach
  void stopServer() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void shouldReturnHeadingsAndAbsoluteLinks() throws PageLoadException {
    serve("/", """
        <html>
          <body>
            <h1>Main Title</h1>
            <h2>Section Title</h2>
            <a href="/relative">Relative</a>
            <a href="https://other.com/abs">Absolute</a>
          </body>
        </html>
        """);

    PageContent content = new JsoupPageFetcher().fetch(baseUrl());

    assertEquals(List.of("# Main Title", "## Section Title"), content.headings());
    assertEquals(2, content.links().size());
    assertTrue(content.links().contains("http://localhost:" + port + "/relative"),
        "Relative href should be resolved against the base URL");
    assertTrue(content.links().contains("https://other.com/abs"));
  }

  @Test
  void shouldPrefixAllHeadingLevels() throws PageLoadException {
    serve("/", """
        <html><body>
          <h1>A</h1>
          <h2>B</h2>
          <h3>C</h3>
          <h4>D</h4>
          <h5>E</h5>
          <h6>F</h6>
        </body></html>
        """);

    PageContent content = new JsoupPageFetcher().fetch(baseUrl());

    assertEquals(
        List.of("# A", "## B", "### C", "#### D", "##### E", "###### F"),
        content.headings()
    );
  }

  @Test
  void shouldThrowPageLoadExceptionWhenHostUnreachable() {
    PageFetcher pageFetcher = new JsoupPageFetcher(500);

    assertThrows(
        PageLoadException.class,
        () -> pageFetcher.fetch("http://localhost:1/no-server")
    );
  }

  private void serve(String path, String html) {
    server.createContext(path, exchange -> {
      byte[] response = html.getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, response.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(response);
      }
    });
  }

  private String baseUrl() {
    return "http://localhost:" + port + "/";
  }
}
