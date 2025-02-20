package com.graphicsfuzz.glslsmith.stateprinters;

import com.graphicsfuzz.glslsmith.Buffer;
import com.graphicsfuzz.glslsmith.ProgramState;
import java.util.ArrayList;
import java.util.List;

public interface StatePrinter {
  default String changeShaderFromHarness(String harnessText, String newGlslCode) {
    String oldCode = getShaderCodeFromHarness(harnessText);
    return harnessText.replace(oldCode, newGlslCode + "\n");
  }

  String getShaderCodeFromHarness(String fileContent);

  String addBufferToHarness(String fileContent, Buffer buffer);

  ArrayList<Boolean> parseIdsBuffer(String idsBufferName);

  String printHarness(ProgramState programState);

  List<Buffer> getBuffersFromHarness(String fileContent);

  default String correctlyPrintNumber(Number value) {
    if (value instanceof Float) {
      return String.format("%.1f", value);
    }
    return String.valueOf(value);
  }
}
