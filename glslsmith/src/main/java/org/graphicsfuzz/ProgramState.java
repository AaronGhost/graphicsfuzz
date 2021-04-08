package org.graphicsfuzz;

import com.graphicsfuzz.common.ast.TranslationUnit;
import com.graphicsfuzz.common.tool.PrettyPrinterVisitor;
import com.graphicsfuzz.common.util.ShaderKind;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class ProgramState {
  private TranslationUnit translationUnit;
  private ShaderKind shaderKind;
  private Map<String, Symbol> symbolTable = new LinkedHashMap<>();
  private int bindingOffset = 0;
  private int variableOffset = 0;

  public int getBindingOffset() {
    return bindingOffset;
  }

  public int getVariableOffset() {
    return variableOffset;
  }

  public void addVariableOffset(int offset) {
    variableOffset += offset;
  }

  public void addBindingOffset(int offset) {
    bindingOffset += offset;
  }

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

  //Symbol table functions
  public void addSymbol(Symbol symbol) {
    symbolTable.put(symbol.getName(), symbol);
  }

  public Symbol getSymbol(String name) {
    return symbolTable.get(name);
  }

  public List<Symbol> getSymbolByType(String symbolType) {
    Predicate<Map.Entry<String, Symbol>> predicate =
        (t -> t.getValue().getType().equals(symbolType));
    return getSymbolMatchingPredicate(predicate);
  }

  public List<Symbol> getSymbolMatchingPredicate(Predicate<Map.Entry<String, Symbol>>
                                                          symbolPredicate) {
    return symbolTable.entrySet().stream().filter(symbolPredicate).map(
        Map.Entry::getValue).collect(Collectors.toList());
  }
}
