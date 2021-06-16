package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.ProgramState;

public interface PostProcessorInterface {
  ProgramState process(ProgramState state);
}
