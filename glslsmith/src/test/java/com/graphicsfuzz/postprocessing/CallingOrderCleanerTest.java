package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.ProgramState;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class CallingOrderCleanerTest extends CommonPostProcessingTest {

  @Override
  protected List<PostProcessorInterface> createInstance() {
    return Collections.singletonList(new CallingOrderCleaner());
  }


  @Test
  public void testProcessWithSimpleReadReadShader() {
    String readOnlyText = "#version 320 es\n"
        + "void main()\n"
        + "{\n"
        + " ivec3 var_0 = ivec3(3);\n"
        + " ivec3 var_1[2] = ivec3[2](var_0, var_0);\n"
        + "}\n";

    ProgramState returnState = new CallingOrderCleaner()
        .process(generateProgramStateForCode(readOnlyText));
    Assert.assertEquals(returnState.getShaderCode(), readOnlyText);
  }

  @Test
  public void testProcessWithSimpleReadWriteShader() {
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

    ProgramState returnState = new CallingOrderCleaner()
        .process(generateProgramStateForCode(readWriteText));
    Assert.assertEquals(returnState.getShaderCode(), readWriteCleanedText);
  }

  @Test
  public void testProcessWithWriteReadAndWriteWriteShader() {
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

    ProgramState returnState = new CallingOrderCleaner()
        .process(generateProgramStateForCode(writeReadAndWriteWriteText));
    Assert.assertEquals(returnState.getShaderCode(), writeReadAndWriteWriteCleanedText);
  }

  @Test
  public void testProcessWithIntricateArrayShader() {
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

    ProgramState returnState = new CallingOrderCleaner()
        .process(generateProgramStateForCode(intricateWriteWriteText));
    Assert.assertEquals(returnState.getShaderCode(), intricateWriteWriteCleanedText);
  }

  @Test
  public void testProcessWithIntraArgsShader() {
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

    ProgramState returnState = new CallingOrderCleaner()
        .process(generateProgramStateForCode(intraArgText));
    Assert.assertEquals(returnState.getShaderCode(), intraArgCleanedText);
  }

  @Test
  public void testProcessWithOutFunCall() {
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

    ProgramState returnState = new CallingOrderCleaner()
        .process(generateProgramStateForCode(funCallText));
    Assert.assertEquals(returnState.getShaderCode(), funCallCleanedText);
  }

  @Test
  public void testProcessWithInitializerInFunCall() {
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

    ProgramState returnState = new CallingOrderCleaner()
        .process(generateProgramStateForCode(initializerInFunCallText));
    Assert.assertEquals(returnState.getShaderCode(), initializerInFunCallCleanedText);
  }

  @Test
  public void testProcessWithFunCallInInitializer() {
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

    ProgramState returnState = new CallingOrderCleaner()
        .process(generateProgramStateForCode(funCallInInitializerText));
    Assert.assertEquals(returnState.getShaderCode(), funCallInInitializerCleanedText);
  }

  @Test
  public void testProcessWithExprWithoutUB() {
    String exprWithoutUbText = "#version 320 es\n"
        + "void main()\n"
        + "{\n"
        + " int x = 1;\n"
        + " int y = int[1](1)[x += x] += x;\n"
        + "}\n";

    ProgramState returnState = new CallingOrderCleaner()
        .process(generateProgramStateForCode(exprWithoutUbText));
    Assert.assertEquals(returnState.getShaderCode(), exprWithoutUbText);
  }

  @Test
  public void testProcessWithExprWithSimpleUB() {
    String exprWithSimpleUbText = "#version 320 es\n"
        + "void main()\n"
        + "{\n"
        + " int x = 1;\n"
        + " int y = x + x++ + (x += 5);\n"
        + "}\n";

    String exprWithSimpleUbCleanedText = "#version 320 es\n"
        + "void main()\n"
        + "{\n"
        + " int x = 1;\n"
        + " int x_expr_temp_0 = x;\n"
        + " int x_expr_temp_1 = x;\n"
        + " int y = x + x_expr_temp_0 ++ + (x_expr_temp_1 += 5);\n"
        + "}\n";

    ProgramState returnState = new CallingOrderCleaner()
        .process(generateProgramStateForCode(exprWithSimpleUbText));
    Assert.assertEquals(returnState.getShaderCode(), exprWithSimpleUbCleanedText);
  }


  @Test
  public void testProcessWithTernary() {
    String exprWithTernaryUbText = "#version 320 es\n"
        + "void main()\n"
        + "{\n"
        + " int x = 1;\n"
        + " int y = 1;\n"
         + " int z = (x += (y + x)) + (y + 3) + y++;\n"
        + " int w = (y += 1) + ((x += 1) != (y += 1) ? y += 2 : x + y) + y;\n"
        + "}\n";

    String exprWithTernaryUbClearedText = "#version 320 es\n"
        + "void main()\n"
        + "{\n"
        + " int x = 1;\n"
        + " int y = 1;\n"
        + " int y_expr_temp_0 = y;\n"
        + " int z = (x += (y + x)) + (y + 3) + y_expr_temp_0 ++;\n"
        + " int y_expr_temp_read = y;\n"
        + " int y_expr_temp_2 = y;\n"
        + " int y_expr_temp_1 = y;\n"
        + " int w = (y += 1) + ((x += 1) != (y_expr_temp_1 += 1) ? y_expr_temp_2 += 2 : x + "
        + "y_expr_temp_read) + y_expr_temp_read;\n"
        + "}\n";
    ProgramState returnState = new CallingOrderCleaner()
        .process(generateProgramStateForCode(exprWithTernaryUbText));
    Assert.assertEquals(returnState.getShaderCode(), exprWithTernaryUbClearedText);
  }

}
