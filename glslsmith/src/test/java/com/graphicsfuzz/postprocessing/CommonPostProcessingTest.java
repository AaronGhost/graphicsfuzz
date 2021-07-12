package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.ProgramState;
import com.graphicsfuzz.common.ast.TranslationUnit;
import com.graphicsfuzz.common.util.ParseHelper;
import com.graphicsfuzz.common.util.ShaderKind;
import com.graphicsfuzz.config.DefaultConfig;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public abstract class CommonPostProcessingTest {

  String emptyProgramText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + "}\n";

  ProgramState emptyProgramState = generateProgramStateForCode(emptyProgramText);

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

  public ProgramState generateProgramStateForCode(String programText) {
    TranslationUnit unit = null;
    try {
      unit = ParseHelper.parse(programText);
    } catch (Exception e) {
      e.printStackTrace();
    }
    ProgramState state = new ProgramState(new DefaultConfig());
    state.programInitialization(unit, ShaderKind.COMPUTE);
    return state;
  }
}
