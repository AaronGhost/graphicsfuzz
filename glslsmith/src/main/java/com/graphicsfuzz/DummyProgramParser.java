package com.graphicsfuzz;

import com.graphicsfuzz.common.ast.TranslationUnit;
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
        + "ivec4 var_0[2] = ivec4[2](ivec4(1,23,12,13),ivec4(2));\n"
        + "ivec2 var_1 = var_0[0].xy;\n"
        + "}";
    TranslationUnit unit = ParseHelper.parse(programText);
    System.out.println(unit);
  }
}
