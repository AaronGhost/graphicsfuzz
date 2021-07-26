package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.ProgramState;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class InitializerCleanerTest extends CommonPostProcessingTest {

  String readOnlyText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + " ivec3 var_0 = ivec3(3);\n"
      + " ivec3 var_1[2] = ivec3[2](var_0, var_0);\n"
      + "}\n";

  String readWriteText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + " ivec3 var_0 = ivec3(3);\n"
      + " ivec3 var_1[2] = ivec3[2](var_0, var_0 += 2);\n"
      + "}\n";

  String readWriteCleanedText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + " ivec3 var_0 = ivec3(3);\n"
      + " ivec3 var_0_init_0_temp_0 = var_0;\n"
      + " ivec3 var_1[2] = ivec3[2](var_0, var_0_init_0_temp_0 += 2);\n"
      + "}\n";

  String writeReadAndWriteWriteText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + " ivec3 var_0 = ivec3(3);\n"
      + " ivec3 var_1[2] = ivec3[2](var_0 += 2, var_0);\n"
      + " ivec3 var_2[5] = ivec3[4](var_1[0] += 1, var_1[1] += 2, var_1[0] += 3, var_1[1] + 4, "
      + "var_1[0] + 5);\n"
      + "}\n";

  String writeReadAndWriteWriteCleanedText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + " ivec3 var_0 = ivec3(3);\n"
      + " ivec3 var_0_init_0_temp_read = var_0;\n"
      + " ivec3 var_1[2] = ivec3[2](var_0 += 2, var_0_init_0_temp_read);\n"
      + " ivec3 var_1_init_1_temp_1[2] = var_1;\n"
      + " ivec3 var_1_init_1_temp_0[2] = var_1;\n"
      + " ivec3 var_1_init_1_temp_read[2] = var_1;\n"
      + " ivec3 var_2[5] = ivec3[4](var_1[0] += 1, var_1_init_1_temp_0[1] += 2, "
      + "var_1_init_1_temp_1[0] += 3, var_1_init_1_temp_read[1] + 4, var_1_init_1_temp_read[0] + "
      + "5);\n"
      + "}\n";


  @Override
  protected List<PostProcessorInterface> createInstance() {
    return Collections.singletonList(new InitializerCleaner());
  }

  @Test
  public void testProcessWithSimpleReadReadShader() {
    ProgramState returnState = new InitializerCleaner()
        .process(generateProgramStateForCode(readOnlyText));
    Assert.assertEquals(returnState.getShaderCode(), readOnlyText);
  }

  @Test
  public void testProcessWithSimpleReadWriteShader() {
    ProgramState returnState = new InitializerCleaner()
        .process(generateProgramStateForCode(readWriteText));
    Assert.assertEquals(returnState.getShaderCode(), readWriteCleanedText);
  }

  @Test
  public void testProcessWithWriteReadAndWriteWriteShader() {
    ProgramState returnState = new InitializerCleaner()
        .process(generateProgramStateForCode(writeReadAndWriteWriteText));
    Assert.assertEquals(returnState.getShaderCode(), writeReadAndWriteWriteCleanedText);
  }

}
