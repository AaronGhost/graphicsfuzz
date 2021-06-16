package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.ProgramState;
import com.graphicsfuzz.common.ast.TranslationUnit;
import com.graphicsfuzz.common.util.GlslParserException;
import com.graphicsfuzz.common.util.ParseHelper;
import com.graphicsfuzz.common.util.ParseTimeoutException;
import com.graphicsfuzz.common.util.ShaderKind;
import com.graphicsfuzz.config.DefaultConfig;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class WrapperBuilderTest {
  ProgramState emptyProgramState;
  ProgramState singleLineProgramState;

  String emptyProgramText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + "}\n";

  String singleLineProgramText = "#version 320 es\n"
      + "void main()\n"
      + "{\n"
      + "vec2 var_0 = vec2(3) << 5;"
      + "}\n";

  String wrapperLineProgramText =  "#version 320 es\n"
      + "vec2 SAFE_LSHIFT(vec2 p0, int p1);\n"
      + "vec2 SAFE_LSHIFT(vec2 A, int B)\n"
      + "{\n"
      + " return B >= 32 || B < 0 ? A << 16 : A << B;\n"
      + "}\n"
      + "void main()\n"
      + "{\n"
      + " vec2 var_0 = SAFE_LSHIFT(vec2(3), 5);\n"
      + "}\n";

  @Before
  public void setup() throws GlslParserException, IOException, ParseTimeoutException,
      InterruptedException {

    TranslationUnit unit = ParseHelper.parse(emptyProgramText);
    emptyProgramState = new ProgramState(new DefaultConfig());
    emptyProgramState.programInitialization(unit, ShaderKind.COMPUTE);
    unit = ParseHelper.parse(singleLineProgramText);
    singleLineProgramState = new ProgramState(new DefaultConfig());
    singleLineProgramState.programInitialization(unit, ShaderKind.COMPUTE);
  }

  @Test
  public void testProcessWithEmptyShader() {
    ProgramState returnState = new WrapperBuilder().process(emptyProgramState);
    Assert.assertEquals(returnState.getShaderCode(), emptyProgramText);
  }

  @Test
  public void testProcessWithSingleLineShader() {
    ProgramState returnState = new WrapperBuilder().process(singleLineProgramState);
    Assert.assertEquals(returnState.getShaderCode(), wrapperLineProgramText);
  }
}
