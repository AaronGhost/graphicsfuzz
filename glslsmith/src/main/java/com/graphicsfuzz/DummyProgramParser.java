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
        + " ivec3 var_0 = ivec3(3);\n"
        + " ivec3 var_1[2] = ivec3[2](var_0, var_0 += 2);\n"
        + "}\n";
    TranslationUnit unit = ParseHelper.parse(programText);
    System.out.println(unit);
  }
}
