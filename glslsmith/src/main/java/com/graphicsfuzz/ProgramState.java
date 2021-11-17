package com.graphicsfuzz;

import com.graphicsfuzz.common.ast.TranslationUnit;
import com.graphicsfuzz.common.ast.decl.ArrayInfo;
import com.graphicsfuzz.common.ast.expr.IntConstantExpr;
import com.graphicsfuzz.common.ast.type.ArrayType;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.BindingLayoutQualifier;
import com.graphicsfuzz.common.ast.type.LayoutQualifierSequence;
import com.graphicsfuzz.common.ast.type.Std430LayoutQualifier;
import com.graphicsfuzz.common.ast.type.TypeQualifier;
import com.graphicsfuzz.common.tool.PrettyPrinterVisitor;
import com.graphicsfuzz.common.util.ShaderKind;
import com.graphicsfuzz.config.ConfigInterface;
import com.graphicsfuzz.postprocessing.Wrapper;
import com.graphicsfuzz.scope.FuzzerScope;
import com.graphicsfuzz.scope.FuzzerScopeEntry;
import com.graphicsfuzz.scope.UnifiedTypeInterface;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutableTriple;


public class ProgramState {
  // General attributes
  private TranslationUnit translationUnit;
  private final ConfigInterface configInterface;

  // Scope management
  private FuzzerScope currentScope = new FuzzerScope();
  private int scopeDepth = 0;

  // Indices management for variables
  private int bindingOffset = 0;
  private int bufferAndUniformOffset = 0;

  // Variable to query the shader generation states
  private int exprDepth = 0;
  private boolean lvalue = false;
  private boolean constant = true;
  private boolean shiftOperation = false;
  private int initializer = 0;
  private int funCall = 0;
  private boolean outParam = false;
  private int swizzleDepth = 0;
  private int switchDepth = 0;
  private int loopDepth = 0;
  private int forDepth = 0;
  private FuzzerScopeEntry currentLValueVariable = null;

  // Variables to handle the undefined behaviour on calling orders
  private final Set<FuzzerScopeEntry> currentInitExprReadEntries = new HashSet<>();
  private final Set<FuzzerScopeEntry> currentInitExprWrittenEntries = new HashSet<>();
  private final Set<FuzzerScopeEntry> seenInitReadEntries = new HashSet<>();
  private final Set<FuzzerScopeEntry> seenInitWrittenEntries = new HashSet<>();
  private final Stack<Set<FuzzerScopeEntry>> seenFunCallEntries = new Stack<>();
  private final Set<FuzzerScopeEntry> seenInFunCallArg = new HashSet<>();

  // Referencing the necessary safe wrappers for later generation
  private final Set<ImmutableTriple<Wrapper, BasicType, BasicType>> necessaryWrappers =
      new HashSet<>();

  // API populated uniforms and buffer
  private final Map<String, Buffer> bufferTable = new LinkedHashMap<>();

  // unique ID counter for wrapper calls generation
  private int wrapperCounter = 0;
  private ArrayList<Boolean> ids = new ArrayList<>();

  public ProgramState(ConfigInterface configInterface) {
    this.configInterface = configInterface;
  }

  //TODO add support for uniforms

  public ConfigInterface getConfigInterface() {
    return configInterface;
  }

  public ConfigInterface.RunType getRunType() {
    return configInterface.getRunType();
  }

  public int getLoopDepth() {
    return loopDepth;
  }

  public int getForDepth() {
    return forDepth;
  }

  public int getSwitchDepth() {
    return switchDepth;
  }

  public TranslationUnit getTranslationUnit() {
    return translationUnit;
  }

