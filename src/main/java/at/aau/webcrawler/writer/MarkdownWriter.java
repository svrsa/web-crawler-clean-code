package at.aau.webcrawler.writer;

import at.aau.webcrawler.model.LinkResult;
import at.aau.webcrawler.model.PageResult;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MarkdownWriter {

    private static final String REPORT_FILE = "report.md";

    private final Path outputPath;

    public MarkdownWriter() {
        this(Path.of(REPORT_FILE));
    }

    // AI-assisted: the constructor overload for testability via path injection (DIP) was suggested by AI.
    // The final design and integration were made manually.
    public MarkdownWriter(Path outputPath) {
        this.outputPath = outputPath;
    }

    public void writeReport(PageResult rootPage) {
        writeReport(List.of(rootPage));
    }

    public void writeReport(List<PageResult> rootPages) {
        try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            for (int index = 0; index < rootPages.size(); index++) {
                if (index > 0) {
                    writer.write("\n");
                }
                PageResult rootPage = rootPages.get(index);
                writePage(rootPage, rootPage.getDepth(), writer);
            }
        } catch (IOException e) {
            throw new ReportWriteException("Could not write report to: " + outputPath, e);
        }
    }

    // AI-assisted: the page-writing structure and method structure were discussed with AI.
    // The final implementation was manually adapted and tested.
    private void writePage(PageResult page, int rootDepth, Writer writer) throws IOException {
        writePageHeader(page, writer);

        if (page.hasError()) {
            writeError(page, writer);
            return;
        }

        String arrowPrefix = buildArrowPrefix(rootDepth, page.getDepth());
        writeHeadings(page, arrowPrefix, writer);
        writeLinks(page, arrowPrefix, writer);
        writeChildPages(page, rootDepth, writer);
    }

    private void writePageHeader(PageResult page, Writer writer) throws IOException {
        writer.write("<a>[" + page.getUrl() + "](" + page.getUrl() + ")</a>\n");
        writer.write("<br>depth: " + page.getDepth() + "\n");
    }

    private void writeError(PageResult page, Writer writer) throws IOException {
        writer.write("<br>**error:** " + page.getErrorMessage() + "\n");
    }

    private void writeHeadings(PageResult page, String arrowPrefix, Writer writer) throws IOException {
        for (String heading : page.getHeadings()) {
            writer.write(applyArrowPrefixToHeading(heading, arrowPrefix) + "\n");
        }
    }

    private void writeLinks(PageResult page, String arrowPrefix, Writer writer) throws IOException {
        for (LinkResult link : page.getLinks()) {
            writer.write("\n");
            writer.write(formatLink(link, arrowPrefix) + "\n");
        }
    }

    private void writeChildPages(PageResult page, int rootDepth, Writer writer) throws IOException {
        for (PageResult child : page.getChildPages()) {
            writer.write("\n");
            writePage(child, rootDepth, writer);
        }
    }

    private String formatLink(LinkResult link, String arrowPrefix) {
        String label = link.isBroken() ? "broken link" : "link to";
        String anchor = "<a>[" + link.getUrl() + "](" + link.getUrl() + ")</a>";
        return "<br>" + arrowPrefix + label + " " + anchor;
    }

    private String buildArrowPrefix(int rootDepth, int currentDepth) {
        int levels = rootDepth - currentDepth;
        if (levels <= 0) return "";
        return "--".repeat(levels) + "> ";
    }

    private String applyArrowPrefixToHeading(String heading, String arrowPrefix) {
        if (arrowPrefix.isEmpty()) {
            return heading;
        }
        int spaceIndex = heading.indexOf(' ');
        if (spaceIndex == -1) {
            return heading;
        }
        String hashes = heading.substring(0, spaceIndex + 1);
        String text = heading.substring(spaceIndex + 1);
        return hashes + arrowPrefix + text;
    }
}
