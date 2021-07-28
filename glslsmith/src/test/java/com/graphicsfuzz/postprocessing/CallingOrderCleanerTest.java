package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.ProgramState;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class CallingOrderCleanerTest extends CommonPostProcessingTest {

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

  String intricateWriteWriteText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + " ivec3 var_0 = ivec3(3);\n"
      + " ivec3 var_1[2][2] = ivec3[3](ivec3[2](var_0 += 1, var_0 += 2), ivec3[2](var_0 += 1, "
      + "var_0 += 2));\n"
      + "}\n";

  String intricateWriteWriteCleanedText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + " ivec3 var_0 = ivec3(3);\n"
      + " ivec3 var_0_init_0_temp_0 = var_0;\n"
      + " ivec3 var_0_init_1_temp_1 = var_0;\n"
      + " ivec3 var_0_init_1_temp_2 = var_0;\n"
      + " ivec3 var_1[2][2] = ivec3[3](ivec3[2](var_0 += 1, var_0_init_0_temp_0 += 2), "
      + "ivec3[2](var_0_init_1_temp_1 += 1, var_0_init_1_temp_2 += 2));\n"
      + "}\n";

  String intraArgText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + " int var_0 = 3;\n"
      + " int var_1[2] = int[2](var_0 += (var_0 += 1), var_0);\n"
      + "}";

  String intraArgCleanedText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + " int var_0 = 3;\n"
      + " int var_0_init_0_temp_read = var_0;\n"
      + " int var_1[2] = int[2](var_0 += (var_0 += 1), var_0_init_0_temp_read);\n"
      + "}\n";

  String funCallText = "#version 320 es\n"
      + "void f(out int x, out int y)\n"
      + "{\n"
      + "}\n"
      + "void main()\n"
      + "{\n"
      + " int var_0 = 3;\n"
      + " f(var_0, var_0);\n"
      + "}\n";

  String funCallCleanedText = "#version 320 es\n"
      + "void f(out int x, out int y)\n"
      + "{\n"
      + "}\n"
      + "void main()\n"
      + "{\n"
      + " int var_0 = 3;\n"
      + " int var_0_func_0_temp_0 = var_0;\n"
      + " f(var_0, var_0_func_0_temp_0);\n"
      + "}\n";

  String initializerInFunCallText = "#version 320 es\n"
      + "void f(out int x, int y, int z[4], out int w)\n"
      + "{\n"
      + "}\n"
      + "void main()\n"
      + "{\n"
      + " int var_0 = 3;\n"
      + " f(var_0, var_0, int[4](var_0, var_0, var_0 += 1, var_0 += 2), var_0);\n"
      + "}\n";

  String initializerInFunCallCleanedText = "#version 320 es\n"
      + "void f(out int x, int y, int z[4], out int w)\n"
      + "{\n"
      + "}\n"
      + "void main()\n"
      + "{\n"
      + " int var_0 = 3;\n"
      + " int var_0_func_0_temp_2 = var_0;\n"
      + " int var_0_init_0_temp_1 = var_0;\n"
      + " int var_0_init_0_temp_0 = var_0;\n"
      + " f(var_0, var_0, int[4](var_0, var_0, var_0_init_0_temp_0 += 1, var_0_init_0_temp_1 += "
      + "2), var_0_func_0_temp_2);\n"
      + "}\n";

  String funCallInInitializerText = "#version 320 es\n"
      + "int f(out int x, int y, out int z)\n"
      + "{\n"
      + " return z + x;\n"
      + "}\n"
      + "void main()\n"
      + "{\n"
      + " int var_0 = 3;\n"
      + " int var_1[4] = int[4](var_0 += 1, f(var_0, var_0, var_0), var_0, var_0 += 2);\n"
      + "}\n";

  String funCallInInitializerCleanedText = "#version 320 es\n"
      + "int f(out int x, int y, out int z)\n"
      + "{\n"
      + " return z + x;\n"
      + "}\n"
      + "void main()\n"
      + "{\n"
      + " int var_0 = 3;\n"
      + " int var_0_init_0_temp_read = var_0;\n"
      + " int var_0_init_0_temp_1 = var_0;\n"
      + " int var_0_init_0_temp_0 = var_0;\n"
      + " int var_0_init_0_temp_2 = var_0;\n"
      + " int var_1[4] = int[4](var_0 += 1, f(var_0_init_0_temp_0, var_0_init_0_temp_read, "
      + "var_0_init_0_temp_1), var_0_init_0_temp_read, var_0_init_0_temp_2 += 2);\n"
      + "}\n";

  @Override
  protected List<PostProcessorInterface> createInstance() {
    return Collections.singletonList(new CallingOrderCleaner());
  }

  @Test
  public void testProcessWithSimpleReadReadShader() {
    ProgramState returnState = new CallingOrderCleaner()
        .process(generateProgramStateForCode(readOnlyText));
    Assert.assertEquals(returnState.getShaderCode(), readOnlyText);
  }

  @Test
  public void testProcessWithSimpleReadWriteShader() {
    ProgramState returnState = new CallingOrderCleaner()
        .process(generateProgramStateForCode(readWriteText));
    Assert.assertEquals(returnState.getShaderCode(), readWriteCleanedText);
  }

  @Test
  public void testProcessWithWriteReadAndWriteWriteShader() {
    ProgramState returnState = new CallingOrderCleaner()
        .process(generateProgramStateForCode(writeReadAndWriteWriteText));
    Assert.assertEquals(returnState.getShaderCode(), writeReadAndWriteWriteCleanedText);
  }

  @Test
  public void testProcessWithIntricateArrayShader() {
    ProgramState returnState = new CallingOrderCleaner()
        .process(generateProgramStateForCode(intricateWriteWriteText));
    Assert.assertEquals(returnState.getShaderCode(), intricateWriteWriteCleanedText);
  }

  @Test
  public void testProcessWithIntraArgsShader() {
    ProgramState returnState = new CallingOrderCleaner()
        .process(generateProgramStateForCode(intraArgText));
    Assert.assertEquals(returnState.getShaderCode(), intraArgCleanedText);
  }

  @Test
  public void testProcessWithOutFunCall() {
    ProgramState returnState = new CallingOrderCleaner()
        .process(generateProgramStateForCode(funCallText));
    Assert.assertEquals(returnState.getShaderCode(), funCallCleanedText);
  }

  @Test
  public void testProcessWithInitializerInFunCall() {
    ProgramState returnState = new CallingOrderCleaner()
        .process(generateProgramStateForCode(initializerInFunCallText));
    Assert.assertEquals(returnState.getShaderCode(), initializerInFunCallCleanedText);
  }

  @Test
  public void testProcessWithFunCallInInitializer() {
    ProgramState returnState = new CallingOrderCleaner()
        .process(generateProgramStateForCode(funCallInInitializerText));
    Assert.assertEquals(returnState.getShaderCode(), funCallInInitializerCleanedText);
  }
}
