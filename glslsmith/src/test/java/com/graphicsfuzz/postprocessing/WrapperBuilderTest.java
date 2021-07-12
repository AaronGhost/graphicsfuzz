package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.ProgramState;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class WrapperBuilderTest extends CommonPostProcessingTest {

  String singleLineProgramText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + "vec2 var_0 = vec2(3) << 5;"
      + "}\n";

  String wrapperLineProgramText =  "#version 320 es\n"
      + "vec2 SAFE_LSHIFT(vec2 p0, int p1);\n"
      + "vec2 SAFE_LSHIFT(vec2 A, int B)\n"
      + "{\n"
      + " return B >= 32 || B < 0 ? A << 16 : A << B;\n"
      + "}\n"
      + "void main()\n"
      + "{\n"
      + " vec2 var_0 = SAFE_LSHIFT(vec2(3), 5);\n"
      + "}\n";

  @Override
  protected List<PostProcessorInterface> createInstance() {
    return Collections.singletonList(new WrapperBuilder());
  }

  @Test
  public void testProcessWithSingleLineShader() {
    ProgramState returnState = new WrapperBuilder()
        .process(generateProgramStateForCode(singleLineProgramText));
    Assert.assertEquals(returnState.getShaderCode(), wrapperLineProgramText);
  }
}
