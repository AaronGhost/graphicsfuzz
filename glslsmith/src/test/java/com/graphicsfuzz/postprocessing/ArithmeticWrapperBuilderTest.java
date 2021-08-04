package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.ProgramState;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class ArithmeticWrapperBuilderTest extends CommonPostProcessingTest {

  String singleLineProgramText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + "ivec2 var_0 = ivec2(3) << 5;"
      + "}\n";

  String wrapperLineProgramText =  "#version 320 es\n"
      + "ivec2 SAFE_LSHIFT(ivec2 p0, int p1);\n"
      + "ivec2 SAFE_LSHIFT(ivec2 A, int B)\n"
      + "{\n"
      + " return B >= 32 || B < 0 ? A << 16 : A << B;\n"
      + "}\n"
      + "void main()\n"
      + "{\n"
      + " ivec2 var_0 = SAFE_LSHIFT(ivec2(3), 5);\n"
      + "}\n";

  String multipleArithmeticProgramText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + "ivec2 var_0 = ivec2(3) << 5 / 3;"
      + "}\n";

  String multipleWrappersProgramText = "#version 320 es\n"
      + "int SAFE_DIV(int p0, int p1);\n"
      + "ivec2 SAFE_LSHIFT(ivec2 p0, int p1);\n"
      + "ivec2 SAFE_LSHIFT(ivec2 A, int B)\n"
      + "{\n"
      + " return B >= 32 || B < 0 ? A << 16 : A << B;\n"
      + "}\n"
      + "int SAFE_DIV(int A, int B)\n"
      + "{\n"
      + " return B == 0 || A == -2147483648 && B == -1 ? A / 2 : A / B;\n"
      + "}\n"
      + "void main()\n"
      + "{\n"
      + " ivec2 var_0 = SAFE_LSHIFT(ivec2(3), SAFE_DIV(5, 3));\n"
      + "}\n";

  @Override
  protected List<PostProcessorInterface> createInstance() {
    return Collections.singletonList(new ArithmeticWrapperBuilder());
  }

  @Test
  public void testProcessWithSingleLineShader() {
    ProgramState returnState = new ArithmeticWrapperBuilder()
        .process(generateProgramStateForCode(singleLineProgramText));
    Assert.assertEquals(returnState.getShaderCode(), wrapperLineProgramText);
  }

  @Test
  public void testProcessWithMultipleArithmeticShader() {
    ProgramState returnState = new ArithmeticWrapperBuilder()
        .process(generateProgramStateForCode(multipleArithmeticProgramText));
    Assert.assertEquals(returnState.getShaderCode(), multipleWrappersProgramText);
  }
}