  //General attributes management
  public ShaderKind getShaderKind() {
    return configInterface.getShaderKind();
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

  public String getShadingLanguageVersion() {
    return translationUnit.getShadingLanguageVersion().getVersionString();
  }

  public void programInitialization(TranslationUnit translationUnit) {
    this.translationUnit = translationUnit;
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

  public boolean hasEntryBeenRead(FuzzerScopeEntry entry) {
    return seenInitWrittenEntries.contains(entry);
  }

  public void setEntryHasBeenRead(FuzzerScopeEntry entry) {
    if (!configInterface.allowMultipleWriteAccessInInitializers() && initializer > 0) {
      currentInitExprReadEntries.add(entry);
    }
  }

  public void finishInitParam() {
    seenInitReadEntries.addAll(currentInitExprReadEntries);
    seenInitWrittenEntries.addAll(currentInitExprWrittenEntries);
    currentInitExprReadEntries.clear();
    currentInitExprWrittenEntries.clear();
  }

  public void finishFunCallArg() {
    if (outParam) {
      seenFunCallEntries.peek().addAll(seenInFunCallArg);
      seenFunCallEntries.clear();
    }
  }

  public void setEntryHasBeenWritten(FuzzerScopeEntry entry) {
    if (!configInterface.allowMultipleWriteAccessInInitializers() && initializer > 0) {
      currentInitExprWrittenEntries.add(entry);
    }
    if (funCall > 0 && outParam) {
      seenInFunCallArg.add(entry);
    }
  }

  public FuzzerScopeEntry getScopeEntryByName(String variableName) {
    return currentScope.getScopeEntryByName(variableName);
  }

  public List<FuzzerScopeEntry> getWriteEntriesOfCompatibleType(BasicType type) {
    List<FuzzerScopeEntry> scopeEntries = currentScope.getWriteEntriesOfCompatibleType(type);
    return filterWriteAvailableEntries(scopeEntries);
  }

  public List<FuzzerScopeEntry> getReadEntriesOfCompatibleType(BasicType type) {
    List<FuzzerScopeEntry> scopeEntries = currentScope.getReadEntriesOfCompatibleType(type);
    if (! configInterface.allowMultipleWriteAccessInInitializers() && initializer > 0) {
      return scopeEntries.stream().filter(
          t -> !seenInitWrittenEntries.contains(t)
      ).collect(Collectors.toList());
    }
    return scopeEntries;
  }

  public List<FuzzerScopeEntry> getReadEntriesOfCompatibleType(BasicType baseType,
                                                               int baseTypeSize) {
    return getReadEntriesOfCompatibleType(baseType).stream()
        .filter(t -> t.getBaseTypeSize() == baseTypeSize
            && t.getBaseType() == baseType).collect(Collectors.toList());
  }

  public List<FuzzerScopeEntry> getWriteAvailableEntries() {
    List<FuzzerScopeEntry> scopeEntries = currentScope.getWriteAvailableEntries();
    return filterWriteAvailableEntries(scopeEntries);
  }

  public List<FuzzerScopeEntry> getWriteAvailableArrayEntries() {
    return getWriteAvailableEntries().stream()
        .filter(t -> t.getBaseTypeSize() > 1).collect(Collectors.toList());
  }

  public void enterLoop() {
    this.loopDepth++;
  }

  public void enterFor() {
    this.forDepth++;
  }

  public void exitLoop() {
    this.loopDepth--;
  }

  public void exitFor() {
    this.forDepth--;
  }

  public void enterSwitch() {
    this.switchDepth++;
  }

  public void exitSwitch() {
    this.switchDepth--;
  }

  private List<FuzzerScopeEntry> filterWriteAvailableEntries(List<FuzzerScopeEntry> scopeEntries) {
    if (! configInterface.allowMultipleWriteAccessInInitializers() && initializer > 0) {
      scopeEntries = scopeEntries.stream().filter(
          t -> !(seenInitReadEntries.contains(t)
              || seenInitWrittenEntries.contains(t))).collect(Collectors.toList());
    }
    if (funCall > 0 && outParam) {
      scopeEntries = scopeEntries.stream().filter(
          t -> seenFunCallEntries.peek().contains(t)).collect(Collectors.toList());
    }
    return scopeEntries;
  }


  //Functions to add variable to the current scope
  public void addUniformBufferVariable(String name, UnifiedTypeInterface variable) {
    assert scopeDepth == 0;
    bufferAndUniformOffset += 1;
    currentScope.addVariable(name, variable, false, false);
  }

  public void addVariable(String name, UnifiedTypeInterface variable) {
    this.addVariable(name, variable, true, false);
  }

  public void addVariable(String name, UnifiedTypeInterface variable, boolean incrementOffset,
                          boolean incrementShadowOffset) {
    currentScope.addVariable(name, variable, incrementOffset, incrementShadowOffset);
  }

  //Name and offset get function
  public int getBindingOffset() {
    return bindingOffset;
  }

  public String getNextUniformBufferName() {
    return "ext_" + bufferAndUniformOffset;
  }

  public boolean isAShadowNameStillAvailable() {
    return currentScope.getAvailableShadowOffset() < currentScope.getFirstAvailableOffset();
  }

  public String getAvailableShadowName() {
    assert currentScope.getAvailableShadowOffset() < currentScope.getFirstAvailableOffset();
    return "var_" + currentScope.getAvailableShadowOffset();
  }

  public String getAvailableNoShadowName() {
    return "var_" + currentScope.getAvailableOffset();
  }

  //Querying the shader generation states
  public boolean isLValue() {
    return lvalue;
  }

  public boolean isSideEffectOpPermitted() {
    return lvalue && !currentLValueVariable.isWriteOnly();
  }

  public void enterInitializer() {
    initializer++;
  }

  public void exitInitializer() {
    initializer--;
    assert initializer >= 0;
    if (initializer == 0) {
      seenInitWrittenEntries.clear();
      seenInitReadEntries.clear();
    }
  }

  public void enterFunCall() {
    funCall++;
    seenFunCallEntries.push(new HashSet<>());
  }

  public void exitFunCall() {
    funCall--;
    assert funCall >= 0;
    seenFunCallEntries.pop();
  }

  public void setOutParam(boolean outParam) {
    this.outParam = outParam;
  }

  public boolean isOutParam() {
    return outParam;
  }

  public FuzzerScopeEntry getCurrentLValueVariable() {
    return currentLValueVariable;
  }

  public void setLvalue(boolean lvalue, FuzzerScopeEntry variable) {
    this.currentLValueVariable = variable;
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
  public void registerWrapper(Wrapper op, BasicType typeA, BasicType typeB) {
    necessaryWrappers.add(new ImmutableTriple<>(op, typeA, typeB));
    if (op == Wrapper.SAFE_MOD || op == Wrapper.SAFE_MOD_ASSIGN) {
      if (typeA.getElementType() == BasicType.INT) {
        registerWrapper(Wrapper.SAFE_ABS, typeA, null);
      }
      if (typeB != null && typeB.getElementType() == BasicType.INT) {
        registerWrapper(Wrapper.SAFE_ABS, typeB, null);
      }
    }
    if (op == Wrapper.SAFE_BITFIELD_EXTRACT || op == Wrapper.SAFE_BITFIELD_INSERT) {
      registerWrapper(Wrapper.SAFE_ABS, BasicType.INT, null);
    }
  }

  public Set<ImmutableTriple<Wrapper, BasicType, BasicType>> getWrappers() {
    return necessaryWrappers;
  }

  //API populated uniforms and buffer
  public void addBuffer(Buffer buffer) {
    assert scopeDepth == 0;
    bindingOffset += 1;
    bufferTable.put(buffer.getName(), buffer);
  }

  public boolean hasBuffer(String name) {
    return bufferTable.containsKey(name);
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

  // Wrapper counter
  public boolean hasIdsBuffer() {
    return wrapperCounter >= 1;
  }

  public Buffer getIdsBuffer() {
    assert wrapperCounter >= 1;
    int idsBufferBinding = getBindingOffset();
    int nbIds = wrapperCounter;
    ArrayInfo idsInfo = new ArrayInfo(
        Collections.singletonList(Optional.of(new IntConstantExpr(String.valueOf(nbIds)))));
    idsInfo.setConstantSizeExpr(0, nbIds);
    return new Buffer("buffer_ids",
        new LayoutQualifierSequence(new BindingLayoutQualifier(idsBufferBinding),
            new Std430LayoutQualifier()),
        new ArrayList<>(Collections.nCopies(nbIds, 1)),
        Collections.singletonList(TypeQualifier.BUFFER), Collections.singletonList("ids"),
        Collections.singletonList(new ArrayType(BasicType.INT, idsInfo)),
        "", false, getBindingOffset());
  }

  public int wrapperCounterPostIncrement() {
    wrapperCounter++;
    return wrapperCounter - 1;
  }

  public void setIds(ArrayList<Boolean> ids) {
    this.ids = ids;
  }

  public boolean lookupIds(int index) {
    assert getRunType() == ConfigInterface.RunType.REDUCED_WRAPPERS;
    assert ids.size() != 0;
    return ids.get(index);
  }
}
