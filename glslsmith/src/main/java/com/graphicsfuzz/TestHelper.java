package com.graphicsfuzz;

import com.graphicsfuzz.common.ast.TranslationUnit;
import com.graphicsfuzz.common.ast.decl.Declaration;
import com.graphicsfuzz.common.ast.expr.Expr;
import com.graphicsfuzz.common.tool.PrettyPrinterVisitor;
import com.graphicsfuzz.common.util.ParseHelper;
import com.graphicsfuzz.config.ParameterConfiguration;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class TestHelper {
  public static String getText(Expr expr) {
    ByteArrayOutputStream innerStream = new ByteArrayOutputStream();
    try (PrintStream printStream = new PrintStream(innerStream, true, "utf-8")) {
      PrettyPrinterVisitor prettyPrinterVisitor = new PrettyPrinterVisitor(printStream);
      prettyPrinterVisitor.visit(expr);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return innerStream.toString();
  }

  public static String getText(Declaration declarationToDump) {
    ByteArrayOutputStream innerStream = new ByteArrayOutputStream();
    try (PrintStream printStream = new PrintStream(innerStream, true, "utf-8")) {
      PrettyPrinterVisitor prettyPrinterVisitor = new PrettyPrinterVisitor(printStream);
      prettyPrinterVisitor.visit(declarationToDump);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return innerStream.toString();
  }

  public static ProgramState generateProgramStateForCode(String programText, List<Buffer> buffers) {
    TranslationUnit unit = null;
    try {
      unit = ParseHelper.parse(programText);
    } catch (Exception e) {
      e.printStackTrace();
    }
    ProgramState state = new ProgramState(new ParameterConfiguration.Builder()
        .getConfig());
    for (Buffer buffer : buffers) {
      state.addBuffer(buffer);
    }
    state.programInitialization(unit);
    return state;
  }

  public static ProgramState generateProgramStateForCode(String programText) {
    return generateProgramStateForCode(programText, new ArrayList<>());
  }
}
