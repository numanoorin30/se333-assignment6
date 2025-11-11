# SE333 – Assignment 6 – UI Testing with Playwright

## Reflection 
    
In the manual Playwright tests (playwrightTraditional), I explicitly controlled 
selectors, assertions, and navigation, which took more effort up front but produced 
tests that closely match the assignment specifications and are easier to reason about and 
maintain, since I know exactly where to update them when the site changes. With the AI-assisted 
approach (playwrightLLM), describing flows in natural language and letting an AI generate tests 
can speed up initial creation, but I still need to fix brittle selectors, insert reliable waits, 
tighten assertions, and make sure the tests follow the assignment constraints; the generated code 
is often noisy or fragile, so it’s not “fire and forget.” Overall, the manual tests are more reliable 
and intentional, while AI-assisted tests work best as a fast starting point that still requires human 
review and refinement before being trusted or integrated into CI—an effective workflow is to use AI to draft, 
then refine manually, then run in CI.

## Overview

This repository contains my solution for **Assignment 6 – UI Testing**.

It uses:
- Java + Maven
- Playwright for Java
- JUnit 5
- GitHub Actions to run UI tests automatically

The main scenario automates a purchase pathway on the
[DePaul University Bookstore](https://depaul.bncollege.com/) website:
searching for earbuds, filtering, selecting a JBL product, adding to cart,
proceeding through checkout steps, and cleaning up the cart.

## Project Structure

- `src/test/java/playwrightTraditional/BookstoreTraditionalTest.java`  
  Manually-authored Playwright tests following the detailed step-by-step flow
  from the assignment (specification-based and structural-based checks).

- `src/test/java/playwrightLLM/BookstoreLLMGeneratedTest.java`  
  Tests representing an AI-assisted / MCP-style generation approach.

- `.github/workflows/Assignment6-UI-Tests.yml`  
  GitHub Actions workflow that:
    - Installs JDK and dependencies
    - Runs all Playwright tests on each push/PR
    - Uploads Playwright video recordings as artifacts

## How to Run Locally

```bash
mvn -Dplaywright.cli.install=true test
