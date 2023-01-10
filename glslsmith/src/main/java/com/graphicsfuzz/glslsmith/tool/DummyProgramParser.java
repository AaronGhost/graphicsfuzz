package com.graphicsfuzz.glslsmith.tool;

import com.graphicsfuzz.common.ast.TranslationUnit;
import com.graphicsfuzz.common.typing.Typer;
import com.graphicsfuzz.common.util.GlslParserException;
import com.graphicsfuzz.common.util.ParseHelper;
import com.graphicsfuzz.common.util.ParseTimeoutException;
import java.io.IOException;

public class DummyProgramParser {

  public static void main(String[] args) throws GlslParserException, IOException,
      ParseTimeoutException, InterruptedException {
    String programText = "#version 320 es\n"
        + "void main()\n"
        + "{\n"
        + " ivec3 var_0[2] = ivec3[2](ivec3(3), ivec3(3));\n"
        + " ivec3 var_1_func_20_temp_1[2] = var_0;\n"
        + "}\n";
    TranslationUnit unit = ParseHelper.parse(programText);
    Typer typer = new Typer(unit);
    System.out.println("finished");
  }
}
