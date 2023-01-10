package com.graphicsfuzz.glslsmith.postprocessing;

import com.graphicsfuzz.glslsmith.ProgramState;
import com.graphicsfuzz.glslsmith.util.TestHelper;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;


public class StdWrapperBuilderTest extends CommonPostProcessingTest {

  String singleLineProgramText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + "ivec2 var_0 = bitfieldExtract(ivec2(3) + ivec2(5), 5, 3);"
      + "}\n";

  @Override
  protected List<PostProcessorInterface> createInstance() {
    return Collections.singletonList(new StdWrapperBuilder());
  }

  @Test
  public void testProcessWithSafeBitfieldExtractShader() {
    ProgramState returnState = new StdWrapperBuilder()
        .process(TestHelper.generateProgramStateForCode(singleLineProgramText));
    String shaderCode = returnState.getShaderCode();

    // Order of declaration is undefined
    Assert.assertTrue(
        shaderCode.contains("ivec2 SAFE_BITFIELD_EXTRACT(ivec2 p0, int p1, int p2);\n"));
    Assert.assertTrue(
        shaderCode.contains("int SAFE_ABS(int p0);\n"));
    Assert.assertTrue(
        shaderCode.contains(
            "int SAFE_ABS(int A)\n"
                + "{\n"
                + " return A == -2147483648 ? 2147483647 : abs(A);\n"
                + "}\n"
        ));
    Assert.assertTrue(
        shaderCode.contains("ivec2 SAFE_BITFIELD_EXTRACT(ivec2 value, int offset, int bits)\n"
            + "{\n"
            + " int safe_offset = SAFE_ABS(offset) % 32;\n"
            + " int safe_bits = SAFE_ABS(bits) % (32 - safe_offset);\n"
            + " return bitfieldExtract(value, safe_offset, safe_bits);\n"
            + "}\n"
        ));
    Assert.assertTrue(
        shaderCode.contains("void main()\n"
        + "{\n"
        + " ivec2 var_0 = SAFE_BITFIELD_EXTRACT(ivec2(3) + ivec2(5), 5, 3);\n"
        + "}\n"
        ));
  }

  String uintFloatConversionText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + " uvec2 var_0 = uvec2(- 2.0f);\n"
      + "}\n";

  String uintFloatCleanedConversionText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + " uvec2 var_0 = uvec2(abs(- 2.0f));\n"
      + "}\n";

  @Test
  public void testProcessWithUintFromFloatConversionShader() {
    ProgramState returnState = new StdWrapperBuilder()
        .process(TestHelper.generateProgramStateForCode(uintFloatConversionText));
    Assert.assertEquals(returnState.getShaderCode(), uintFloatCleanedConversionText);
  }
}
