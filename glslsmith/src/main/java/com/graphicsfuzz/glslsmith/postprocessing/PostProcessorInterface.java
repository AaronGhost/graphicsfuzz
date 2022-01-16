package com.graphicsfuzz.glslsmith.postprocessing;

import com.graphicsfuzz.glslsmith.ProgramState;

public interface PostProcessorInterface {
  ProgramState process(ProgramState state);
}
