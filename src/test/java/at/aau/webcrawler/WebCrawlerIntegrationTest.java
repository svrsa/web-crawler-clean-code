package at.aau.webcrawler;

import at.aau.webcrawler.config.ArgumentParser;
import at.aau.webcrawler.config.CrawlerConfiguration;
import at.aau.webcrawler.crawler.CrawlerService;
import at.aau.webcrawler.model.PageResult;
import at.aau.webcrawler.writer.MarkdownWriter;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test that starts a local HTTP server to test the full crawl-to-report
 * pipeline in a controlled environment.
 */
public class WebCrawlerIntegrationTest {

    // AI-assisted: using @TempDir for isolated report output was suggested by AI.
    // The final integration into the test setup was manually implemented and reviewed.
    @TempDir
    Path tempDir;

    private HttpServer server;
    private int port;

    private static final String HTML_ROOT = """
            <html>
              <head><title>Test Page</title></head>
              <body>
                <h1>Welcome</h1>
                <a href="http://localhost:%d/child">Child Page</a>
                <a href="http://localhost:%d/broken">Broken Link</a>
              </body>
            </html>
            """;

    private static final String HTML_CHILD = """
            <html>
              <head><title>Child</title></head>
              <body>
                <h2>Child Heading</h2>
              </body>
            </html>
            """;

    @BeforeEach
    void startServer() throws IOException {
        // AI-assisted: the local HttpServer setup with dynamic port assignment
        // was discussed with AI. The handler structure was manually implemented and tested.
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();

        server.createContext("/", exchange -> {
            byte[] response = String.format(HTML_ROOT, port, port)
                    .getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        server.createContext("/child", exchange -> {
            byte[] response = HTML_CHILD.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        server.createContext("/broken", exchange -> {
            byte[] response = "Not Found".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(404, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
        // tempDir and its contents are cleaned up automatically by @TempDir
    }

    @Test
    void testCrawlAndGenerateReport() throws IOException {
        String startUrl = "http://localhost:" + port + "/";

        CrawlerConfiguration config = new ArgumentParser().parse(
                new String[]{startUrl, "2", "localhost"}
        );

        PageResult root = new CrawlerService().crawlPage(
                config.getStartUrl(),
                config.getMaxDepth(),
                config.getAllowedDomains()
        );

        Path reportPath = tempDir.resolve("report.md");
        new MarkdownWriter(reportPath).writeReport(root);

        assertTrue(Files.exists(reportPath), "report.md must exist");

        String content = Files.readString(reportPath);

        // Root URL rendered as anchor
        assertTrue(content.contains("<a>[" + startUrl + "](" + startUrl + ")</a>"),
                "Root URL should be present in anchor format");

        // Depth label present
        assertTrue(content.contains("<br>depth:"),
                "Depth label should be present");

        // Root heading — no arrow prefix at root level
        assertTrue(content.contains("# Welcome"),
                "Root h1 heading should be present without arrow");

        // Child heading — one level deep, so prefixed with "-->"
        assertTrue(content.contains("## --> Child Heading"),
                "Child h2 heading should appear with --> prefix");

        // Broken link uses "broken link" label
        assertTrue(content.contains("broken link"),
                "Broken link should be labeled 'broken link'");

        // Working link uses "link to" label
        assertTrue(content.contains("link to"),
                "Working link should be labeled 'link to'");
    }
}