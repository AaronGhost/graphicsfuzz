package org.graphicsfuzz;

import com.graphicsfuzz.common.ast.TranslationUnit;
import com.graphicsfuzz.common.tool.PrettyPrinterVisitor;
import com.graphicsfuzz.common.util.ShaderKind;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;


public class ProgramState {
  private TranslationUnit translationUnit;
  private ShaderKind shaderKind;
  private List<Buffer> inputBuffers;
  private List<Buffer> outputBuffers;

  public ShaderKind getShaderKind() {
    return shaderKind;
  }

  public String getShaderCode() {
    assert translationUnit != null;
    //GLSL shader parsing
    ByteArrayOutputStream innerStream = new ByteArrayOutputStream();
    try (PrintStream printStream = new PrintStream(innerStream, true, "utf-8")) {
      PrettyPrinterVisitor prettyPrinterVisitor = new PrettyPrinterVisitor(printStream);
      prettyPrinterVisitor.visit(translationUnit);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return innerStream.toString();
  }

  public void programInitialization(TranslationUnit translationUnit, ShaderKind shaderKind) {
    this.translationUnit = translationUnit;
    this.shaderKind = shaderKind;
  }

  public List<Buffer> getInputBuffers() {
    return inputBuffers;
  }

  public void setInputBuffers(List<Buffer> inputBuffers) {
    this.inputBuffers = inputBuffers;
  }

  public List<Buffer> getOutputBuffers() {
    return outputBuffers;
  }

  public void setOutputBuffers(List<Buffer> outputBuffers) {
    this.outputBuffers = outputBuffers;
  }
}
