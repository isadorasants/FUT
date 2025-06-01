package com.fut.backend.domain;

import lombok.Data;

import java.util.List;

@Data
public class ExpectedResult {
  private String status;
  private List<String> errors;
  private List<String> warnings;
  private List<String> informations;
  private List<Invariant> invariants;
}