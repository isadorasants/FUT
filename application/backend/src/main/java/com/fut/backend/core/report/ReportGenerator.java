package com.fut.backend.core.report;

import com.fut.backend.domain.TestCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReportGenerator {

  private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);

  private final ObjectMapper objectMapper = new ObjectMapper();

  public void generateReport(List<TestCase> testCases, Map<String, List<String>> discrepanciesByTestId) {
    List<Map<String, Object>> testResults = new ArrayList<>();
    int passed = 0;
    long totalTimeMs = 0;

    for (TestCase testCase : testCases) {
      long startTime = System.currentTimeMillis();
      Map<String, Object> testResult = new HashMap<>();
      String testId = testCase.getId();
      List<String> discrepancies = discrepanciesByTestId.getOrDefault(testId, List.of());
      String status = discrepancies.isEmpty() ? "success" : "failure";
      long executionTimeMs = System.currentTimeMillis() - startTime;

      testResult.put("test_id", testId);
      testResult.put("status", status);
      testResult.put("discrepancies", discrepancies);
      testResult.put("execution_time_ms", executionTimeMs);
      testResults.add(testResult);

      if (status.equals("success")) {
        passed++;
      }
      totalTimeMs += executionTimeMs;
    }

    Map<String, Object> report = new HashMap<>();
    report.put("tests", testResults);
    report.put("total_tests", testCases.size());
    report.put("passed", passed);
    report.put("failed", testCases.size() - passed);
    report.put("total_time_ms", totalTimeMs);
    report.put("timestamp", Instant.now().toString());

    writeReportToFile(report);
  }

  private void writeReportToFile(Map<String, Object> report) {
    try {
      Files.createDirectories(Paths.get("reports"));
      objectMapper.writeValue(new File("reports/test-report.json"), report);
    } catch (IOException e) {
      logger.error("Erro ao gerar relatório: {}", e.getMessage());
    }
  }
}