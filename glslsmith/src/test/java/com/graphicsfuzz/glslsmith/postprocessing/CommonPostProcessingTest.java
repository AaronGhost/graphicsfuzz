package com.graphicsfuzz.glslsmith.postprocessing;

import com.graphicsfuzz.glslsmith.ProgramState;
import com.graphicsfuzz.glslsmith.util.TestHelper;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public abstract class CommonPostProcessingTest {

  String emptyProgramText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + "}\n";

  ProgramState emptyProgramState = TestHelper.generateProgramStateForCode(emptyProgramText);

  private List<PostProcessorInterface> processedInterfaces;

  protected abstract List<PostProcessorInterface> createInstance();

  @Before
  public void setup() {
    processedInterfaces = createInstance();
  }

  @Test
  public void testProcessWithEmptyShader() {
    for (PostProcessorInterface postProcessorInterface : processedInterfaces) {
      ProgramState returnState = postProcessorInterface.process(emptyProgramState);
      Assert.assertEquals(returnState.getShaderCode(), emptyProgramText);
    }
  }
}
