package com.graphicsfuzz.stateprinters;

import com.graphicsfuzz.ProgramState;

public interface StatePrinter {
  String changeShaderFromHarness(String harnessText, String newGlslCode);

  String getShaderCodeFromHarness(String fileContent);

  String printWrapper(ProgramState programState);

}
