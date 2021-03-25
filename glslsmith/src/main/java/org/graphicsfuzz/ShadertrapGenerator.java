package org.graphicsfuzz;

public class ShadertrapGenerator {

  public String generateShaderTrapWrapper(ProgramState programState) {
    String shaderTrapPrefix = "DECLARE_SHADER shader KIND COMPUTE\n";
    String shaderTrapSuffix = "END\n"
        + "COMPILE_SHADER shader_compiled SHADER shader\n"
        + "CREATE_PROGRAM compute_prog SHADERS shader_compiled\n"
        + "RUN_COMPUTE PROGRAM compute_prog NUM_GROUPS 1 1 1\n";
    return shaderTrapPrefix+programState.getShaderCode()+shaderTrapSuffix;
  }
}
