package com.fut.backend.domain;

import lombok.Data;

@Data
public class Invariant {
  private String expression;
  private Boolean expected;
}
