package at.aau.webcrawler.writer;

import at.aau.webcrawler.model.LinkResult;
import at.aau.webcrawler.model.PageResult;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class MarkdownWriter {

    private static final String REPORT_FILE = "report.md";

    public static void writeReport(PageResult rootPage) {
        try (Writer writer = new FileWriter(REPORT_FILE, StandardCharsets.UTF_8)) {
            writePage(rootPage, 0, writer);
        } catch (IOException e) {
            throw new RuntimeException("Konnte Report nicht schreiben: " + REPORT_FILE, e);
        }
    }

    private static void writePage(PageResult page, int depth, Writer writer) throws IOException {
        // Seiten-URL als Überschrift der Tiefe anpassen
        String indent = "  ".repeat(depth);
        writer.write(indent + "## Page: " + page.getUrl() + "\n\n");

        // Überschriften der Seite
        if (!page.getHeadings().isEmpty()) {
            writer.write(indent + "Headings:\n");
            for (String heading : page.getHeadings()) {
                writer.write(indent + "  - " + heading + "\n");
            }
            writer.write("\n");
        }

        // Links der Seite
        if (!page.getLinks().isEmpty()) {
            writer.write(indent + "Links:\n");
            for (LinkResult link : page.getLinks()) {
                String line = indent + "  - ";
                if (link.isBroken()) {
                    line += "~~broken~~ [" + link.getUrl() + "](" + link.getUrl() + ")";
                } else {
                    line += "[" + link.getUrl() + "](" + link.getUrl() + ")";
                }
                writer.write(line + "\n");
            }
            writer.write("\n");
        }

        // Kindseiten rekursiv ausgeben
        for (PageResult child : page.getChildPages()) {
            writePage(child, depth + 1, writer);
        }
    }
}