# Web Crawler – Clean Code Assignment

A Java-based web crawler that creates a compact markdown overview of a given website and recursively linked websites by recording headings and links.

## Overview

The goal of this project is to implement a command-line web crawler in Java for the Clean Code assignment.

The crawler starts from one or more given URLs, extracts headings and links from each website, and follows allowed links up to a specified depth. Page loads are processed concurrently with a configurable thread pool. The final result is written into a single markdown report file.

The project focuses on clean code, maintainability, and automated testing.

## Project Goals

The crawler should:

- accept start URL(s), crawl depth, allowed domain(s), and an optional thread count as command-line arguments
- extract headings (`h1` to `h6`)
- extract links (`a[href]`)
- recursively crawl linked pages within the allowed depth
- process pages concurrently with a fixed-size thread pool
- only follow links that belong to one of the allowed domains
- avoid visiting the same page more than once
- detect broken links
- generate a compact markdown report
- include automated unit tests for the implemented features

## Tech Stack

- Java 17
- Maven
- jsoup
- JUnit 5
- IntelliJ IDEA
- GitHub

## Repository Structure

```text
.
├── src/
│   ├── main/
│   │   └── java/
│   │       └── at/aau/webcrawler/
│   │           ├── Main.java
│   │           ├── config/
│   │           │   ├── ArgumentParser.java
│   │           │   └── CrawlerConfiguration.java
│   │           ├── crawler/
│   │           │   ├── WebCrawler.java
│   │           │   ├── CrawlCoordinator.java
│   │           │   ├── CrawlTask.java
│   │           │   ├── CrawledPage.java
│   │           │   └── DomainFilter.java
│   │           ├── fetch/
│   │           │   ├── PageFetcher.java
│   │           │   ├── PageContent.java
│   │           │   ├── LinkStatusChecker.java
│   │           │   ├── PageLoadException.java
│   │           │   ├── JsoupPageFetcher.java
│   │           │   └── JsoupLinkStatusChecker.java
│   │           ├── model/
│   │           │   ├── LinkResult.java
│   │           │   └── PageResult.java
│   │           └── writer/
│   │               ├── MarkdownWriter.java
│   │               └── ReportWriteException.java
│   └── test/
│       └── java/
│           └── at/aau/webcrawler/
│               ├── ArgumentParserTest.java
│               ├── DomainFilterTest.java
│               ├── LinkResultTest.java
│               ├── MarkdownWriterTest.java
│               ├── PageResultTest.java
│               ├── WebCrawlerIntegrationTest.java
│               ├── WebCrawlerTest.java
│               └── fetch/
│                   ├── JsoupPageFetcherTest.java
│                   └── JsoupLinkStatusCheckerTest.java
├── pom.xml
└── README.md
```

## Components

- **`Main`**  
  Entry point of the application. Wires together configuration, crawling, and report writing.
  
- **`ArgumentParser`**  
  Parses and validates command-line arguments (URL, depth, allowed domains).
  
- **`CrawlerConfiguration`**  
  Immutable value object holding the parsed crawler settings.
  
- **`WebCrawler`**  
  Holds the crawl configuration and starts the concurrent crawl with a fixed-size thread pool.
  The default thread count is `8`, but it can be overridden via the optional CLI argument.

- **`CrawlCoordinator`**  
  Coordinates submitted crawl tasks, tracks pending work, submits discovered child pages, and reconstructs the final parent/child tree after all tasks have completed.

- **`CrawlTask`**  
  Processes one page: fetches page content, extracts link status, records page-load errors, and returns a library-agnostic `CrawledPage`.

- **`CrawledPage`**  
  Internal immutable record used while concurrent crawl results are collected before building final `PageResult` objects.
  
- **`DomainFilter`**  
  Decides whether a URL belongs to one of the allowed domains.

- **`fetch` package**  
  Boundary toward third-party HTML libraries. `PageFetcher` and `LinkStatusChecker` are
  pure interfaces; `PageContent` and `PageLoadException` are the library-agnostic data
  and error types. `JsoupPageFetcher` and `JsoupLinkStatusChecker` are the only main
  classes that import `org.jsoup.*` — swapping jsoup means replacing this package.

- **`MarkdownWriter`**  
  Generates the final `report.md`. Accepts an optional `Path` via constructor for testability (DIP).
  Renders failed pages as a clearly marked `**error:**` block instead of crashing the report.

- **`ReportWriteException`**  
  Dedicated unchecked exception thrown when the report cannot be written.

