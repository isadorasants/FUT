package com.fut.backend.core.comparator;

import com.fut.backend.domain.ExpectedResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ResultComparator {

  public List<String> compare(List<String> validatorOutput, ExpectedResult expectedResult) {
    List<String> discrepancies = new ArrayList<>();

    String status = validatorOutput.isEmpty() ? "success" : "failure";
    if (!status.equals(expectedResult.getStatus())) {
      discrepancies.add(String.format("Status mismatch: expected %s, got %s", expectedResult.getStatus(), status));
    }

    List<String> expectedErrors = expectedResult.getErrors() != null ? expectedResult.getErrors() : List.of();
    List<String> expectedWarnings = expectedResult.getWarnings() != null ? expectedResult.getWarnings() : List.of();
    List<String> expectedInformations = expectedResult.getInformations() != null ? expectedResult.getInformations()
        : List.of();

    for (String error : expectedErrors) {
      if (!validatorOutput.contains(error)) {
        discrepancies.add(String.format("Missing error: %s", error));
      }
    }

    for (String warning : expectedWarnings) {
      if (!validatorOutput.contains(warning)) {
        discrepancies.add(String.format("Missing warning: %s", warning));
      }
    }

    for (String info : expectedInformations) {
      if (!validatorOutput.contains(info)) {
        discrepancies.add(String.format("Missing information: %s", info));
      }
    }

    for (String output : validatorOutput) {
      if (!expectedErrors.contains(output) && !expectedWarnings.contains(output)
          && !expectedInformations.contains(output)) {
        discrepancies.add(String.format("Unexpected output: %s", output));
      }
    }

    return discrepancies;
  }
}