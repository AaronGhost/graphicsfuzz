package com.graphicsfuzz.stateprinters;

import com.graphicsfuzz.Buffer;
import com.graphicsfuzz.ProgramState;
import java.util.List;

public interface StatePrinter {
  default String changeShaderFromHarness(String harnessText, String newGlslCode) {
    String oldCode = getShaderCodeFromHarness(harnessText);
    return harnessText.replace(oldCode, newGlslCode + "\n");
  }

  String getShaderCodeFromHarness(String fileContent);

  String printWrapper(ProgramState programState);

  List<Buffer> getBuffersFromHarness(String fileContent);

  default String correctlyPrintNumber(Number value) {
    if (value instanceof Float) {
      return String.format("%.1f", value);
    }
    return String.valueOf(value);
  }
}
