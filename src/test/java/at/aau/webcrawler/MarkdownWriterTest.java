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
import java.util.Collections;
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
    void testReportFileIsCreated() {
        PageResult root = new PageResult(
                "http://example.com",
                0,
                List.of("# Heading 1"),
                Collections.emptyList(),
                Collections.emptyList()
        );

        writer.writeReport(root);

        assertTrue(Files.exists(reportPath), "report.md should be created");
    }

    @Test
    void testReportContainsUrl() throws IOException {
        PageResult root = new PageResult(
                "http://example.com",
                0,
                List.of("# Main Heading"),
                Collections.emptyList(),
                Collections.emptyList()
        );

        writer.writeReport(root);
        String content = Files.readString(reportPath);

        assertTrue(content.contains("<a>[http://example.com](http://example.com)</a>"),
                "Should contain page URL in anchor format");
    }

    @Test
    void testReportContainsDepth() throws IOException {
        PageResult root = new PageResult(
                "http://example.com",
                2,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        writer.writeReport(root);
        String content = Files.readString(reportPath);

        assertTrue(content.contains("<br>depth: 2"), "Should contain depth label");
    }

    @Test
    void testReportContainsHeadings() throws IOException {
        PageResult root = new PageResult(
                "http://example.com",
                0,
                List.of("# Main Heading", "## Sub Heading"),
                Collections.emptyList(),
                Collections.emptyList()
        );

        writer.writeReport(root);
        String content = Files.readString(reportPath);

        assertTrue(content.contains("# Main Heading"), "Should contain h1 heading");
        assertTrue(content.contains("## Sub Heading"), "Should contain h2 heading");
    }

    @Test
    void testReportContainsWorkingLink() throws IOException {
        LinkResult link = new LinkResult("http://example.com/page", false);
        PageResult root = new PageResult(
                "http://example.com",
                0,
                Collections.emptyList(),
                List.of(link),
                Collections.emptyList()
        );

        writer.writeReport(root);
        String content = Files.readString(reportPath);

        assertTrue(content.contains("link to"), "Should contain 'link to' label for working link");
        assertTrue(content.contains("<a>[http://example.com/page](http://example.com/page)</a>"),
                "Should contain working link in anchor format");
    }

    @Test
    void testReportContainsBrokenLink() throws IOException {
        LinkResult brokenLink = new LinkResult("http://example.com/broken", true);
        PageResult root = new PageResult(
                "http://example.com",
                0,
                Collections.emptyList(),
                List.of(brokenLink),
                Collections.emptyList()
        );

        writer.writeReport(root);
        String content = Files.readString(reportPath);

        assertTrue(content.contains("broken link"), "Should contain 'broken link' label");
        assertTrue(content.contains("<a>[http://example.com/broken](http://example.com/broken)</a>"),
                "Should contain broken link URL in anchor format");
    }

    @Test
    void testNestedPageHasArrowPrefix() throws IOException {
        // depth: root starts at 2 , child at 1
        PageResult child = new PageResult(
                "http://example.com/child",
                1,
                List.of("# Child Heading"),
                Collections.emptyList(),
                Collections.emptyList()
        );
        PageResult root = new PageResult(
                "http://example.com",
                2,
                List.of("# Root Heading"),
                Collections.emptyList(),
                List.of(child)
        );

        writer.writeReport(root);
        String content = Files.readString(reportPath);

        assertTrue(content.contains("# Root Heading"),
                "Root heading should have no arrow prefix");
        assertTrue(content.contains("# --> Child Heading"),
                "Child heading should have --> prefix");
    }

    @Test
    void testDeeplyNestedPageHasDoubleArrowPrefix() throws IOException {
        // (depth 2→1→0), so prefix is "---->"
        PageResult grandchild = new PageResult(
                "http://example.com/grand",
                0,
                List.of("# Grand Heading"),
                Collections.emptyList(),
                Collections.emptyList()
        );
        PageResult child = new PageResult(
                "http://example.com/child",
                1,
                List.of("# Child Heading"),
                Collections.emptyList(),
                List.of(grandchild)
        );
        PageResult root = new PageResult(
                "http://example.com",
                2,
                List.of("# Root Heading"),
                Collections.emptyList(),
                List.of(child)
        );

        writer.writeReport(root);
        String content = Files.readString(reportPath);

        assertTrue(content.contains("# ----> Grand Heading"),
                "Grandchild heading should have ----> prefix");
    }
}