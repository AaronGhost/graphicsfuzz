package org.graphicsfuzz;

import com.graphicsfuzz.common.util.ShaderKind;

public class ShaderTrapStatePrinter implements StatePrinter {

  @Override
  public String printWrapper(ProgramState programState) {
    if (programState.getShaderKind() == ShaderKind.COMPUTE) {
      String shaderTrapPrefix = "DECLARE_SHADER shader KIND COMPUTE\n";
      String shaderTrapSuffix = "END\n"
          + "COMPILE_SHADER shader_compiled SHADER shader\n"
          + "CREATE_PROGRAM compute_prog SHADERS shader_compiled\n"
          + "RUN_COMPUTE PROGRAM compute_prog NUM_GROUPS 1 1 1\n";
      return shaderTrapPrefix + programState.getShaderCode() + shaderTrapSuffix;
    } else {
      throw new UnsupportedOperationException("Not implemented yet");
    }
  }
}
