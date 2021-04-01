package org.graphicsfuzz;


import com.graphicsfuzz.common.util.IRandom;

public class ProgramGenerator {
  private ProgramState programState;
  private ShaderGenerator shaderGenerator;
  private IRandom randomGenerator;


  public ProgramGenerator(IRandom randomGenerator) {
    this.randomGenerator = randomGenerator;
    programState = new ProgramState();
    //We only generate Compute shaders for now
    if (randomGenerator.nextInt(1) == 0) {
      shaderGenerator = new ComputeShaderGenerator(randomGenerator);
    } else {
      throw new UnsupportedOperationException("Not implemented yet");
    }
  }

  public String generateProgram(StatePrinter printer) {
    shaderGenerator.generateShader(programState);
    return printer.printWrapper(programState);
  }
}
