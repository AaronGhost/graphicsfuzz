package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.ProgramState;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class LoopLimiterTest extends CommonPostProcessingTest {

  String simpleWhileProgramText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + "while(true) {\n"
      + " ivec2 var_0 = ivec2(0);\n"
      + "}\n"
      + "}\n";

  String complexWhileProgramTest = "#version 320 es\n"
      + "\n"
      + "void main()\n"
      + "{\n"
      + " while(true)\n"
      + "  {\n"
      + "   ivec2 var_0 = ivec2(0);\n"
      + "   while(false)\n"
      + "    {\n"
      + "     ivec3 var_1 = ivec3(1);\n"
      + "    }\n"
      + "  }\n"
      + "}\n";

  String globalLimitedComplexWhileProgramText = "#version 320 es\n"
      + "int global_limiter = 0;\n"
      + "\n"
      + "void main()\n"
      + "{\n"
      + " while(true)\n"
      + "  {\n"
      + "   global_limiter ++;\n"
      + "   if(global_limiter > 10)\n"
      + "    break;\n"
      + "   ivec2 var_0 = ivec2(0);\n"
      + "   while(false)\n"
      + "    {\n"
      + "     global_limiter ++;\n"
      + "     if(global_limiter > 10)\n"
      + "      break;\n"
      + "     ivec3 var_1 = ivec3(1);\n"
      + "    }\n"
      + "  }\n"
      + "}\n";

  String globalLimitedSimpleWhileProgramText = "#version 320 es\n"
      + "int global_limiter = 0;\n"
      + "\n"
      + "void main()\n"
      + "{\n"
      + " while(true)\n"
      + "  {\n"
      + "   global_limiter ++;\n"
      + "   if(global_limiter > 10)\n"
      + "    break;\n"
      + "   ivec2 var_0 = ivec2(0);\n"
      + "  }\n"
      + "}\n";

  String localLimitedSimpleWhileProgramText = "#version 320 es\n"
      + "int local_limiter_0 = 0;\n"
      + "\n"
      + "void main()\n"
      + "{\n"
      + " while(true)\n"
      + "  {\n"
      + "   local_limiter_0 ++;\n"
      + "   if(local_limiter_0 > 10)\n"
      + "    break;\n"
      + "   ivec2 var_0 = ivec2(0);\n"
      + "  }\n"
      + "}\n";

  String localLimitedComplexWhileProgramText = "#version 320 es\n"
      + "int local_limiter_0 = 0;\n"
      + "\n"
      + "int local_limiter_1 = 0;\n"
      + "\n"
      + "void main()\n"
      + "{\n"
      + " while(true)\n"
      + "  {\n"
      + "   local_limiter_1 ++;\n"
      + "   if(local_limiter_1 > 10)\n"
      + "    break;\n"
      + "   ivec2 var_0 = ivec2(0);\n"
      + "   while(false)\n"
      + "    {\n"
      + "     local_limiter_0 ++;\n"
      + "     if(local_limiter_0 > 10)\n"
      + "      break;\n"
      + "     ivec3 var_1 = ivec3(1);\n"
      + "    }\n"
      + "  }\n"
      + "}\n";

  @Override
  protected List<PostProcessorInterface> createInstance() {
    return Arrays.asList(new LoopLimiter(true, 10), new LoopLimiter(false, 10));
  }

  @Test
  public void testWhileWithGlobalLimiter() {
    ProgramState returnState = new LoopLimiter(true, 10)
        .process(generateProgramStateForCode(simpleWhileProgramText));
    Assert.assertEquals(returnState.getShaderCode(), globalLimitedSimpleWhileProgramText);
  }

  @Test
  public void testWhileWithLocalLimiter() {
    ProgramState returnState = new LoopLimiter(false, 10)
        .process(generateProgramStateForCode(simpleWhileProgramText));
    Assert.assertEquals(returnState.getShaderCode(), localLimitedSimpleWhileProgramText);
  }

  @Test
  public void testComplexWhileWithGlobalLimiter() {
    ProgramState returnState = new LoopLimiter(true, 10)
        .process(generateProgramStateForCode(complexWhileProgramTest));
    Assert.assertEquals(returnState.getShaderCode(), globalLimitedComplexWhileProgramText);
  }

  @Test
  public void testComplexWhileWithLocalLimiters() {
    ProgramState returnState = new LoopLimiter(false, 10)
        .process(generateProgramStateForCode(complexWhileProgramTest));
    Assert.assertEquals(returnState.getShaderCode(), localLimitedComplexWhileProgramText);
  }
}
