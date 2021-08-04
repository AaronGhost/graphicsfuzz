package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.ProgramState;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class BufferFormatEnforcerTest extends CommonPostProcessingTest {

  String bufferWithoutNeedText = "#version 320 es\n"
      + "layout(std430, binding = 1) buffer buffer_0 {\n"
      + " int ext_0;\n"
      + "};\n"
      + "void main()\n"
      + "{\n"
      + " ivec2 var_0 = ivec2(3) << 5;\n"
      + "}\n";

  String bufferWithOnlyBindingText = "#version 320 es\n"
      + "layout(binding = 1) buffer buffer_0 {\n"
      + " int ext_0;\n"
      + "};\n"
      + "void main()\n"
      + "{\n"
      + " ivec2 var_0 = ivec2(3) << 5;\n"
      + "}\n";

  String correctedBufferText = "#version 320 es\n"
      + "layout(binding = 1, std430) buffer buffer_0 {\n"
      + " int ext_0;\n"
      + "};\n"
      + "void main()\n"
      + "{\n"
      + " ivec2 var_0 = ivec2(3) << 5;\n"
      + "}\n";

  @Override
  protected List<PostProcessorInterface> createInstance() {
    return Collections.singletonList(new BufferFormatEnforcer());
  }


  @Test
  public void testProcessWithAlreadyCorrectBufferShader() {
    ProgramState returnState = new BufferFormatEnforcer()
        .process(generateProgramStateForCode(bufferWithoutNeedText));
    Assert.assertEquals(returnState.getShaderCode(), bufferWithoutNeedText);
  }

  @Test
  public void testProcessWithBufferToCorrectShader() {
    ProgramState returnState = new BufferFormatEnforcer()
        .process(generateProgramStateForCode(bufferWithOnlyBindingText));
    Assert.assertEquals(returnState.getShaderCode(), correctedBufferText);
  }
}
