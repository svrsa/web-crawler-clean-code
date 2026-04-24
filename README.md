# Web Crawler – Clean Code Assignment

A Java-based web crawler that creates a compact markdown overview of a given website and recursively linked websites by recording headings and links.

## Overview

The goal of this project is to implement a command-line web crawler in Java for the Clean Code assignment.

The crawler starts from a given URL, extracts headings and links from the website, and recursively follows allowed links up to a specified depth. The final result is written into a single markdown report file.

The project focuses on clean code, maintainability, and automated testing.

## Project Goals

The crawler should:

- accept a start URL, crawl depth, and allowed domain(s) as command-line arguments
- extract headings (`h1` to `h6`)
- extract links (`a[href]`)
- recursively crawl linked pages within the allowed depth
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
│   │           │   ├── CrawlerService.java
│   │           │   └── DomainFilter.java
│   │           ├── model/
│   │           │   ├── LinkResult.java
│   │           │   └── PageResult.java
│   │           └── parser/
│   │               └── HtmlParser.java
│   └── test/
│       └── java/
│           └── at/aau/webcrawler/
│               ├── ArgumentParserTest.java
│               ├── DomainFilterTest.java
│               ├── HtmlParserTest.java
│               ├── LinkResultTest.java
│               └── PageResultTest.java
├── pom.xml
└── README.md
```

## Planned Components

- `Main`  
  Entry point of the application.

- `ArgumentParser`  
  Parses and validates command-line arguments.

- `CrawlerService`  
  Contains the main crawling logic, recursion, depth handling, and duplicate-visit prevention.

- `HtmlParser`  
  Extracts headings and links from HTML documents.

- `MarkdownWriter`  
  Generates the final `report.md` output.

- `model` package  
  Stores result objects for crawled pages and links.

## Functional Requirements

The crawler should support:

- command-line input for URL, depth, and domains
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

## Testing

Planned test areas include:

- argument parsing
- heading extraction
- link extraction
- domain filtering
- depth handling
- duplicate-visit prevention
- broken link detection
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