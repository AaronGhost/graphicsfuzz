package com.graphicsfuzz.stateprinters;

import com.graphicsfuzz.Buffer;
import com.graphicsfuzz.ProgramState;
import java.util.List;

public interface StatePrinter {
  String changeShaderFromHarness(String harnessText, String newGlslCode);

  String getShaderCodeFromHarness(String fileContent);

  String printWrapper(ProgramState programState);

  List<Buffer> getBuffersFromHarness(String fileContent);

}
