package at.aau.webcrawler;

import at.aau.webcrawler.model.LinkResult;
import at.aau.webcrawler.model.PageResult;
import at.aau.webcrawler.writer.MarkdownWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkdownWriterTest {

    // AI-assisted: using @TempDir instead of @AfterEach was suggested by AI.
    // AI was also used for the research of @TempDir.
    // The final test structure was manually adapted and validated.
    @TempDir
    Path tempDir;

    private Path reportPath;
    private MarkdownWriter writer;

    @BeforeEach
    void setUp() {
        reportPath = tempDir.resolve("report.md");
        writer = new MarkdownWriter(reportPath);
    }

    @Test
    void shouldCreateReportFile() {
        PageResult root = PageResult.builder("http://example.com", 0)
                .headings(List.of("# Heading 1"))
                .build();

        writer.writeReport(root);

        assertTrue(Files.exists(reportPath), "report.md should be created");
    }

    @Test
    void shouldWritePageUrl() throws IOException {
        PageResult root = PageResult.builder("http://example.com", 0)
                .headings(List.of("# Main Heading"))
                .build();

        writer.writeReport(root);
        String content = Files.readString(reportPath);

        assertTrue(content.contains("<a>[http://example.com](http://example.com)</a>"),
                "Should contain page URL in anchor format");
    }

    @Test
    void shouldWritePageDepth() throws IOException {
        PageResult root = PageResult.builder("http://example.com", 2).build();

        writer.writeReport(root);
        String content = Files.readString(reportPath);

        assertTrue(content.contains("<br>depth: 2"), "Should contain depth label");
    }

    @Test
    void shouldWriteHeadings() throws IOException {
        PageResult root = PageResult.builder("http://example.com", 0)
                .headings(List.of("# Main Heading", "## Sub Heading"))
                .build();

        writer.writeReport(root);
        String content = Files.readString(reportPath);

        assertTrue(content.contains("# Main Heading"), "Should contain h1 heading");
        assertTrue(content.contains("## Sub Heading"), "Should contain h2 heading");
    }

    @Test
    void shouldWriteWorkingLink() throws IOException {
        LinkResult link = new LinkResult("http://example.com/page", false);
        PageResult root = PageResult.builder("http://example.com", 0)
                .links(List.of(link))
                .build();

        writer.writeReport(root);
        String content = Files.readString(reportPath);

        assertTrue(content.contains("link to"), "Should contain 'link to' label for working link");
        assertTrue(content.contains("<a>[http://example.com/page](http://example.com/page)</a>"),
                "Should contain working link in anchor format");
    }

    @Test
    void shouldWriteBrokenLink() throws IOException {
        LinkResult brokenLink = new LinkResult("http://example.com/broken", true);
        PageResult root = PageResult.builder("http://example.com", 0)
                .links(List.of(brokenLink))
                .build();

        writer.writeReport(root);
        String content = Files.readString(reportPath);

        assertTrue(content.contains("broken link"), "Should contain 'broken link' label");
        assertTrue(content.contains("<a>[http://example.com/broken](http://example.com/broken)</a>"),
                "Should contain broken link URL in anchor format");
    }

    @Test
    void shouldNotPrefixRootHeading() throws IOException {
        PageResult child = PageResult.builder("http://example.com/child", 1)
                .headings(List.of("# Child Heading"))
                .build();
        PageResult root = PageResult.builder("http://example.com", 2)
                .headings(List.of("# Root Heading"))
                .childPages(List.of(child))
                .build();

        writer.writeReport(root);
        String content = Files.readString(reportPath);

        assertTrue(content.contains("# Root Heading"),
                "Root heading should have no arrow prefix");
    }

    @Test
    void shouldPrefixNestedPageHeading() throws IOException {
        PageResult child = PageResult.builder("http://example.com/child", 1)
                .headings(List.of("# Child Heading"))
                .build();
        PageResult root = PageResult.builder("http://example.com", 2)
                .childPages(List.of(child))
                .build();

        writer.writeReport(root);
        String content = Files.readString(reportPath);

        assertTrue(content.contains("# --> Child Heading"),
                "Child heading should have --> prefix");
    }

    @Test
    void shouldPrefixDeeplyNestedPageHeading() throws IOException {
        PageResult grandchild = PageResult.builder("http://example.com/grand", 0)
                .headings(List.of("# Grand Heading"))
                .build();
        PageResult child = PageResult.builder("http://example.com/child", 1)
                .headings(List.of("# Child Heading"))
                .childPages(List.of(grandchild))
                .build();
        PageResult root = PageResult.builder("http://example.com", 2)
                .headings(List.of("# Root Heading"))
                .childPages(List.of(child))
                .build();

        writer.writeReport(root);
        String content = Files.readString(reportPath);

        assertTrue(content.contains("# ----> Grand Heading"),
                "Grandchild heading should have ----> prefix");
    }

    @Test
    void shouldWriteErrorBlockForFailedPage() throws IOException {
        PageResult root = PageResult.error("http://example.com/broken", 0, "timeout after 5000ms");

        writer.writeReport(root);
        String content = Files.readString(reportPath);

        assertTrue(content.contains("<a>[http://example.com/broken](http://example.com/broken)</a>"),
                "Error page should still include the URL header");
        assertTrue(content.contains("<br>**error:** timeout after 5000ms"),
                "Error message should be rendered as a clearly marked block");
    }

    @Test
    void shouldStillRenderSiblingsWhenOneChildPageFailed() throws IOException {
        PageResult failingChild = PageResult.error("http://example.com/down", 1, "host unreachable");
        PageResult workingChild = PageResult.builder("http://example.com/ok", 1)
                .headings(List.of("# Ok"))
                .build();
        PageResult root = PageResult.builder("http://example.com", 2)
                .headings(List.of("# Root"))
                .childPages(List.of(failingChild, workingChild))
                .build();

        writer.writeReport(root);
        String content = Files.readString(reportPath);

        assertTrue(content.contains("# Root"), "Root heading must still be present");
        assertTrue(content.contains("<br>**error:** host unreachable"),
                "Failed child page must render its error block");
        assertTrue(content.contains("# --> Ok"),
                "Working sibling must still render with depth prefix");
    }
}
