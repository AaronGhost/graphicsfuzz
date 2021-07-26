package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.ProgramState;
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

  String wrapperLineProgramTextv1 = "#version 320 es\n"
      + "ivec2 SAFE_BITFIELD_EXTRACT(ivec2 p0);\n"
      + "ivec2 SAFE_ABS(ivec2 p0);\n"
      + "ivec2 SAFE_BITFIELD_EXTRACT(ivec2 value, int offset, int bits)\n"
      + "{\n"
      + " int safe_offset = SAFE_ABS(offset) % 32;\n"
      + " int safe_bits = SAFE_ABS(bits) % (32 - safe_offset);\n"
      + " return bitfieldExtract(value, safe_offset, safe_bits);\n"
      + "}\n"
      + "ivec2 SAFE_ABS(ivec2 A)\n"
      + "{\n"
      + " A[0] = A[0] == -2147483648 ? 2147483647 : abs(A[0]);\n"
      + " A[1] = A[1] == -2147483648 ? 2147483647 : abs(A[1]);\n"
      + " return A;\n"
      + "}\n"
      + "void main()\n"
      + "{\n"
      + " ivec2 var_0 = SAFE_BITFIELD_EXTRACT(ivec2(3) + ivec2(5), 5, 3);\n"
      + "}\n";

  String wrapperLineProgramTextv2 = "#version 320 es\n"
      + "ivec2 SAFE_ABS(ivec2 p0);\n"
      + "ivec2 SAFE_BITFIELD_EXTRACT(ivec2 p0);\n"
      + "ivec2 SAFE_ABS(ivec2 A)\n"
      + "{\n"
      + " A[0] = A[0] == -2147483648 ? 2147483647 : abs(A[0]);\n"
      + " A[1] = A[1] == -2147483648 ? 2147483647 : abs(A[1]);\n"
      + " return A;\n"
      + "}\n"
      + "ivec2 SAFE_BITFIELD_EXTRACT(ivec2 value, int offset, int bits)\n"
      + "{\n"
      + " int safe_offset = SAFE_ABS(offset) % 32;\n"
      + " int safe_bits = SAFE_ABS(bits) % (32 - safe_offset);\n"
      + " return bitfieldExtract(value, safe_offset, safe_bits);\n"
      + "}\n"
      + "void main()\n"
      + "{\n"
      + " ivec2 var_0 = SAFE_BITFIELD_EXTRACT(ivec2(3) + ivec2(5), 5, 3);\n"
      + "}\n";

  @Override
  protected List<PostProcessorInterface> createInstance() {
    return Collections.singletonList(new StdWrapperBuilder());
  }

  @Test
  public void testProcessWithSingleLineShader() {
    ProgramState returnState = new StdWrapperBuilder()
        .process(generateProgramStateForCode(singleLineProgramText));
    String shaderCode = returnState.getShaderCode();
    Assert.assertTrue(shaderCode.equals(wrapperLineProgramTextv1)
        || shaderCode.equals(wrapperLineProgramTextv2));
  }
}
