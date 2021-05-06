package com.graphicsfuzz.stateprinters;

import com.graphicsfuzz.ProgramState;

public interface StatePrinter {
  String printWrapper(ProgramState programState);
}
