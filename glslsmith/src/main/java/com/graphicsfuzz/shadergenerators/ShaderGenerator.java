package com.graphicsfuzz.shadergenerators;

import static java.lang.Math.max;

import com.graphicsfuzz.Buffer;
import com.graphicsfuzz.ProgramState;
import com.graphicsfuzz.common.ast.decl.ArrayInfo;
import com.graphicsfuzz.common.ast.decl.Initializer;
import com.graphicsfuzz.common.ast.decl.InterfaceBlock;
import com.graphicsfuzz.common.ast.decl.VariableDeclInfo;
import com.graphicsfuzz.common.ast.decl.VariablesDeclaration;
import com.graphicsfuzz.common.ast.expr.ArrayConstructorExpr;
import com.graphicsfuzz.common.ast.expr.ArrayIndexExpr;
import com.graphicsfuzz.common.ast.expr.BinOp;
import com.graphicsfuzz.common.ast.expr.BinaryExpr;
import com.graphicsfuzz.common.ast.expr.BoolConstantExpr;
import com.graphicsfuzz.common.ast.expr.Expr;
import com.graphicsfuzz.common.ast.expr.FloatConstantExpr;
import com.graphicsfuzz.common.ast.expr.FunctionCallExpr;
import com.graphicsfuzz.common.ast.expr.IntConstantExpr;
import com.graphicsfuzz.common.ast.expr.MemberLookupExpr;
import com.graphicsfuzz.common.ast.expr.ParenExpr;
import com.graphicsfuzz.common.ast.expr.TernaryExpr;
import com.graphicsfuzz.common.ast.expr.TypeConstructorExpr;
import com.graphicsfuzz.common.ast.expr.UIntConstantExpr;
import com.graphicsfuzz.common.ast.expr.UnOp;
import com.graphicsfuzz.common.ast.expr.UnaryExpr;
import com.graphicsfuzz.common.ast.expr.VariableIdentifierExpr;
import com.graphicsfuzz.common.ast.stmt.BlockStmt;
import com.graphicsfuzz.common.ast.stmt.BreakStmt;
import com.graphicsfuzz.common.ast.stmt.ContinueStmt;
import com.graphicsfuzz.common.ast.stmt.DeclarationStmt;
import com.graphicsfuzz.common.ast.stmt.DefaultCaseLabel;
import com.graphicsfuzz.common.ast.stmt.DoStmt;
import com.graphicsfuzz.common.ast.stmt.ExprCaseLabel;
import com.graphicsfuzz.common.ast.stmt.ExprStmt;
import com.graphicsfuzz.common.ast.stmt.ForStmt;
import com.graphicsfuzz.common.ast.stmt.IfStmt;
import com.graphicsfuzz.common.ast.stmt.LoopStmt;
import com.graphicsfuzz.common.ast.stmt.NullStmt;
import com.graphicsfuzz.common.ast.stmt.Stmt;
import com.graphicsfuzz.common.ast.stmt.SwitchStmt;
import com.graphicsfuzz.common.ast.stmt.WhileStmt;
import com.graphicsfuzz.common.ast.type.ArrayType;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.VoidType;
import com.graphicsfuzz.common.util.IRandom;
import com.graphicsfuzz.config.ConfigInterface;
import com.graphicsfuzz.config.FuzzerConstants;
import com.graphicsfuzz.functions.FunctionRegistry;
import com.graphicsfuzz.functions.FunctionStruct;
import com.graphicsfuzz.random.IRandomType;
import com.graphicsfuzz.random.RandomTypeGenerator;
import com.graphicsfuzz.scope.FuzzerScopeEntry;
import com.graphicsfuzz.scope.UnifiedTypeInterface;
import com.graphicsfuzz.scope.UnifiedTypeProxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public abstract class ShaderGenerator {
  protected IRandom randGen;
  protected ProgramState programState;
  protected IRandomType randomTypeGenerator;
  protected ConfigInterface configuration;
  protected FunctionRegistry registry;

  public void generateShader() {
    resetProgramState();
  }

  public ShaderGenerator(IRandom randomGenerator, ConfigInterface configuration) {
    this(randomGenerator, new RandomTypeGenerator(randomGenerator, configuration), configuration);
  }

  public ShaderGenerator(IRandom randomGenerator, IRandomType randomTypeGenerator,
                         ConfigInterface configuration) {
    this.randGen = randomGenerator;
    this.randomTypeGenerator = randomTypeGenerator;
    this.configuration = configuration;
    this.programState = new ProgramState(configuration);
    this.registry = new FunctionRegistry(randGen, programState.getShaderKind());
  }

  public void resetProgramState() {
    programState = new ProgramState(configuration);
  }

  public ProgramState getProgramState() {
    return programState;
  }

  //All generate instructions return classes inherited from IASTNodes
  //TODO support named instance
  protected InterfaceBlock generateInterfaceBlockFromBuffer(Buffer buffer) {
    return new InterfaceBlock(Optional.ofNullable(buffer.getLayoutQualifiers()),
        buffer.getInterfaceQualifiers(),
        buffer.getName(),
        buffer.getMemberNames(),
        buffer.getMemberTypes(),
        Optional.of("")
    );
  }

  protected Expr generateArrayConstructorExpr(BasicType elementType, int arraySize,
                                              boolean constOnly) {
    List<Expr> initializerExprs = new ArrayList<>();

    // Taint generation to prevent from generating read / write access in initializers
    programState.enterInitializer();
    for (int i = 0; i < arraySize; i++) {
      if (constOnly) {
        initializerExprs.add(generateBaseConstantExpr(elementType));
      } else {
        initializerExprs.add(generateBaseExpr(elementType));
      }
      programState.finishInitParam();
    }
    programState.exitInitializer();

    return new ArrayConstructorExpr(new ArrayType(elementType,
        new ArrayInfo(Collections.singletonList(Optional.of(
            new IntConstantExpr(String.valueOf(arraySize)))))),
        initializerExprs);
  }

  //Generate a Variable declaration
  //TODO generate the full range of possible const values
  protected VariablesDeclaration generateRandomTypedVarDecls(int varDeclNumber,
                                                             boolean hasInitializer) {
    List<VariableDeclInfo> varDeclInfos = new ArrayList<>();

    //Get a random new type from those available to the generator (boolean, signed/unsigned
    // integers) and vectors) it can either be an array or a BasicType
    UnifiedTypeInterface proxy = randomTypeGenerator.getRandomQualifiedProxyType();
    BasicType baseType = proxy.getBaseType();

    //Create a correct initializer for the selected type
    Initializer initializer = null;
    if (hasInitializer) {

      // Initializer for the arrays
      if (proxy.isArray()) {
        //Build a complete array declaration
        initializer = new Initializer(generateArrayConstructorExpr(proxy.getBaseType(),
            proxy.getBaseTypeSize(), proxy.isConstOnly()));
      } else {
        // Generate any random expression available from the underlying basic type
        if (proxy.isConstOnly()) {
          initializer = new Initializer(generateBaseConstantExpr(baseType));
        } else {
          initializer = new Initializer(generateBaseExpr(baseType));
        }
      }
    }

    // Add the necessary array of the correct size
    // TODO handle multiple dimension arrays
    ArrayInfo info = proxy.isArray() ? new ArrayInfo(
        Collections.singletonList(Optional.of(
            new IntConstantExpr(String.valueOf(proxy.getBaseTypeSize()))))) : null;

    // For the number of variables declared here, get a new variable and register it in the
    // program state
    for (int i = 0; i < varDeclNumber; i++) {
      String name;
      // forbidden shadowing in loops for llvmpipe defect
      if (programState.isAShadowNameStillAvailable() && randGen.nextBoolean()
          && programState.getForDepth() == 0) {
        name = programState.getAvailableShadowName();
        programState.addVariable(name, proxy, false, true);
      } else {
        name = programState.getAvailableNoShadowName();
        programState.addVariable(name, proxy, true, false);
      }

      // Ensure that variables are seen as being written in the current scope
      if (hasInitializer) {
        programState.setEntryHasBeenWritten(programState.getScopeEntryByName(name));
      }
      varDeclInfos.add(new VariableDeclInfo(name, info, initializer));
    }
    return new VariablesDeclaration(proxy.getRealType(), varDeclInfos);
  }

  protected Pair<Expr, Number> generateScalarConstantExprValue(BasicType type) {
    return generateScalarConstantExprValue(type, 0L);
  }

  protected Pair<Expr, Number> generateScalarConstantExprValue(BasicType type, Long bound) {
    assert type.isScalar();
    if (type.equals(BasicType.UINT)) {
      if (programState.isShiftOperation()) {
        Integer value = randGen.nextInt(32);
        return new ImmutablePair<>(new UIntConstantExpr(value + "u"), value);
      } else if (bound != 0L) {
        Long value = randGen.nextLong(bound);
        return new ImmutablePair<>(new UIntConstantExpr(value + "u"), value);
      } else {
        Long value = randGen.nextLong(FuzzerConstants.MAX_UINT_VALUE);
        return new ImmutablePair<>(new UIntConstantExpr(value + "u"), value);
      }
    } else if (type.equals(BasicType.FLOAT)) {
      Float value;
      if (bound != 0L) {
        value = randGen.nextFloat(bound.floatValue());
      } else {
        //TODO add fuzzer constant
        value = randGen.nextFloat(-(1 << 12), (1 << 12));
      }
      return new ImmutablePair<>(new FloatConstantExpr(String.format("%.1f", value) + "f"), value);
    } else {

      //Generates a int between 0 and 32 if we are on a shift operation else generate on full range
      assert type.equals(BasicType.INT);
      Integer value;
      if (programState.isShiftOperation()) {
        value = randGen.nextInt(32);
      } else if (bound != 0L) {
        value = randGen.nextInt(bound.intValue());
      }  else {
        value = randGen.nextInt(FuzzerConstants.MIN_INT_VALUE, FuzzerConstants.MAX_INT_VALUE);
      }
      return new ImmutablePair<>(new IntConstantExpr(String.valueOf(value)), value);
    }

  }

  //Arithmetic Base generation
  protected Expr generateBaseConstantExpr(BasicType type) {
    // Sets the correct flags in the program state for further recursive call (ie: no lvalue +
    // constant)
    programState.setLvalue(false, null);
    programState.setConstant(true);

    // Randomly generate a constructor such as ivec4(3) or ivec4(1,2,3,4) by combining the base
    // Constant type for the underlying Basic type
    // TODO maybe use a generateVectorInstruction to reduce recursive calls
    if (type.isVector()) {
      int numberOfValues = randGen.nextBoolean() ? type.getNumElements() : 1;
      List<Expr> args = new ArrayList<>();
      for (int i = 0; i < numberOfValues; i++) {
        //Recursive call with the basic type
        args.add(generateBaseConstantExpr(type.getElementType()));
      }
      return new TypeConstructorExpr(type.getText(), args);

      //Generates a uint between 0u and 32u if we are on a shift operation else generate on full
      // range (uint are represented by longs in java)
    } else if (type.equals(BasicType.BOOL)) {
      return new BoolConstantExpr(randGen.nextBoolean());
    } else {
      return generateScalarConstantExprValue(type).getLeft();
    }
  }


  protected Expr generateBaseUnaryExpr(BasicType type) {
    // Generate the inner expression first
    Expr nextExpr = generateBaseExpr(type);

    // Early exit for boolean values (only not is a valid unary operation)
    if (type.equals(BasicType.BOOL)) {
      return new UnaryExpr(new ParenExpr(nextExpr), UnOp.LNOT);
    }

    // Generate an appropriate unary operation (taking into account if the inner expression is a
    // lvalue). Floats are isolated from integers (no ~ operation)
    UnOp unOp;
    if (type.getElementType().equals(BasicType.FLOAT)) {
      unOp = randomTypeGenerator.getRandomBaseFloatUnaryOp(programState.isSideEffectOpPermitted());
    } else {
      unOp = randomTypeGenerator.getRandomBaseIntUnaryOp(programState.isSideEffectOpPermitted());
    }

    // Updates the variable state for further actions if the value has been written (++)
    if (unOp.isSideEffecting()) {
      programState.setEntryHasBeenWritten(programState.getCurrentLValueVariable());
    }

    // Updates the program state flags (no, lvalue, no constant)
    programState.setLvalue(false, null);
    programState.setConstant(false);

    return new UnaryExpr(nextExpr, unOp);
  }

  protected Expr generateBaseBinaryExpr(BasicType returnType) {
    // Use the return type to determine suitable types for the left operand on vector types
    // (either vector or element from vector type) or not boolean-type for boolean return type
    BasicType leftType = randomTypeGenerator.getAvailableTypeFromReturnType(returnType);
    Expr leftExpr = generateBaseExpr(leftType);

    //Find a suitable operation according to return type and left-type
    // return: boolean, left: boolean => boolean operation
    // return: float => subset of float operation
    // return: boolean, left: int, uint, ... => comparison operation
    // return: vector, left: element type => arithmetic operation without lvalue
    // return: vector, int, uint, left: same type => arithmetic operation with possible lvalue
    BinOp op;
    if (returnType.equals(BasicType.BOOL) && leftType.equals(BasicType.BOOL)) {
      op = randomTypeGenerator.getRandomBaseBoolBinaryOp();
    } else if (returnType.getElementType().equals(BasicType.BOOL)) {
      op = randomTypeGenerator.getRandomComparisonOp();
    } else if (returnType.getElementType().equals(BasicType.FLOAT)) {
      if (returnType.isVector() && leftType.isScalar()) {
        op = randomTypeGenerator.getRandomBaseFloatBinaryOp(returnType, false);
      } else {
        op = randomTypeGenerator.getRandomBaseFloatBinaryOp(returnType,
            programState.isSideEffectOpPermitted());
      }
    } else if (returnType.isVector() && leftType.isScalar()) {
      op = randomTypeGenerator.getRandomBaseIntVectBinaryOp();
    } else {
      op = randomTypeGenerator.getRandomBaseIntBinaryOp(returnType, leftType,
          programState.isSideEffectOpPermitted());
    }

    // Updates the variable state in the program state if it has been written
    if (op.isSideEffecting()) {
      programState.setEntryHasBeenWritten(programState.getCurrentLValueVariable());
    }
    // Sets flags fpr shift operations to limit the range of constant generations in the right
    // operand
    programState.setShiftOperation(
        op == BinOp.SHL || op == BinOp.SHR || op == BinOp.SHL_ASSIGN || op == BinOp.SHR_ASSIGN);
    BasicType rightType = randomTypeGenerator.getAvailableTypeFromOp(leftType, op, returnType);
    Expr rightExpr = generateBaseExpr(rightType);

    // Sets the flags after full binary expression generation
    programState.setLvalue(false, null);
    programState.setShiftOperation(false);
    boolean wasConstant = programState.isConstant();
    programState.setConstant(false);

    // Enforces meaningful priority rules on operand by adding extra parentheses where needed
    // For boolean test if left and / or right operands need parenthesis (type change)
    if (returnType.equals(BasicType.BOOL)) {
      List<BinOp> lessPriorityOp = Arrays.asList(BinOp.EQ, BinOp.NE, BinOp.BOR, BinOp.BXOR,
          BinOp.BAND);
      boolean leftNeedParent = ShaderSimplifier.doesExprContainsBinOp(leftExpr, lessPriorityOp);
      boolean rightNeedParent = ShaderSimplifier.doesExprContainsBinOp(rightExpr, lessPriorityOp);
      return new BinaryExpr(leftNeedParent ? new ParenExpr(leftExpr) : leftExpr,
          rightNeedParent ? new ParenExpr(rightExpr) : rightExpr, op);
    }
    // Side effecting operations always need parentheses to be used in outer expressions
    //TODO verify if the wasConstant is necessary there
    if (op.isSideEffecting() || wasConstant) {
      return new ParenExpr(new BinaryExpr(leftExpr, rightExpr, op));
    }
    // Verify type changes in shift operations (the full right operand is then put into parentheses)
    if (op == BinOp.SHL || op == BinOp.SHR || op == BinOp.SHL_ASSIGN || op == BinOp.SHR_ASSIGN
        && leftType != rightType) {
      return new ParenExpr(new BinaryExpr(leftExpr, new ParenExpr(rightExpr), op));
    }
    return new BinaryExpr(leftExpr, rightExpr, op);
  }

  //Generate a variable access for the given type or crash if none is available
  protected Expr generateBaseVarExpr(BasicType type) {
    return generateBaseVarExpr(type, false);
  }

  protected Expr generateBaseVarExpr(BasicType type, boolean ensureLValue) {
    // If we are returning a value which is made to be written we write it as being written)
    List<FuzzerScopeEntry> availableEntries = ensureLValue
        ? programState.getWriteEntriesOfCompatibleType(type) :
        programState.getReadEntriesOfCompatibleType(type);
    assert !availableEntries.isEmpty();
    FuzzerScopeEntry var = availableEntries.get(randGen.nextInt(availableEntries.size()));
    boolean lvalue = true;
    if (programState.hasEntryBeenRead(var) || var.isReadOnly()) {
      lvalue = false;
    } else {
      programState.setEntryHasBeenRead(var);
      if (ensureLValue) {
        programState.setEntryHasBeenWritten(var);
      } else if (var.getBaseType().isVector()) {
        if (type.getNumElements() > var.getBaseType().getNumElements()) {
          lvalue = false;
        } else {
          lvalue = randGen.nextBoolean();
        }
      }
    }

    Expr randomAccessExpr = generateRandomAccessExpr(var, type, lvalue);
    programState.setLvalue(lvalue, lvalue ? var : null);
    programState.setConstant(false);
    return randomAccessExpr;
  }

  protected Expr generateTerminalExpr(BasicType type) {
    if (randGen.nextBoolean() && !programState
        .getReadEntriesOfCompatibleType(type).isEmpty()) {
      return generateBaseVarExpr(type);
    }
    return generateBaseConstantExpr(type);
  }

  protected Expr generateBaseNonTerminalExpr(BasicType type) {
    switch (randGen.nextInt(6)) {
      case 0:
        return generateBaseBinaryExpr(type);
      case 1:
        return new ParenExpr(generateBaseBinaryExpr(type));
      case 2:
        return generateTernaryExpr(type);
      case 3:
        return generateBaseUnaryExpr(type);
      case 4:
        return generateFunCallExpr(type);
      default:
        return new ParenExpr(generateBaseUnaryExpr(type));
    }
  }

  protected Expr generateFunCallExpr(BasicType type) {
    return generateFunCallExpr(new UnifiedTypeProxy(type));
  }

  protected Expr generateFunCallExpr(UnifiedTypeProxy type) {
    FunctionStruct funCallStruct = null;
    List<Expr> parameters = new ArrayList<>();
    // We set in the programState that we are generating a funcall, ie store values if the
    // variable is declared as out later and no flush until the end of the functionCall generation
    programState.enterFunCall();
    boolean allParametersNotAvailable = true;
    while (allParametersNotAvailable) {
      funCallStruct = registry.getRandomFunctionStruct(type);
      allParametersNotAvailable = false;
      parameters.clear();
      for (UnifiedTypeProxy parameterType : funCallStruct.parameterTypes) {
        if (parameterType.isVoid()) {
          assert parameters.isEmpty();
          break;
        }
        if (parameterType.isOut()) {
          programState.setOutParam(true);
          if (programState.getWriteEntriesOfCompatibleType(parameterType.getBaseType()).isEmpty()) {
            allParametersNotAvailable = true;
            break;
          }
          parameters.add(generateBaseVarExpr(parameterType.getBaseType(), true));
          programState.finishFunCallArg();
          programState.setOutParam(false);
        } else {
          parameters.add(generateBaseExpr(parameterType.getBaseType()));
        }
      }
    }
    programState.exitFunCall();
    programState.setLvalue(false, null);
    programState.setConstant(false);
    return new FunctionCallExpr(funCallStruct.name, parameters);
  }

  protected Expr generateTernaryExpr(BasicType type) {
    Expr condExpr = generateBaseExpr(BasicType.BOOL);
    Expr ifExpr = generateBaseExpr(type);
    Expr elseExpr = generateBaseExpr(type);
    programState.setLvalue(false, null);
    programState.setConstant(false);
    return new ParenExpr(new TernaryExpr(condExpr, ifExpr, elseExpr));
  }

  protected Expr generateBaseExpr(BasicType type) {
    if (programState.getExprDepth() < configuration.getMaxExprDepth()
        && !(type.isVector() && type.getElementType().isBoolean())) {
      programState.incrementExprDepth();
      int nextAction =
          randGen.nextInt(configuration.getMaxExprDepth() + 1 - programState.getExprDepth());
      Expr expr = nextAction >= 1 ? generateBaseNonTerminalExpr(type) :
          generateTerminalExpr(type);
      programState.decrementExprDepth();
      return expr;
    } else {
      return generateTerminalExpr(type);
    }
  }

  protected Expr generateArrayAssignmentLine() {
    // Obtain an array which can be written to
    List<FuzzerScopeEntry> arrayScopeEntries = programState.getWriteAvailableArrayEntries();
    FuzzerScopeEntry var = arrayScopeEntries.get(randGen.nextInt(arrayScopeEntries.size()));

    // Check if a second array of the same type and size exists
    List<FuzzerScopeEntry> readableEntries =
        programState.getReadEntriesOfCompatibleType(var.getBaseType(), var.getBaseTypeSize());

    if (!readableEntries.isEmpty() && randGen.nextBoolean()) {
      // Randomly add a copy operation
      return new BinaryExpr(new VariableIdentifierExpr(var.getName()),
          new VariableIdentifierExpr(
              readableEntries.get(randGen.nextInt(readableEntries.size())).getName()),
          BinOp.ASSIGN);
    } else {
      // Write an array access
      return new BinaryExpr(new VariableIdentifierExpr(var.getName()),
          generateArrayConstructorExpr(var.getBaseType(), var.getBaseTypeSize(),
              randGen.nextBoolean()), BinOp.ASSIGN);
    }
  }

  protected Expr generateProgramAssignmentLine() {
    if (programState.getWriteAvailableArrayEntries().size() > 0 && randGen.nextInt(7) < 2) {
      return generateArrayAssignmentLine();
    } else {
      return generateBaseTypeAssignmentLine();
    }
  }

  protected Expr generateBaseTypeAssignmentLine() {
    programState.setConstant(false);
    programState.setLvalue(false, null);
    List<FuzzerScopeEntry> scopeEntries = programState.getWriteAvailableEntries();
    FuzzerScopeEntry var = scopeEntries.get(randGen.nextInt(scopeEntries.size()));
    BinOp op;
    if (var.getBaseType().getElementType().equals(BasicType.BOOL) || var.isWriteOnly()) {
      op = BinOp.ASSIGN;
    } else if (var.getBaseType().getElementType().equals(BasicType.FLOAT)) {
      op = randomTypeGenerator.getRandomBaseFloatAssignOp();
    } else {
      op = randomTypeGenerator.getRandomBaseIntAssignOp();
    }
    programState.setEntryHasBeenWritten(var);
    if (op != BinOp.ASSIGN) {
      programState.setEntryHasBeenRead(var);
    }
    programState.setShiftOperation(op == BinOp.SHL_ASSIGN || op == BinOp.SHR_ASSIGN);
    BasicType returnType = randomTypeGenerator.getRandomTargetType(var.getBaseType());
    BasicType rightType = randomTypeGenerator.getAvailableTypeFromOp(returnType, op, returnType);
    Expr rightExpr = generateBaseExpr(rightType);
    return new BinaryExpr(generateRandomAccessExpr(var, returnType), rightExpr, op);
  }

  protected Expr generateRandomAccessExpr(FuzzerScopeEntry var, BasicType targetType) {
    return generateRandomAccessExpr(var, targetType, true);
  }

  protected Expr generateRandomAccessExpr(FuzzerScopeEntry var, BasicType targetType,
                                          boolean lvalue) {
    Expr childExpr = new VariableIdentifierExpr(var.getName());
    if (var.isArray()) {
      boolean previousLvalue = programState.isLValue();
      FuzzerScopeEntry previousVar = programState.getCurrentLValueVariable();
      Expr indexExpr = generateBaseExpr(BasicType.INT);
      programState.setLvalue(previousLvalue, previousVar);
      childExpr = new ArrayIndexExpr(new VariableIdentifierExpr(var.getName()),
          indexExpr);
    }
    if (var.getBaseType().isVector()) {
      if (var.getBaseType().getNumElements() == targetType.getNumElements()) {
        return randGen.nextBoolean() ? childExpr : generateRandomSwizzle(childExpr,
            var.getBaseType(), targetType, lvalue);
      }
      if (targetType.getNumElements() == 1 && randGen.nextBoolean()) {
        BasicType tempTargetType = lvalue ? BasicType.makeVectorType(targetType,
            randGen.nextInt(2, var.getBaseType().getNumElements() + 1)) :
            BasicType.makeVectorType(targetType,
                randGen.nextInt(2, 5));
        return new ArrayIndexExpr(generateRandomSwizzle(childExpr, var.getBaseType(),
            tempTargetType, lvalue),
            new IntConstantExpr(String.valueOf(randGen.nextInt(tempTargetType.getNumElements()))));
      }
      return generateRandomSwizzle(childExpr, var.getBaseType(), targetType, lvalue);
    }
    return randGen.nextBoolean() ? new ParenExpr(childExpr) : childExpr;
  }

  protected Expr generateRandomSwizzle(Expr childExpr, BasicType currentType, BasicType targetType,
                                       boolean lvalue) {
    boolean recurse = programState.getSwizzleDepth() < configuration.getMaxSwizzleDepth()
        && randGen.nextBoolean();
    int localSrcSize;
    if (!recurse) {
      localSrcSize = currentType.getNumElements();
    } else if (lvalue) {
      localSrcSize = randGen.nextInt(max(targetType.getNumElements(), 2),
          currentType.getNumElements() + 1);
    } else {
      localSrcSize = randGen.nextInt(2, 5);
    }
    int letterSet = randGen.nextInt(0, 3);
    List<String> setList;
    switch (letterSet) {
      case 0:
        setList = new ArrayList<>(Arrays.asList("x", "y", "z", "w"));
        break;
      case 1:
        setList = new ArrayList<>(Arrays.asList("r", "g", "b", "a"));
        break;
      default:
        setList = new ArrayList<>(Arrays.asList("s", "t", "p", "q"));
    }
    setList = setList.subList(0, localSrcSize);
    StringBuilder swizzleBuilder = new StringBuilder();
    for (int i = 0; i < targetType.getNumElements(); i++) {
      int randomIndex = randGen.nextInt(setList.size());
      swizzleBuilder.append(setList.get(randomIndex));
      if (lvalue) {
        setList.remove(randomIndex);
      }
    }
    if (recurse) {
      programState.incrementSwizzleDepth();
      childExpr = generateRandomSwizzle(childExpr, currentType,
          BasicType.makeVectorType(currentType.getElementType(), localSrcSize), lvalue);
      programState.decrementSwizzleDepth();
    }
    return new MemberLookupExpr(randGen.nextBoolean() ? new ParenExpr(childExpr) : childExpr,
        swizzleBuilder.toString());
  }

  protected Stmt generateIfStmt() {
    Expr ifExpr = generateBaseExpr(BasicType.BOOL);
    Stmt ifStmt = new BlockStmt(generateScope(), true);
    Stmt elseStmt = randGen.nextBoolean() ? new BlockStmt(generateScope(), true) : null;
    return new IfStmt(ifExpr, ifStmt, elseStmt);
  }

  protected Stmt generateSwitchStmt(boolean enforceCases) {
    // Generate the switch expr
    BasicType switchType = randomTypeGenerator.getRandomScalarInteger();
    Expr switchExpr = generateBaseExpr(switchType);

    programState.enterSwitch();
    // Prepares the body of the expression, already seen cases and the position of case stmts in
    // the switch (to generate default)
    final List<Stmt> switchBody = new ArrayList<>();
    final List<Integer> existingCases = new ArrayList<>();
    final List<Integer> casePositions = new ArrayList<>();

    int currentPos = 0;

    // Generates a random length
    int switchLength = randGen.nextInt(configuration.allowEmptySwitch() ? 0 : 1,
        configuration.getMaxSwitchCases());

    long offset = 0;
    if (enforceCases) {
      if (switchType.equals(BasicType.INT)) {
        offset = randGen.nextInt(FuzzerConstants.MIN_INT_VALUE, FuzzerConstants.MAX_INT_VALUE);
        switchExpr = new BinaryExpr(new IntConstantExpr(String.valueOf(offset)),
            new ParenExpr(new BinaryExpr(new FunctionCallExpr("abs", switchExpr),
                new IntConstantExpr(String.valueOf(switchLength)), BinOp.MOD)), BinOp.ADD);
      } else {
        offset = randGen.nextLong(FuzzerConstants.MAX_UINT_VALUE);
        switchExpr = new BinaryExpr(new IntConstantExpr(offset + "u"),
            new ParenExpr(new BinaryExpr(switchExpr,
                new UIntConstantExpr(switchLength + "u"), BinOp.MOD)), BinOp.ADD);
      }
    }

    for (int i = 0; i < switchLength; i++) {
      // Generate a base constant and ensures that the constant is not already used in a case
      Expr possibleBaseConstantExpr;
      if (enforceCases) {
        if (switchType.equals(BasicType.INT)) {
          possibleBaseConstantExpr =
              new IntConstantExpr(String.valueOf((offset + i) % FuzzerConstants.MAX_INT_VALUE));
        } else {
          possibleBaseConstantExpr =
              new UIntConstantExpr(((offset + i) % FuzzerConstants.MAX_UINT_VALUE) + "u");
        }
      } else {
        Pair<Expr, Number> possibleBaseConstantPair = generateScalarConstantExprValue(switchType);
        while (existingCases.contains(possibleBaseConstantPair.getRight().intValue())) {
          possibleBaseConstantPair = generateScalarConstantExprValue(switchType);
        }
        possibleBaseConstantExpr = possibleBaseConstantPair.getLeft();
        existingCases.add(possibleBaseConstantPair.getRight().intValue());
      }
      casePositions.add(currentPos);

      // Build the case stmt
      switchBody.add(new ExprCaseLabel(possibleBaseConstantExpr));
      switchBody.add(new BlockStmt(generateScope(i == switchLength - 1 ? 1 : 0,
          configuration.getMaxSwitchScopeLength()),
          true));

      // Move the cursor to add the next stmt and randomly add a breakStmt
      currentPos += 2;
      if (randGen.nextBoolean()) {
        switchBody.add(new BreakStmt());
        currentPos++;
      }

    }

    // Randomly add a default case to the switch
    if (configuration.enforceDefaultCase() || randGen.nextBoolean()) {

      // Add in an empty Switch directly and randomly add a break
      if (switchBody.isEmpty()) {
        switchBody.add(new DefaultCaseLabel());
        switchBody.add(new BlockStmt(generateScope(1, configuration.getMaxSwitchScopeLength()),
            true));
        if (randGen.nextBoolean()) {
          switchBody.add(new BreakStmt());
        }
      } else {

        // Pick a position randomly in the switch to add the default statement (after a complete
        // case stmt)
        int defaultIndex = randGen.nextInt(casePositions.size());
        switchBody.add(casePositions.get(defaultIndex), new DefaultCaseLabel());
        switchBody.add(new BlockStmt(generateScope(defaultIndex == casePositions.size() - 1 ? 1
            : 0, configuration.getMaxSwitchScopeLength()), true));
        if (randGen.nextBoolean()) {
          switchBody.add(new BreakStmt());
        }
      }
    }

    programState.exitSwitch();
    return new SwitchStmt(switchExpr, new BlockStmt(switchBody, true));
  }

  protected LoopStmt generateWhileLoop() {
    final Expr condExpr = generateBaseExpr(BasicType.BOOL);
    programState.enterLoop();
    final Stmt bodyStmt = new BlockStmt(generateScope(1, configuration.getMaxWhileScopeLength()),
        true);
    programState.exitLoop();
    return new WhileStmt(condExpr, bodyStmt);
  }

  protected LoopStmt generateDoWhileLoop() {
    programState.enterLoop();
    programState.enterFor();
    final Stmt bodyStmt = new BlockStmt(generateScope(1, configuration.getMaxWhileScopeLength()),
        true);
    programState.enterFor();
    programState.exitLoop();
    final Expr condExpr = generateBaseExpr(BasicType.BOOL);
    return new DoStmt(bodyStmt, condExpr);
  }



  protected LoopStmt generateForLoop(boolean enforceInduction) {
    Stmt initStmt = new NullStmt();
    Expr condExpr = null;
    Expr incrExpr = null;

    // Scopes correctly the loop indexes
    programState.addScope();
    programState.enterLoop();
    programState.enterFor();

    if (enforceInduction) {
      // Get a name and a random type
      final String inductionVarName = "for_" + programState.getAvailableNoShadowName();
      final BasicType inductionType = randomTypeGenerator.getRandomBaseType(true);
      programState.addVariable(inductionVarName, new UnifiedTypeProxy(inductionType), false, false);

      // Generate an offset, the number of executed step and choose an operation
      final Pair<Expr, Number> exprValuePair = generateScalarConstantExprValue(inductionType);
      final Number offset = exprValuePair.getRight();
      final Expr offsetExpr = exprValuePair.getLeft();

      final Number maxStep = randGen.nextInt(configuration.getMaxForLength());
      final BinOp inductionOp = randomTypeGenerator.getRandomForOp(inductionType);

      // Build the statements
      initStmt = new DeclarationStmt(new VariablesDeclaration(inductionType,
              new VariableDeclInfo(inductionVarName, null, new Initializer(offsetExpr))));

      // Computes the increment operand type
      Number step;
      if ((inductionOp.equals(BinOp.ADD) || inductionOp.equals(BinOp.SUB))
          && randGen.nextBoolean()) {

        // Generate an increment / decrement
        step = 1;
        if (inductionOp.equals(BinOp.ADD)) {
          incrExpr = new UnaryExpr(new VariableIdentifierExpr(inductionVarName),
              randGen.nextBoolean() ? UnOp.POST_INC : UnOp.PRE_INC);
        } else {
          incrExpr = new UnaryExpr(new VariableIdentifierExpr(inductionVarName),
              randGen.nextBoolean() ? UnOp.POST_DEC : UnOp.PRE_DEC);
        }
      } else {
        // Generate a value
        BinOp assignOp = BinOp.valueOf(inductionOp + "_ASSIGN");
        Pair<Expr, Number> incrementExprValuePair = generateScalarConstantExprValue(inductionType,
            configuration.getMaxForIncrement());
        incrExpr = new BinaryExpr(new VariableIdentifierExpr(inductionVarName),
            incrementExprValuePair.getLeft(), assignOp);
        step = incrementExprValuePair.getRight();
      }

      // Generate the correct comparison operand for the test
      BinOp comparisonOp;
      // Less than (we increment the value)
      if (inductionOp.equals(BinOp.ADD) || inductionOp.equals(BinOp.MUL)) {
        if (randGen.nextBoolean()) {
          comparisonOp = BinOp.LT;
        } else {
          comparisonOp = BinOp.LE;
        }

        // More than we decrement the value
      } else {
        if (randGen.nextBoolean()) {
          comparisonOp = BinOp.GT;
        } else {
          comparisonOp = BinOp.GE;
        }
      }

      // Generate the correct max value for the increment operand
      Number maxValue = 0;
      long bound = (maxStep.longValue() * step.longValue());
      switch (inductionOp) {
        case ADD:
          maxValue = offset.longValue() + bound;
          break;
        case SUB:
          maxValue = offset.longValue() - bound;
          break;
        case DIV:
          maxValue = offset.longValue() / ((bound == 0) ? 1 : bound);
          break;
        case MUL:
          maxValue = offset.longValue() * bound;
          break;
        default:
          assert false;
          break;
      }

      final Expr maxExpr = inductionType.equals(BasicType.FLOAT)
          ? new FloatConstantExpr(
              maxValue.longValue() % FuzzerConstants.MAX_PERMITTED_FLOAT + ".0f")
          : inductionType.equals(BasicType.INT)
          ? new IntConstantExpr(String.valueOf(
              maxValue.longValue() % FuzzerConstants.MAX_INT_VALUE))
          : new UIntConstantExpr(maxValue.longValue() % FuzzerConstants.MAX_UINT_VALUE + "u");
      // Generate the conditionExpr using the value of the increment
      condExpr = new BinaryExpr(new VariableIdentifierExpr(inductionVarName), maxExpr,
          comparisonOp);

    } else {
      // Randomly creates a declaration or an expression on an existing value or nothing
      if (randGen.nextBoolean()) {
        if (randGen.nextBoolean()) {
          initStmt = new DeclarationStmt(generateRandomTypedVarDecls(0, true));
        } else {
          initStmt = new ExprStmt(generateBaseExpr(randomTypeGenerator.getRandomBaseType()));
        }
      }
      // Randomly creates a condition or nothing
      if (randGen.nextBoolean()) {
        condExpr = generateBaseExpr(BasicType.BOOL);
      }

      // Randomly creates an increment or nothing
      if (randGen.nextBoolean()) {
        incrExpr = generateBaseExpr(randomTypeGenerator.getRandomBaseType());
      }
    }
    final ForStmt loopStmt = new ForStmt(initStmt, condExpr, incrExpr,
        new BlockStmt(generateScope(1,
        configuration.getMaxWhileScopeLength(), false), false));

    programState.exitFor();
    programState.exitLoop();
    programState.exitScope();
    return loopStmt;
  }


  protected ExprStmt generateVoidFunCall() {
    return new ExprStmt(generateFunCallExpr(new UnifiedTypeProxy(VoidType.VOID)));
  }

  protected List<Stmt> generateScope(int minScopeLength, int maxScopeLength) {
    return generateScope(minScopeLength, maxScopeLength, true);
  }


  protected List<Stmt> generateScope() {
    return generateScope(1, configuration.getMaxMainLength(), true);
  }

  //TODO ensure at this stage that some function exists that has a void return type and does not
  // necessit out params
  protected List<Stmt> generateScope(int minScopeLength, int maxScopeLength, boolean induceScope) {
    if (induceScope) {
      programState.addScope();
    }
    List<Stmt> stmts = new ArrayList<>();

    // Decide of the available actions in current context and skew the probability for assignments
    List<Integer> options = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 5, 5, 5));
    if (programState.getScopeDepth() < configuration.getMaxScopeDepth()) {
      options.addAll(Arrays.asList(2, 2, 3, 4, 4, 8, 9, 10, 11));
    }
    if (programState.getSwitchDepth() > 0 || programState.getLoopDepth() > 0) {
      options.add(6);
    }
    if (programState.getLoopDepth() > 0) {
      options.add(7);
    }

    // Choose the number of stmts in the current scope
    final int stmtNumber = randGen.nextInt(minScopeLength, maxScopeLength);
    for (int i = 0; i < stmtNumber; i++) {
      Stmt stmt;
      // Choose the action to perform
      int actionIndex = options.get(randGen.nextInt(options.size()));
      switch (actionIndex) {
        case 1:
          stmt = new ExprStmt(generateProgramAssignmentLine());
          break;
        case 2:
          stmt = generateIfStmt();
          break;
        case 3:
          stmt = generateSwitchStmt(false);
          break;
        case 4:
          stmt = generateWhileLoop();
          break;
        case 5:
          stmt = generateVoidFunCall();
          break;
        case 6:
          stmt = new BreakStmt();
          break;
        case 7:
          stmt = new ContinueStmt();
          break;
        case 8:
          stmt = generateSwitchStmt(true);
          break;
        case 9:
          stmt = generateForLoop(false);
          break;
        case 10:
          stmt = generateForLoop(true);
          break;
        case 11:
          stmt = generateDoWhileLoop();
          break;
        default:
          stmt = new DeclarationStmt(generateRandomTypedVarDecls(
              randGen.nextPositiveInt(configuration.getMaxVardeclElements()),
              true));
          break;
      }
      stmts.add(stmt);
    }
    if (induceScope) {
      programState.exitScope();
    }
    return stmts;
  }
}
