package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.ProgramState;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class InitializerEnforcerTest extends CommonPostProcessingTest {

  @Override
  protected List<PostProcessorInterface> createInstance() {
    return Collections.singletonList(new InitializerEnforcer());
  }

  String baseTypeProgramText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + " ivec2 var_0;\n"
      + "}\n";

  String baseTypeWithInitializerProgramText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + " ivec2 var_0 = ivec2(1);\n"
      + "}\n";


  String qualifiedTypeProgramText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + " const ivec2 var_0;\n"
      + "}\n";

  String qualifiedTypeWithInitializerProgramText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + " const ivec2 var_0 = ivec2(1);\n"
      + "}\n";


  String arrayProgramText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + " const ivec2 var_0[3];\n"
      + "}\n";

  String arrayWithInitializerProgramText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + " const ivec2 var_0[3] = ivec2[3](ivec2(1), ivec2(1), ivec2(1));\n"
      + "}\n";

  @Test
  public void testProcessWithBaseTypeShader() {
    ProgramState returnState = new InitializerEnforcer()
        .process(generateProgramStateForCode(baseTypeProgramText));
    Assert.assertEquals(returnState.getShaderCode(), baseTypeWithInitializerProgramText);
  }

  @Test
  public void testProcessWithQualifiedTypeShader() {
    ProgramState returnState = new InitializerEnforcer()
        .process(generateProgramStateForCode(qualifiedTypeProgramText));
    Assert.assertEquals(returnState.getShaderCode(), qualifiedTypeWithInitializerProgramText);
  }

  @Test
  public void testProcessWithArrayTypeShader() {
    ProgramState returnState = new InitializerEnforcer()
        .process(generateProgramStateForCode(arrayProgramText));
    Assert.assertEquals(returnState.getShaderCode(), arrayWithInitializerProgramText);
  }


}