- **`model` package**  
  `PageResult` and `LinkResult` store the crawled data as plain value objects.
  `PageResult.error(url, depth, message)` produces a result for failed page loads;
  callers use `hasError()` / `getErrorMessage()` to discriminate.

## Report Format
 
Each crawled page is written in the following structure:
 
```
<a>[https://example.com](https://example.com)</a>
<br>depth: 2
# Heading 1
## Heading 1.1
 
<br>link to <a>[https://linked-page.com](https://linked-page.com)</a>
 
<br>broken link <a>[https://broken.com](https://broken.com)</a>
```
 
Pages at deeper levels receive an arrow prefix that reflects their depth relative to the root:
 
| Level below root | Arrow prefix |
|-----------------|--------------|
| 0 (root)        | *(none)*     |
| 1               | `-->`        |
| 2               | `---->`      |
| 3               | `------>`    |
 
Example for a child page one level below root:
 
```
<a>[https://child.com](https://child.com)</a>
<br>depth: 1
## --> Child Heading
 
<br>--> link to <a>[https://grandchild.com](https://grandchild.com)</a>
```

## Functional Requirements

The crawler should support:

- command-line input for start URL(s), depth, allowed domain(s), and optional thread count
- recursive crawling up to the given depth
- domain filtering
- heading extraction
- link extraction
- broken link detection
- duplicate-visit prevention
- markdown output in a single file

## Team

- **Stefan Vrsajkovic**
- **Felix Schippel**

## Responsibilities

### Stefan Vrsajkovic

- project structure setup
- argument parsing
- crawling logic
- HTML parsing
- domain filtering
- depth handling
- duplicate-visit prevention
- unit tests for crawler-related components

### Felix Schippel

- markdown report generation
- output formatting
- README improvements
- build/integration support
- integration tests
- optional layout improvements

## Requirements

Before running the project, make sure the following tools are installed:

- JDK 17
- Maven
- Git
- IntelliJ IDEA or another Java IDE

## Getting Started

### Clone the repository

```bash
git clone <repository-url>
cd web-crawler-clean-code
```

### Run tests

```bash
mvn clean test
```

### Build the project

```bash
mvn clean package
```

### Run the application

Example:

```bash
java -jar target/web-crawler-clean-code-1.0-SNAPSHOT.jar https://example.com 2 example.com
```

Example with multiple domains:

```bash
java -jar target/web-crawler-clean-code-1.0-SNAPSHOT.jar https://example.com 2 example.com,example.org
```

Example with multiple start URLs:

```bash
java -jar target/web-crawler-clean-code-1.0-SNAPSHOT.jar https://example.com,https://example.org 2 example.com,example.org
```

Example with an explicit thread count:

```bash
java -jar target/web-crawler-clean-code-1.0-SNAPSHOT.jar https://example.com,https://example.org 2 example.com,example.org 4
```

Arguments:

```text
<urls> <depth> <domains> [threads]
```

- `urls`: one or more comma-separated start URLs
- `depth`: maximum crawl depth
- `domains`: one or more comma-separated allowed domains
- `threads`: optional positive integer; defaults to `8`

## Testing

Planned test areas include:

- argument parsing
- heading extraction
- link extraction
- domain filtering
- depth handling
- duplicate-visit prevention
- broken link detection
- page-load error handling
- concurrent crawling
- multiple start URLs
- markdown generation

## Workflow

### Branch Naming

Examples:

- `feature/crawler-core`
- `feature/markdown-output`
- `test/html-parser`
- `docs/update-readme`

### Commit Messages

Examples:

- `chore: set up initial project structure`
- `build: add jsoup and junit dependencies`
- `feat: implement argument parsing`
- `feat: add heading extraction`
- `test: add tests for domain filtering`
- `docs: update project readme`

## Notes

- each website should only be crawled once
- recursion must stop at the given depth
- only allowed domains should be followed
- crawl tasks must not wait for child tasks inside worker threads
- failed page loads should be logged and rendered in the report instead of stopping the whole crawl
- broken links should be clearly highlighted in the markdown report
- code should remain clean, readable, and modular

## AI Usage

AI was used as a supporting tool for discussing design ideas, selected refactorings, and small implementation details.
AI-assisted code sections are marked directly in the source code with comments.
All AI-assisted parts were manually reviewed, adapted, and tested.

## Submission

The repository should contain:

- complete source code
- automated unit tests
- a README with build, run, and test instructions

Additionally, the submission requires:

- a tagged release in the repository
- a PDF (1 A4 page) containing:
  - the link to the tagged release
  - the names of the group members

## License

This project was created for university coursework.
