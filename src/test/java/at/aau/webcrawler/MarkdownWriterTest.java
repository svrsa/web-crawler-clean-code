package at.aau.webcrawler;

import at.aau.webcrawler.model.LinkResult;
import at.aau.webcrawler.model.PageResult;
import at.aau.webcrawler.writer.MarkdownWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkdownWriterTest {

    private Path reportPath;

    @BeforeEach
    void setUp() {
        reportPath = Paths.get("report.md");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(reportPath);
    }

    @Test
    void testReportFileIsCreated() {
        PageResult root = new PageResult(
                "http://example.com",
                0,
                List.of("Heading 1"),
                Collections.emptyList(),
                Collections.emptyList()
        );
        MarkdownWriter.writeReport(root);
        assertTrue(Files.exists(reportPath), "report.md should be created");
    }

    @Test
    void testReportContainsHeadingsAndLinks() throws IOException {
        LinkResult link = new LinkResult("http://example.com/page", false);
        LinkResult brokenLink = new LinkResult("http://example.com/broken", true);

        PageResult root = new PageResult(
                "http://example.com",
                0,
                List.of("Main Heading"),
                List.of(link, brokenLink),
                Collections.emptyList()
        );
        MarkdownWriter.writeReport(root);

        String content = Files.readString(reportPath);

        assertTrue(content.contains("Main Heading"), "Should contain heading text");
        assertTrue(content.contains("[http://example.com/page]"), "Should contain working link");
        assertTrue(content.contains("~~broken~~"), "Should mark broken link");
        assertTrue(content.contains("[http://example.com/broken]"), "Should contain broken link URL");
    }

    @Test
    void testNestedPagesIndentation() throws IOException {
        PageResult child = new PageResult(
                "http://example.com/child",
                1,
                List.of("Child Heading"),
                Collections.emptyList(),
                Collections.emptyList()
        );
        PageResult root = new PageResult(
                "http://example.com",
                0,
                List.of("Root Heading"),
                Collections.emptyList(),
                List.of(child)
        );
        MarkdownWriter.writeReport(root);

        String content = Files.readString(reportPath);

        // Child-URL should appear with indentation
        assertTrue(content.contains("## Page: http://example.com/child"), "Child page should be present");
    }
}