package com.graphicsfuzz;

import com.graphicsfuzz.common.ast.TranslationUnit;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.tool.PrettyPrinterVisitor;
import com.graphicsfuzz.common.util.ShaderKind;
import com.graphicsfuzz.scope.FuzzerScope;
import com.graphicsfuzz.scope.FuzzerScopeEntry;
import com.graphicsfuzz.scope.UnifiedTypeInterface;
import com.graphicsfuzz.shadergenerators.Wrapper;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutableTriple;


public class ProgramState {
  //General attributes
  private TranslationUnit translationUnit;
  private ShaderKind shaderKind;

  //Scope management
  private FuzzerScope currentScope = new FuzzerScope();
  private int scopeDepth = 0;

  //Indices management for variables
  private int bindingOffset = 0;
  private int bufferAndUniformOffset = 0;
  private int shadowAvailableVariableOffset = 0;
  private int nonShadowAvailableVariableOffset = 0;

  //Variable to query the shader generation states
  private int exprDepth = 0;
  private boolean lvalue = false;
  private boolean constant = true;
  private boolean shiftOperation = false;
  private int swizzleDepth = 0;

  //Referencing the necessary safe wrappers for later generation
  private final Set<ImmutableTriple<Wrapper.Operation, BasicType, BasicType>> necessaryWrappers =
      new HashSet<>();

  //API populated uniforms and buffer
  private final Map<String, Buffer> bufferTable = new LinkedHashMap<>();

  //TODO add support for uniforms

  //General attributes management
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

  //Scope management
  public int getScopeDepth() {
    return scopeDepth;
  }

  public void addScope() {
    scopeDepth += 1;
    currentScope = new FuzzerScope(currentScope);
  }

  public void exitScope() {
    scopeDepth -= 1;
    currentScope = currentScope.getParent();
  }

  public List<FuzzerScopeEntry> getReadEntriesOfCompatibleType(BasicType type) {
    return currentScope.getReadEntriesOfCompatibleType(type);
  }

  public List<FuzzerScopeEntry> getWriteAvailableEntries() {
    return currentScope.getWriteAvailableEntries();
  }

  //Functions to add variable to the current scope
  public void addUniformBufferVariable(String name, UnifiedTypeInterface variable) {
    assert scopeDepth == 0;
    bufferAndUniformOffset += 1;
    currentScope.addVariable(name, variable, false);
  }

  public void addVariable(String name, UnifiedTypeInterface variable) {
    this.addVariable(name, variable, true);
  }

  public void addVariable(String name, UnifiedTypeInterface variable, boolean canBeHidden) {
    if (canBeHidden) {
      shadowAvailableVariableOffset += 1;
    } else {
      nonShadowAvailableVariableOffset += 1;
    }
    currentScope.addVariable(name, variable, canBeHidden);
  }

  //Name and offset get function
  public int getBindingOffset() {
    return bindingOffset;
  }

  public String getNextUniformBufferName() {
    return "ext_" + bufferAndUniformOffset;
  }

  public String getAvailableShadowName() {
    //TODO shadowing problems
    return "var_" + shadowAvailableVariableOffset;
  }

  //Querying the shader generation states
  public boolean isLValue() {
    return lvalue;
  }

  public void setLvalue(boolean lvalue) {
    this.lvalue = lvalue;
  }

  public boolean isConstant() {
    return constant;
  }

  public void setConstant(boolean constant) {
    this.constant = constant;
  }

  public boolean isShiftOperation() {
    return shiftOperation;
  }

  public void setShiftOperation(boolean shiftOperation) {
    this.shiftOperation = shiftOperation;
  }

  public int getExprDepth() {
    return exprDepth;
  }

  public void incrementExprDepth() {
    exprDepth++;
  }

  public void decrementExprDepth() {
    exprDepth--;
  }

  public int getSwizzleDepth() {
    return swizzleDepth;
  }

  public void incrementSwizzleDepth() {
    swizzleDepth++;
  }

  public void decrementSwizzleDepth() {
    swizzleDepth--;
  }

  //Wrappers management
  public void registerWrapper(Wrapper.Operation op, BasicType typeA, BasicType typeB) {
    necessaryWrappers.add(new ImmutableTriple<>(op, typeA, typeB));
    if (op == Wrapper.Operation.SAFE_MOD || op == Wrapper.Operation.SAFE_MOD_ASSIGN) {
      if (typeA.getElementType() == BasicType.INT) {
        registerWrapper(Wrapper.Operation.SAFE_ABS, typeA, null);
      }
      if (typeB != null && typeB.getElementType() == BasicType.INT) {
        registerWrapper(Wrapper.Operation.SAFE_ABS, typeB, null);
      }
    }
  }

  public Set<ImmutableTriple<Wrapper.Operation, BasicType, BasicType>> getWrappers() {
    return necessaryWrappers;
  }

  //API populated uniforms and buffer
  public void addBuffer(Buffer buffer) {
    assert scopeDepth == 0;
    bindingOffset += 1;
    bufferTable.put(buffer.getName(), buffer);
  }

  public Buffer getBuffer(String name) {
    return bufferTable.get(name);
  }

  public List<Buffer> getBuffers() {
    return new ArrayList<>(bufferTable.values());
  }


  public List<Buffer> getBufferByPredicate(Predicate<Map.Entry<String, Buffer>>
                                               bufferPredicate) {
    return bufferTable.entrySet().stream().filter(bufferPredicate).map(
        Map.Entry::getValue).collect(Collectors.toList());
  }
}
