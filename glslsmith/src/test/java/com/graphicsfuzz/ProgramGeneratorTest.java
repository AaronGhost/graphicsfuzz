package com.graphicsfuzz;

import com.graphicsfuzz.common.util.RandomWrapper;
import com.graphicsfuzz.stateprinters.ShaderTrapStatePrinter;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class ProgramGeneratorTest {

  @Test
  public void testGenerateProgramProducesIndependentProgram() {
    ProgramGenerator generator = new ProgramGenerator(new RandomWrapper(0));
    generator.generateProgram(new ShaderTrapStatePrinter());
    List<Buffer> bufferList1 = generator.getShaderGenerator().getProgramState().getBuffers();
    generator.generateProgram(new ShaderTrapStatePrinter());
    List<Buffer> bufferList2 = generator.getShaderGenerator().getProgramState().getBuffers();
    Assert.assertFalse(bufferList2.stream().anyMatch(bufferList1::contains));
  }
}
