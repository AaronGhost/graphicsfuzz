package org.graphicsfuzz;

import com.graphicsfuzz.common.util.IRandom;

public abstract class ShaderGenerator {
  protected IRandom randomGenerator;

  public ShaderGenerator(IRandom randomGenerator) {
    this.randomGenerator = randomGenerator;
  }

  public abstract void generateShader(ProgramState programState);

}
