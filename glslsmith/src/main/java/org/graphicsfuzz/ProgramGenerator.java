package org.graphicsfuzz;


import com.graphicsfuzz.common.ast.decl.ArrayInfo;
import com.graphicsfuzz.common.ast.decl.InterfaceBlock;
import com.graphicsfuzz.common.ast.type.ArrayType;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.common.util.IRandom;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class ProgramGenerator {
  private ShaderGenerator shaderGenerator;
  private IRandom randomGenerator;


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
    ProgramState programState = new ProgramState();
    shaderGenerator.generateShader(programState);
    return printer.printWrapper(programState);
  }

}
