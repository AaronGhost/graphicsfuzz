package com.graphicsfuzz.postprocessing;


import com.graphicsfuzz.ProgramState;
import com.graphicsfuzz.TestHelper;
import com.graphicsfuzz.common.ast.type.BasicType;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.junit.Assert;
import org.junit.Test;

public class ArrayIndexBuilderTest extends CommonPostProcessingTest {

  String linesProgramText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + "ivec3 var_0[2] = ivec3[2](3,4);\n"
      + "ivec3 var_1 = var_0[5];\n"
      + "}\n";

  String clampLineText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + " ivec3 var_0[2] = ivec3[2](3, 4);\n"
      + " ivec3 var_1 = var_0[clamp(5, 0, var_0.length() - 1)];\n"
      + "}\n";

  String absLineText = "#version 320 es\n"
      + "int SAFE_ABS(int p0);\n"
      + "int SAFE_ABS(int A)\n"
      + "{\n"
      + " return A == -2147483648 ? 2147483647 : abs(A);\n"
      + "}\n"
      + "void main()\n"
      + "{\n"
      + " ivec3 var_0[2] = ivec3[2](3, 4);\n"
      + " ivec3 var_1 = var_0[SAFE_ABS(5) % var_0.length()];\n"
      + "}\n";

  @Override
  protected List<PostProcessorInterface> createInstance() {
    return Arrays.asList(new ArrayIndexBuilder(true), new ArrayIndexBuilder(false));
  }

  @Test
  public void testProcessWithSingleLineShaderWithClamp() {
    ProgramState returnState = new ArrayIndexBuilder(true)
        .process(TestHelper.generateProgramStateForCode(linesProgramText));
    Assert.assertEquals(returnState.getShaderCode(), clampLineText);
  }

  @Test
  public void testProcessWithSingleLineShaderWithAbs() {
    ProgramState returnState = new ArrayIndexBuilder(false)
        .process(TestHelper.generateProgramStateForCode(linesProgramText));
    Assert.assertTrue(returnState.getWrappers().contains(
        new ImmutableTriple<Wrapper, BasicType, BasicType>(
            Wrapper.SAFE_ABS, BasicType.INT, null)));
    Assert.assertEquals(returnState.getShaderCode(), absLineText);
  }
}
