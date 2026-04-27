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

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integrationstest, der einen lokalen HTTP-Server startet,
 * um den Crawler in einer kontrollierten Umgebung zu testen.
 */
public class WebCrawlerIntegrationTest {

    private HttpServer server;
    private static final String HTML_PAGE = """
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
        server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();

        // Root-Seite
        server.createContext("/", exchange -> {
            String response = String.format(HTML_PAGE, port, port);
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        });

        // Child-Seite
        server.createContext("/child", exchange -> {
            byte[] response = HTML_CHILD.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        });

        // Broken-Link-Seite liefert 404
        server.createContext("/broken", exchange -> {
            String response = "Not Found";
            exchange.sendResponseHeaders(404, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        });

        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void testCrawlAndGenerateReport() throws Exception {
        int port = server.getAddress().getPort();
        String startUrl = "http://localhost:" + port + "/";

        // ArgumentParser wiederverwenden
        ArgumentParser parser = new ArgumentParser();
        CrawlerConfiguration config = parser.parse(new String[]{
                startUrl, "2", "localhost"
        });

        CrawlerService crawler = new CrawlerService();
        PageResult root = crawler.crawlPage(
                config.getStartUrl(),
                config.getMaxDepth(),
                config.getAllowedDomains()
        );

        // Report schreiben
        MarkdownWriter.writeReport(root);
        Path reportPath = Paths.get("report.md");
        assertTrue(Files.exists(reportPath), "report.md muss existieren");

        String content = Files.readString(reportPath);

        // Inhalt prüfen
        assertTrue(content.contains("Welcome"), "Root Heading soll enthalten sein");
        assertTrue(content.contains("Child Heading"), "Child Heading soll enthalten sein");
        assertTrue(content.contains("~~broken~~"), "Defekter Link soll markiert sein");
    }
}