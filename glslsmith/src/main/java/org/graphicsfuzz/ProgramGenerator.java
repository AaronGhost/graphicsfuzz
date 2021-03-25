package org.graphicsfuzz;

public class ProgramGenerator {
  private ProgramState programState ;
  private ComputeShaderESGenerator shaderGenerator;
  private ShadertrapGenerator shadertrapGenerator;

  public ProgramGenerator(){
    programState = new ProgramState();
    shaderGenerator = new ComputeShaderESGenerator();
    shadertrapGenerator = new ShadertrapGenerator();
  }

  public String generateProgram() {
    shaderGenerator.generateEmptyProgram(programState);
    return shadertrapGenerator.generateShaderTrapWrapper(programState);
  }
}
