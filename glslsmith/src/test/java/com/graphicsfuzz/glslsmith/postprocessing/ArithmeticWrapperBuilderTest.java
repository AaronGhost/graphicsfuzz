package com.graphicsfuzz.glslsmith.postprocessing;

import com.graphicsfuzz.glslsmith.ProgramState;
import com.graphicsfuzz.glslsmith.util.TestHelper;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class ArithmeticWrapperBuilderTest extends CommonPostProcessingTest {

  String singleIntLineProgramText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + "ivec2 var_0 = ivec2(3) << 5;"
      + "}\n";

  String wrapperIntLineProgramText =  "#version 320 es\n"
      + "ivec2 SAFE_LSHIFT(ivec2 p0, int p1);\n"
      + "ivec2 SAFE_LSHIFT(ivec2 A, int B)\n"
      + "{\n"
      + " return B >= 32 || B < 0 ? A << 16 : A << B;\n"
      + "}\n"
      + "void main()\n"
      + "{\n"
      + " ivec2 var_0 = SAFE_LSHIFT(ivec2(3), 5);\n"
      + "}\n";

  String multipleIntArithmeticProgramText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + "ivec2 var_0 = ivec2(3) << 5 / 3;"
      + "}\n";

  String multipleIntWrappersMainText = "void main()\n"
      + "{\n"
      + " ivec2 var_0 = SAFE_LSHIFT(ivec2(3), SAFE_DIV(5, 3));\n"
      + "}\n";

  String singleFloatLineProgramText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + "float var_0 = 5.0f + 3.0f;"
      + "}\n";

  String singleFloatWrapperProgramText = "#version 320 es\n"
      + "float SAFE_FLOAT_RESULT(float p0);\n"
      + "float SAFE_FLOAT_RESULT(float A)\n"
      + "{\n"
      + " return abs(A) >= 16777216.0 ? 10.0f : A;\n"
      + "}\n"
      + "void main()\n"
      + "{\n"
      + " float var_0 = SAFE_FLOAT_RESULT(5.0f + 3.0f);\n"
      + "}\n";

  String multipleFloatArithmeticProgramText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + "float var_0 = 3.0f;"
      + "float var_1 = (var_0++) + -- var_0;"
      + "}\n";

  String multipleFloatWrappersMainText = "void main()\n"
      + "{\n"
      + " float var_0 = 3.0f;\n"
      + " float var_1 = SAFE_FLOAT_RESULT((SAFE_POST_INC(var_0)) + SAFE_PRE_DEC(var_0));\n"
      + "}\n";

  String floatFunctionCallProgramText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + "float var_0 = float(1);"
      + "float var_1 = dot(1.0, 2.0);"
      + "}\n";

  String floatFunctionCallCleanedProgramText = "#version 320 es\n"
      + "float SAFE_FLOAT_RESULT(float p0);\n"
      + "float SAFE_FLOAT_RESULT(float A)\n"
      + "{\n"
      + " return abs(A) >= 16777216.0 ? 10.0f : A;\n"
      + "}\n"
      + "void main()\n"
      + "{\n"
      + " float var_0 = SAFE_FLOAT_RESULT(float(1));\n"
      + " float var_1 = SAFE_FLOAT_RESULT(dot(1.0, 2.0));\n"
      + "}\n";

  @Override
  protected List<PostProcessorInterface> createInstance() {
    return Collections.singletonList(new ArithmeticWrapperBuilder());
  }

  @Test
  public void testProcessWithSingleIntLineShader() {
    ProgramState returnState = new ArithmeticWrapperBuilder()
        .process(TestHelper.generateProgramStateForCode(singleIntLineProgramText));
    System.out.println(returnState.getShaderCode());
    Assert.assertEquals(returnState.getShaderCode(), wrapperIntLineProgramText);
  }

  @Test
  public void testProcessWithMultipleIntArithmeticShader() {
    ProgramState returnState = new ArithmeticWrapperBuilder()
        .process(TestHelper.generateProgramStateForCode(multipleIntArithmeticProgramText));
    Assert.assertTrue(returnState.getShaderCode().contains(multipleIntWrappersMainText));
  }

  @Test
  public void testProcessWithFloatSingleLineShader() {
    ProgramState returnState = new ArithmeticWrapperBuilder()
        .process(TestHelper.generateProgramStateForCode(singleFloatLineProgramText));
    Assert.assertEquals(returnState.getShaderCode(), singleFloatWrapperProgramText);
  }

  @Test
  public void testProcessWithMultipleFloatArithmeticShader() {
    ProgramState returnState = new ArithmeticWrapperBuilder()
        .process(TestHelper.generateProgramStateForCode(multipleFloatArithmeticProgramText));
    System.out.println(returnState.getShaderCode());
    Assert.assertTrue(returnState.getShaderCode().contains(multipleFloatWrappersMainText));
  }

  @Test
  public void testProcessWithFloatFunCallShader() {
    ProgramState returnState = new ArithmeticWrapperBuilder()
        .process(TestHelper.generateProgramStateForCode(floatFunctionCallProgramText));
    Assert.assertEquals(returnState.getShaderCode(), floatFunctionCallCleanedProgramText);
  }


}
