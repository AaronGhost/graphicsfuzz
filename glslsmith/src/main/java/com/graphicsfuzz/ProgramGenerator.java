package com.graphicsfuzz;


import com.graphicsfuzz.common.util.IRandom;
import com.graphicsfuzz.shadergenerators.ComputeShaderGenerator;
import com.graphicsfuzz.shadergenerators.ShaderGenerator;
import com.graphicsfuzz.stateprinters.StatePrinter;

public class ProgramGenerator {
  private final ShaderGenerator shaderGenerator;
  private final IRandom randomGenerator;

  public ShaderGenerator getShaderGenerator() {
    return shaderGenerator;
  }


  public ProgramGenerator(IRandom randomGenerator) {
    this.randomGenerator = randomGenerator;
    //We only generate Compute shaders for now
    if (randomGenerator.nextInt(1) == 0) {
      shaderGenerator = new ComputeShaderGenerator(randomGenerator);
    } else {
      throw new UnsupportedOperationException("Not implemented yet");
    }
  }

  public String generateProgram(StatePrinter printer) {
    shaderGenerator.generateShader();
    ProgramState programState = shaderGenerator.getProgramState();
    return printer.printWrapper(programState);
  }

}
