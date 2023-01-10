//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.graphicsfuzz.glslsmith.postprocessing;

import com.graphicsfuzz.common.ast.decl.Declaration;
import com.graphicsfuzz.common.ast.decl.FunctionDefinition;
import com.graphicsfuzz.common.ast.decl.FunctionPrototype;
import com.graphicsfuzz.common.ast.decl.Initializer;
import com.graphicsfuzz.common.ast.decl.ParameterDecl;
import com.graphicsfuzz.common.ast.decl.VariableDeclInfo;
import com.graphicsfuzz.common.ast.decl.VariablesDeclaration;
import com.graphicsfuzz.common.ast.expr.ArrayIndexExpr;
import com.graphicsfuzz.common.ast.expr.BinOp;
import com.graphicsfuzz.common.ast.expr.BinaryExpr;
import com.graphicsfuzz.common.ast.expr.Expr;
import com.graphicsfuzz.common.ast.expr.FloatConstantExpr;
import com.graphicsfuzz.common.ast.expr.FunctionCallExpr;
import com.graphicsfuzz.common.ast.expr.IntConstantExpr;
import com.graphicsfuzz.common.ast.expr.ParenExpr;
import com.graphicsfuzz.common.ast.expr.TernaryExpr;
import com.graphicsfuzz.common.ast.expr.TypeConstructorExpr;
import com.graphicsfuzz.common.ast.expr.UIntConstantExpr;
import com.graphicsfuzz.common.ast.expr.UnOp;
import com.graphicsfuzz.common.ast.expr.UnaryExpr;
import com.graphicsfuzz.common.ast.expr.VariableIdentifierExpr;
import com.graphicsfuzz.common.ast.stmt.BlockStmt;
import com.graphicsfuzz.common.ast.stmt.DeclarationStmt;
import com.graphicsfuzz.common.ast.stmt.ExprStmt;
import com.graphicsfuzz.common.ast.stmt.IfStmt;
import com.graphicsfuzz.common.ast.stmt.ReturnStmt;
import com.graphicsfuzz.common.ast.stmt.Stmt;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.QualifiedType;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.common.ast.type.TypeQualifier;
import com.graphicsfuzz.glslsmith.config.ConfigInterface.RunType;
import com.graphicsfuzz.glslsmith.config.FuzzerConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class WrapperGenerator {

  public static FunctionPrototype generateDeclaration(Wrapper wrapper, BasicType typeA,
                                                      BasicType typeB, RunType runType) {
    List<ParameterDecl> parameterDecls = new ArrayList<>();

    int i;
    for (i = 0; i < wrapper.nbA; ++i) {
      Type parArgA = typeA;
      if (wrapper.inoutA) {
        parArgA = new QualifiedType(typeA, Collections.singletonList(TypeQualifier.INOUT_PARAM));
      }

      parameterDecls.add(new ParameterDecl("p" + i, parArgA, null));
    }

    for (i = wrapper.nbA; i < wrapper.nbA + wrapper.nbB; ++i) {
      parameterDecls.add(new ParameterDecl("p" + i, typeB, null));
    }

    if (runType == RunType.ADDED_ID) {
      parameterDecls.add(new ParameterDecl("id", BasicType.INT, null));
    }

    BasicType returnType = typeA.isScalar() && typeB != null && typeB.isVector() ? typeB : typeA;
    return new FunctionPrototype(wrapper.name, returnType, parameterDecls);
  }

  public static Expr generateLeftExpr(Expr originalExpr, RunType runType) {
    return runType == RunType.ADDED_ID ? new ParenExpr(
        new BinaryExpr(new BinaryExpr(new ArrayIndexExpr(
            new VariableIdentifierExpr("ids"),
            new VariableIdentifierExpr("id")), new IntConstantExpr("0"), BinOp.ASSIGN),
            originalExpr, BinOp.COMMA)) : originalExpr;
  }

  public static Expr generateRightExpr(Expr originalExpr, RunType runType) {
    return runType == RunType.ADDED_ID ? new ParenExpr(
        new BinaryExpr(new BinaryExpr(new ArrayIndexExpr(
            new VariableIdentifierExpr("ids"),
            new VariableIdentifierExpr("id")), new IntConstantExpr("2"), BinOp.MUL_ASSIGN),
        originalExpr, BinOp.COMMA)) : originalExpr;
  }

  public static Expr generateSafeAbsCall(String variableName, RunType runType) {
    return runType == RunType.ADDED_ID ? new FunctionCallExpr("SAFE_ABS",
        new VariableIdentifierExpr(variableName), new VariableIdentifierExpr("id")) :
        new FunctionCallExpr("SAFE_ABS", new VariableIdentifierExpr(variableName));
  }

  public static List<ParameterDecl> generateCommonParamDecls(BasicType typeA, BasicType typeB,
                                                             boolean inoutA, RunType runType) {
    List<ParameterDecl> parameterDecls = new ArrayList<>();
    if (inoutA) {
      parameterDecls.add(new ParameterDecl("A", new QualifiedType(typeA,
          Collections.singletonList(TypeQualifier.INOUT_PARAM)), null));
    } else {
      parameterDecls.add(new ParameterDecl("A", typeA, null));
    }

    parameterDecls.add(new ParameterDecl("B", typeB, null));
    if (runType == RunType.ADDED_ID) {
      parameterDecls.add(new ParameterDecl("id", BasicType.INT, null));
    }

    return parameterDecls;
  }

  public static Expr generateConstant(BasicType type, String constant) {
    Expr constantExpr;
    if (type.getElementType().equals(BasicType.INT)) {
      constantExpr = new IntConstantExpr(constant);
    } else if (type.getElementType().equals(BasicType.FLOAT)) {
      constantExpr = new FloatConstantExpr(constant + "f");
    } else {
      constantExpr = new UIntConstantExpr(constant + "u");
    }

    return type.isScalar() ? constantExpr : new TypeConstructorExpr(type.getText(),
        constantExpr);
  }

  public static Expr generateVectorComparison(BasicType vectorType, String letter,
                                              String constant) {
    return generateVectorComparison(vectorType, letter, constant, "equal");
  }

  public static Expr generateVectorComparison(BasicType vectorType, String letter,
                                              String constant, String operation) {
    return new FunctionCallExpr("any", new FunctionCallExpr(operation,
        new VariableIdentifierExpr(letter), generateConstant(vectorType, constant)));
  }

  public static Declaration generateClampWrapper(BasicType valueType, BasicType extremumType,
                                                 RunType runType) {
    Expr comparisonExpr;
    if (extremumType.isVector()) {
      comparisonExpr = new FunctionCallExpr("any", new FunctionCallExpr("greaterThan",
          new VariableIdentifierExpr("minVal"), new VariableIdentifierExpr("maxVal")));
    } else {
      comparisonExpr = new BinaryExpr(new VariableIdentifierExpr("minVal"),
          new VariableIdentifierExpr("maxVal"), BinOp.GT);
    }

    Expr returnExpr = new TernaryExpr(comparisonExpr,
        generateLeftExpr(new FunctionCallExpr("clamp",
            new VariableIdentifierExpr("value"),
            new FunctionCallExpr("min",
                new VariableIdentifierExpr("minVal"),
                new VariableIdentifierExpr("maxVal")),
            new FunctionCallExpr("max",
                new VariableIdentifierExpr("minVal"),
                new VariableIdentifierExpr("maxVal"))), runType),
        generateRightExpr(
            new FunctionCallExpr("clamp",
                new VariableIdentifierExpr("value"), new VariableIdentifierExpr("minVal"),
                new VariableIdentifierExpr("maxVal")), runType));
    List<ParameterDecl> parameterDecls = new ArrayList<>(Arrays.asList(new ParameterDecl("value",
        valueType, null), new ParameterDecl("minVal", extremumType, null),
        new ParameterDecl("maxVal", extremumType, null)));
    if (runType == RunType.ADDED_ID) {
      parameterDecls.add(new ParameterDecl("id", BasicType.INT, null));
    }

    return new FunctionDefinition(new FunctionPrototype("SAFE_CLAMP", valueType, parameterDecls),
        new BlockStmt(Collections.singletonList(new ReturnStmt(returnExpr)), true));
  }

  private static Expr generateDivTestExpr(BasicType typeA, BasicType typeB) {
    if (typeA.getElementType() == BasicType.INT) {
      Expr atestExpr = typeA.isVector() ? generateVectorComparison(typeA, "A",
          String.valueOf(FuzzerConstants.MIN_INT_VALUE)) :
          new BinaryExpr(new VariableIdentifierExpr("A"),
          new IntConstantExpr(String.valueOf(FuzzerConstants.MIN_INT_VALUE)), BinOp.EQ);
      Expr btest0Expr = typeB.isVector() ? generateVectorComparison(typeB, "B", "0") :
          new BinaryExpr(new VariableIdentifierExpr("B"), new IntConstantExpr("0"), BinOp.EQ);
      Expr btest1Expr = typeB.isVector() ? generateVectorComparison(typeB, "B", "-1") :
          new BinaryExpr(new VariableIdentifierExpr("B"), new IntConstantExpr("-1"), BinOp.EQ);
      return new BinaryExpr(btest0Expr,
          new BinaryExpr(atestExpr, btest1Expr, BinOp.LAND),
          BinOp.LOR);
    } else {
      return typeB.isVector() ? generateVectorComparison(typeB, "B", "0") :
          new BinaryExpr(new VariableIdentifierExpr("B"), generateConstant(typeB, "0"), BinOp.EQ);
    }
  }

  public static Declaration generateDivWrapper(BasicType typeA, BasicType typeB, RunType runType) {
    BasicType functionReturnType = typeB.isVector() ? typeB : typeA;
    String intText = "2";
    Expr divExpr = new TernaryExpr(generateDivTestExpr(typeA, typeB),
        generateLeftExpr(new BinaryExpr(new VariableIdentifierExpr("A"), generateConstant(typeB,
            intText), BinOp.DIV), runType),
        generateRightExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
            new VariableIdentifierExpr("B"), BinOp.DIV), runType));
    return new FunctionDefinition(new FunctionPrototype("SAFE_DIV", functionReturnType,
        generateCommonParamDecls(typeA, typeB, false, runType)),
        new BlockStmt(Collections.singletonList(new ReturnStmt(divExpr)), true));
  }

  public static Declaration generateDivAssignWrapper(BasicType typeA, BasicType typeB,
                                                     RunType runType) {
    String intText = "2";
    Expr divAssignExpr = new TernaryExpr(generateDivTestExpr(typeA, typeB),
        generateLeftExpr(new ParenExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
            generateConstant(typeB, intText), BinOp.DIV_ASSIGN)), runType),
        generateRightExpr(new ParenExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
            new VariableIdentifierExpr("B"), BinOp.DIV_ASSIGN)), runType));
    return new FunctionDefinition(new FunctionPrototype("SAFE_DIV_ASSIGN", typeA,
        generateCommonParamDecls(typeA, typeB, true, runType)),
        new BlockStmt(Collections.singletonList(new ReturnStmt(divAssignExpr)), true));
  }

  private static Expr generateShiftTestExpr(BasicType typeB) {
    Expr noMoreThan32 = typeB.isVector() ? generateVectorComparison(typeB, "B", "32",
        "greaterThan") : new BinaryExpr(new VariableIdentifierExpr("B"), generateConstant(typeB,
        "32"), BinOp.GE);
    return typeB.getElementType() == BasicType.INT ? new BinaryExpr(noMoreThan32,
        typeB.isVector() ? generateVectorComparison(typeB, "B", "0", "lessThan") :
            new BinaryExpr(new VariableIdentifierExpr("B"), generateConstant(typeB, "0"),
                BinOp.LT), BinOp.LOR) : noMoreThan32;
  }

  private static Declaration generateShiftWrapper(BasicType typeA, BasicType typeB, BinOp op,
                                                  String funName, RunType runType) {
    Expr shiftExpr = new TernaryExpr(generateShiftTestExpr(typeB),
        generateLeftExpr(new BinaryExpr(new VariableIdentifierExpr("A"), generateConstant(typeB,
            "16"), op), runType),
        generateRightExpr(new BinaryExpr(
            new VariableIdentifierExpr("A"),
            new VariableIdentifierExpr("B"), op), runType));
    return new FunctionDefinition(new FunctionPrototype(funName, typeA,
        generateCommonParamDecls(typeA, typeB, false, runType)),
        new BlockStmt(Collections.singletonList(new ReturnStmt(shiftExpr)), true));
  }

  private static Declaration generateShiftAssignWrapper(BasicType typeA, BasicType typeB,
                                                        BinOp op, String funName, RunType runType) {
    Expr shiftAssignExpr = new TernaryExpr(generateShiftTestExpr(typeB),
        generateLeftExpr(new ParenExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
            generateConstant(typeB, "16"), op)), runType),
        generateRightExpr(new ParenExpr(new BinaryExpr(
            new VariableIdentifierExpr("A"), new VariableIdentifierExpr("B"), op)), runType));
    return new FunctionDefinition(new FunctionPrototype(funName, typeA,
        generateCommonParamDecls(typeA, typeB, true, runType)),
        new BlockStmt(Collections.singletonList(new ReturnStmt(shiftAssignExpr)), true));
  }

  public static Declaration generateLShiftWrapper(BasicType typeA, BasicType typeB,
                                                  RunType runType) {
    return generateShiftWrapper(typeA, typeB, BinOp.SHL, "SAFE_LSHIFT", runType);
  }

  public static Declaration generateLShiftAssignWrapper(BasicType typeA, BasicType typeB,
                                                        RunType runType) {
    return generateShiftAssignWrapper(typeA, typeB, BinOp.SHL_ASSIGN, "SAFE_LSHIFT_ASSIGN",
        runType);
  }

  public static Declaration generateRShiftWrapper(BasicType typeA, BasicType typeB,
                                                  RunType runType) {
    return generateShiftWrapper(typeA, typeB, BinOp.SHR, "SAFE_RSHIFT", runType);
  }

  public static Declaration generateRShiftAssignWrapper(BasicType typeA, BasicType typeB,
                                                        RunType runType) {
    return generateShiftAssignWrapper(typeA, typeB, BinOp.SHR_ASSIGN, "SAFE_RSHIFT_ASSIGN",
        runType);
  }

  private static Expr generateModTestExpr(BasicType typeB) {
    return typeB.isVector() ? generateVectorComparison(typeB, "B", "0") :
        new BinaryExpr(new VariableIdentifierExpr("B"), generateConstant(typeB, "0"), BinOp.EQ);
  }

  private static List<Stmt> generateIdInternalStmts(BasicType typeA, BasicType typeB,
                                                 RunType runType) {

    // Test on A and B depends on their effective type (vectors or not)
    Expr paramAComp = typeA.isVector() ? generateVectorComparison(typeA, "A", "0",
        "greaterThanEqual") :
        new BinaryExpr(new VariableIdentifierExpr("A"), new IntConstantExpr("0"), BinOp.GE);
    Expr paramBComp = typeB.isVector() ? generateVectorComparison(typeB, "B", "0",
        "greaterThan") :
        new BinaryExpr(new VariableIdentifierExpr("B"), new IntConstantExpr("0"), BinOp.GT);
    return Arrays.asList(
        //"typeA" tmpA = SAFE_ABS(A, id);
        new DeclarationStmt(new VariablesDeclaration(typeA,
            Collections.singletonList(new VariableDeclInfo("tmpA", null,
                new Initializer(generateSafeAbsCall("A", runType)))))),
        // "typeB" tmpB = SAFE_ABS(B, id);
        new DeclarationStmt(new VariablesDeclaration(typeB,
        Collections.singletonList(new VariableDeclInfo("tmpB", null,
            new Initializer(generateSafeAbsCall("B", runType)))))),
        // ids[id] = A >=0 && B > 0 ? 1 : 0;
        new ExprStmt(new BinaryExpr(
            new ArrayIndexExpr(
                new VariableIdentifierExpr("ids"), new VariableIdentifierExpr("id")),
            new TernaryExpr(new BinaryExpr(paramAComp, paramBComp, BinOp.LAND),
                new IntConstantExpr("2"), new IntConstantExpr("0")),
            BinOp.MUL_ASSIGN)));
  }

  public static Declaration generateModWrapper(BasicType typeA, BasicType typeB, RunType runType) {
    final BasicType functionReturnType = typeB.isVector() ? typeB : typeA;
    TernaryExpr modExpr;
    if (typeA.getElementType() == BasicType.INT) {
      if (runType == RunType.ADDED_ID) {
        modExpr = new TernaryExpr(generateModTestExpr(typeB),
            new BinaryExpr(new VariableIdentifierExpr("tmpA"),
                generateConstant(typeB, String.valueOf(FuzzerConstants.MAX_INT_VALUE - 1)),
                BinOp.MOD),
            new BinaryExpr(new VariableIdentifierExpr("tmpA"),
                new VariableIdentifierExpr("tmpB"), BinOp.MOD));
      } else {
        modExpr = new TernaryExpr(generateModTestExpr(typeB),
            generateLeftExpr(new BinaryExpr(generateSafeAbsCall("A", runType),
                generateConstant(typeB, String.valueOf(FuzzerConstants.MAX_INT_VALUE - 1)),
                BinOp.MOD), runType),
            generateRightExpr(new BinaryExpr(generateSafeAbsCall("A", runType),
                generateSafeAbsCall("B", runType), BinOp.MOD), runType));
      }
    } else {
      modExpr = new TernaryExpr(generateModTestExpr(typeB),
          generateLeftExpr(new BinaryExpr(new VariableIdentifierExpr("A"), generateConstant(typeB,
              String.valueOf(FuzzerConstants.MAX_INT_VALUE - 1)), BinOp.MOD), runType),
          generateRightExpr(new BinaryExpr(
              new VariableIdentifierExpr("A"), new VariableIdentifierExpr("B"),
              BinOp.MOD), runType));
    }
    List<Stmt> stmts = new ArrayList<>();
    if (runType == RunType.ADDED_ID && typeA.getElementType() == BasicType.INT) {
      stmts.addAll(generateIdInternalStmts(typeA, typeB, runType));
    }
    stmts.add(new ReturnStmt(modExpr));

    return new FunctionDefinition(new FunctionPrototype("SAFE_MOD", functionReturnType,
        generateCommonParamDecls(typeA, typeB, false, runType)),
        new BlockStmt(stmts, true));
  }

  public static Declaration generateModAssignWrapper(BasicType typeA, BasicType typeB,
                                                     RunType runType) {
    List<Stmt> stmts;
    if (typeA.getElementType() == BasicType.INT) {
      stmts = new ArrayList<>();
      if (runType == RunType.ADDED_ID) {
        stmts.addAll(generateIdInternalStmts(typeA, typeB, runType));
      } else {
        stmts.add(new DeclarationStmt(
            new VariablesDeclaration(typeB, new VariableDeclInfo("tmpB", null,
                new Initializer(generateSafeAbsCall("B", runType))))));
      }
      stmts.add(new ExprStmt(new BinaryExpr(new VariableIdentifierExpr("A"),
          generateSafeAbsCall("A", runType), BinOp.ASSIGN)));
      stmts.add(new ReturnStmt(new TernaryExpr(generateModTestExpr(typeB),
          generateLeftExpr(new ParenExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
              generateConstant(typeB, String.valueOf(FuzzerConstants.MAX_INT_VALUE - 1)),
              BinOp.MOD_ASSIGN)), runType),
          generateRightExpr(new ParenExpr(new BinaryExpr(
              new VariableIdentifierExpr("A"),
              new VariableIdentifierExpr("tmpB"),
              BinOp.MOD_ASSIGN)), runType))));
    } else {
      stmts = Collections.singletonList(new ReturnStmt(new TernaryExpr(generateModTestExpr(typeB),
            generateLeftExpr(new ParenExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
            generateConstant(typeB, String.valueOf(FuzzerConstants.MAX_INT_VALUE - 1)),
                BinOp.MOD_ASSIGN)), runType),
            generateRightExpr(new ParenExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
                new VariableIdentifierExpr("B"), BinOp.MOD_ASSIGN)), runType))));
    }

    return new FunctionDefinition(new FunctionPrototype("SAFE_MOD_ASSIGN", typeA,
        generateCommonParamDecls(typeA, typeB, true, runType)), new BlockStmt(stmts, true));
  }

  public static Declaration generateAbsWrapper(BasicType type, BasicType useless, RunType runType) {
    List<Stmt> stmts = new ArrayList<>();

    assert type.getElementType() == BasicType.INT;

    //TODO refactor the two left right expressions
    //TODO can be reformated as a ternary operator once NVIDIA lexer is fixed
    if (runType == RunType.ADDED_ID) {
      if (type.isScalar()) {
        stmts.add(new IfStmt(new BinaryExpr(new VariableIdentifierExpr("A"),
            new IntConstantExpr("0"), BinOp.GE),
            new ExprStmt(new BinaryExpr(new ArrayIndexExpr(new VariableIdentifierExpr("ids"),
                new VariableIdentifierExpr("id")), new IntConstantExpr("2"), BinOp.MUL_ASSIGN)),
            new ExprStmt(new BinaryExpr(new ArrayIndexExpr(new VariableIdentifierExpr("ids"),
                new VariableIdentifierExpr("id")), new IntConstantExpr("0"), BinOp.ASSIGN))));
      } else {
        stmts.add(new IfStmt(new FunctionCallExpr("any", new FunctionCallExpr(
            "lessThan", new VariableIdentifierExpr("A"), new TypeConstructorExpr(
                type.getWithoutQualifiers().getText(), new IntConstantExpr("0")))),
            new ExprStmt(new BinaryExpr(new ArrayIndexExpr(new VariableIdentifierExpr("ids"),
                new VariableIdentifierExpr("id")), new IntConstantExpr("2"), BinOp.MUL_ASSIGN)),
            new ExprStmt(new BinaryExpr(new ArrayIndexExpr(new VariableIdentifierExpr("ids"),
                new VariableIdentifierExpr("id")), new IntConstantExpr("0"), BinOp.ASSIGN))));
      }
    }

    if (type.isScalar()) {
      stmts.add(new ReturnStmt(new TernaryExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
          new IntConstantExpr(String.valueOf(FuzzerConstants.MIN_INT_VALUE)), BinOp.EQ),
          new IntConstantExpr(String.valueOf(FuzzerConstants.MAX_INT_VALUE)),
          new FunctionCallExpr("abs",
              new VariableIdentifierExpr("A")))));
    } else {
      for (int i = 0; i < type.getNumElements(); ++i) {
        stmts.add(new ExprStmt(new BinaryExpr(new ArrayIndexExpr(new VariableIdentifierExpr("A"),
            new IntConstantExpr(String.valueOf(i))),
            new TernaryExpr(new BinaryExpr(new ArrayIndexExpr(new VariableIdentifierExpr("A"),
                new IntConstantExpr(String.valueOf(i))),
                new IntConstantExpr(String.valueOf(FuzzerConstants.MIN_INT_VALUE)), BinOp.EQ),
                new IntConstantExpr(String.valueOf(FuzzerConstants.MAX_INT_VALUE)),
                new FunctionCallExpr("abs",
                    new ArrayIndexExpr(new VariableIdentifierExpr("A"),
                        new IntConstantExpr(String.valueOf(i))))), BinOp.ASSIGN)));
      }

      stmts.add(new ReturnStmt(new VariableIdentifierExpr("A")));
    }

    List<ParameterDecl> parameterDecls = new ArrayList<>();
    parameterDecls.add(new ParameterDecl("A", type, null));
    if (runType == RunType.ADDED_ID) {
      parameterDecls.add(new ParameterDecl("id", BasicType.INT, null));
    }

    return new FunctionDefinition(new FunctionPrototype("SAFE_ABS", type, parameterDecls),
        new BlockStmt(stmts, true));
  }

  private static List<Stmt> generateStdBitWrapperBase(RunType runType) {
    List<Stmt> declarationList = new ArrayList<>();
    List<Expr> offsetAbsParams = runType == RunType.ADDED_ID ? Arrays.asList(
        new VariableIdentifierExpr("offset"), new VariableIdentifierExpr("id")) :
        Collections.singletonList(new VariableIdentifierExpr("offset"));
    VariablesDeclaration offsetDeclaration = new VariablesDeclaration(BasicType.INT,
        Collections.singletonList(new VariableDeclInfo("safe_offset", null,
            new Initializer(new BinaryExpr(new FunctionCallExpr("SAFE_ABS", offsetAbsParams),
                new IntConstantExpr("32"), BinOp.MOD)))));
    declarationList.add(new DeclarationStmt(offsetDeclaration));
    List<Expr> bitsAbsParams = runType == RunType.ADDED_ID ? Arrays.asList(
        new VariableIdentifierExpr("bits"), new VariableIdentifierExpr("id")) :
        Collections.singletonList(new VariableIdentifierExpr("bits"));
    VariablesDeclaration bitsDeclaration = new VariablesDeclaration(BasicType.INT,
        Collections.singletonList(new VariableDeclInfo("safe_bits", null,
            new Initializer(new BinaryExpr(new FunctionCallExpr("SAFE_ABS", bitsAbsParams),
                new ParenExpr(new BinaryExpr(new IntConstantExpr("32"),
                    new VariableIdentifierExpr("safe_offset"), BinOp.SUB)), BinOp.MOD)))));
    declarationList.add(new DeclarationStmt(bitsDeclaration));
    if (runType == RunType.ADDED_ID) {
      declarationList.add(new ExprStmt(new BinaryExpr(
          new ArrayIndexExpr(
              new VariableIdentifierExpr("ids"), new VariableIdentifierExpr("id")),
          new IntConstantExpr("0"), BinOp.ASSIGN)));
    }
    return declarationList;
  }

  public static Declaration generateBitInsertWrapper(BasicType valueType, BasicType useless,
                                                     RunType runType) {
    List<Stmt> body = generateStdBitWrapperBase(runType);
    Expr funCallExpr = new FunctionCallExpr("bitfieldInsert",
        new VariableIdentifierExpr("base"), new VariableIdentifierExpr("insert"),
        new VariableIdentifierExpr("safe_offset"), new VariableIdentifierExpr("safe_bits"));
    body.add(new ReturnStmt(funCallExpr));
    List<ParameterDecl> parameterDecls = new ArrayList<>(Arrays.asList(new ParameterDecl("base",
        valueType, null), new ParameterDecl("insert", valueType, null),
        new ParameterDecl("offset", BasicType.INT, null), new ParameterDecl("bits",
            BasicType.INT, null)));
    if (runType == RunType.ADDED_ID) {
      parameterDecls.add(new ParameterDecl("id", BasicType.INT, null));
    }

    return new FunctionDefinition(new FunctionPrototype("SAFE_BITFIELD_INSERT", valueType,
        parameterDecls), new BlockStmt(body, true));
  }

  public static Declaration generateBitExtractWrapper(BasicType valueType, BasicType useless,
                                                      RunType runType) {
    List<Stmt> body = generateStdBitWrapperBase(runType);
    Expr funCallExpr = new FunctionCallExpr("bitfieldExtract",
        new VariableIdentifierExpr("value"), new VariableIdentifierExpr("safe_offset"),
        new VariableIdentifierExpr("safe_bits"));
    body.add(new ReturnStmt(funCallExpr));
    List<ParameterDecl> parameterDecls = new ArrayList<>(Arrays.asList(new ParameterDecl("value",
        valueType, null), new ParameterDecl("offset", BasicType.INT,
        null), new ParameterDecl("bits", BasicType.INT, null)));
    if (runType == RunType.ADDED_ID) {
      parameterDecls.add(new ParameterDecl("id", BasicType.INT, null));
    }

    return new FunctionDefinition(new FunctionPrototype("SAFE_BITFIELD_EXTRACT", valueType,
        parameterDecls), new BlockStmt(body, true));
  }

  private static Expr generateFloatTestExpr(Expr innerTestExpr, BasicType resultType) {
    return resultType.equals(BasicType.FLOAT) ? new BinaryExpr(new FunctionCallExpr("abs",
        innerTestExpr.clone()), new FloatConstantExpr("16777216.0"), BinOp.GE) :
        new FunctionCallExpr("any", new FunctionCallExpr("greaterThanEqual",
            new FunctionCallExpr("abs", innerTestExpr.clone()),
            generateConstant(resultType, "16777216.0")));
  }

  public static Declaration generateAddAssignWrapper(BasicType leftType, BasicType rightType,
                                                     RunType runType) {
    Expr addAssignExpr =
        new TernaryExpr(generateFloatTestExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
            new VariableIdentifierExpr("B"), BinOp.ADD), leftType),
            generateLeftExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
                generateConstant(leftType, "8.0"), BinOp.ASSIGN), runType),
            generateRightExpr(new ParenExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
                new VariableIdentifierExpr("B"), BinOp.ADD_ASSIGN)), runType));
    return new FunctionDefinition(new FunctionPrototype("SAFE_ADD_ASSIGN", leftType,
        generateCommonParamDecls(leftType, rightType, true, runType)),
        new BlockStmt(Collections.singletonList(new ReturnStmt(addAssignExpr)), true));
  }

  public static Declaration generateSubAssignWrapper(BasicType leftType, BasicType rightType,
                                                     RunType runType) {
    Expr subAssignExpr =
        new TernaryExpr(generateFloatTestExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
            new VariableIdentifierExpr("B"), BinOp.SUB), leftType),
            generateLeftExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
                generateConstant(leftType, "5.0"), BinOp.ASSIGN), runType),
            generateRightExpr(new ParenExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
                new VariableIdentifierExpr("B"), BinOp.SUB_ASSIGN)), runType));
    return new FunctionDefinition(new FunctionPrototype("SAFE_SUB_ASSIGN", leftType,
        generateCommonParamDecls(leftType, rightType, true, runType)),
        new BlockStmt(Collections.singletonList(new ReturnStmt(subAssignExpr)), true));
  }

  public static Declaration generateMulAssignWrapper(BasicType leftType, BasicType rightType,
                                                     RunType runType) {
    Expr mulAssignExpr =
        new TernaryExpr(generateFloatTestExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
            new VariableIdentifierExpr("B"), BinOp.MUL), leftType),
            generateLeftExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
                generateConstant(leftType, "12.0"), BinOp.ASSIGN), runType),
            generateRightExpr(new ParenExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
                new VariableIdentifierExpr("B"), BinOp.MUL_ASSIGN)), runType));
    return new FunctionDefinition(new FunctionPrototype("SAFE_MUL_ASSIGN", leftType,
        generateCommonParamDecls(leftType, rightType, true, runType)),
        new BlockStmt(Collections.singletonList(new ReturnStmt(mulAssignExpr)), true));
  }

  private static Declaration generateCommonUnaryWrapper(String functionName,
                                                        BasicType operandType, Expr testOperand,
                                                        Expr defaultOperand, Expr correctReturn,
                                                        RunType runType) {
    Expr unaryExpr = new TernaryExpr(generateFloatTestExpr(testOperand, operandType),
        generateLeftExpr(new BinaryExpr(new VariableIdentifierExpr("A"), defaultOperand,
            BinOp.ASSIGN), runType), generateRightExpr(correctReturn, runType));
    List<ParameterDecl> parameterDecls = new ArrayList<>();
    parameterDecls.add(new ParameterDecl("A", new QualifiedType(operandType,
        Collections.singletonList(TypeQualifier.INOUT_PARAM)), null));
    if (runType == RunType.ADDED_ID) {
      parameterDecls.add(new ParameterDecl("id", BasicType.INT, null));
    }

    return new FunctionDefinition(new FunctionPrototype(functionName, operandType,
        parameterDecls), new BlockStmt(Collections.singletonList(new ReturnStmt(unaryExpr)), true));
  }

  public static Declaration generatePreDecWrapper(BasicType operandType, BasicType useless,
                                                  RunType runType) {
    return generateCommonUnaryWrapper("SAFE_PRE_DEC", operandType,
        new BinaryExpr(new VariableIdentifierExpr("A"), new FloatConstantExpr("1.0f"), BinOp.SUB),
        generateConstant(operandType, "3.0"), new UnaryExpr(new VariableIdentifierExpr("A"),
            UnOp.PRE_DEC), runType);
  }

  public static Declaration generatePreIncWrapper(BasicType operandType, BasicType useless,
                                                  RunType runType) {
    return generateCommonUnaryWrapper("SAFE_PRE_INC", operandType,
        new BinaryExpr(new VariableIdentifierExpr("A"), new FloatConstantExpr("1.0f"), BinOp.ADD),
        generateConstant(operandType, "7.0"), new UnaryExpr(new VariableIdentifierExpr("A"),
            UnOp.PRE_INC), runType);
  }

  public static Declaration generatePostDecWrapper(BasicType operandType, BasicType useless,
                                                   RunType runType) {
    return generateCommonUnaryWrapper("SAFE_POST_DEC", operandType,
        new BinaryExpr(new VariableIdentifierExpr("A"), new FloatConstantExpr("1.0f"), BinOp.SUB),
        generateConstant(operandType, "2.0"), new UnaryExpr(new VariableIdentifierExpr("A"),
            UnOp.POST_DEC), runType);
  }

  public static Declaration generatePostIncWrapper(BasicType operandType, BasicType useless,
                                                   RunType runType) {
    return generateCommonUnaryWrapper("SAFE_POST_INC", operandType,
        new BinaryExpr(new VariableIdentifierExpr("A"), new FloatConstantExpr("1.0f"), BinOp.ADD),
        generateConstant(operandType, "1.0"), new UnaryExpr(new VariableIdentifierExpr("A"),
            UnOp.POST_INC), runType);
  }

  public static Declaration generateFloatResultWrapper(BasicType basicType, BasicType useless,
                                                       RunType runType) {
    Expr assignExpr = new TernaryExpr(generateFloatTestExpr(new VariableIdentifierExpr("A"),
        basicType), generateLeftExpr(generateConstant(basicType, "10.0"), runType),
        generateRightExpr(new VariableIdentifierExpr("A"), runType));
    List<ParameterDecl> parameterDecls = new ArrayList<>();
    parameterDecls.add(new ParameterDecl("A", basicType, null));
    if (runType == RunType.ADDED_ID) {
      parameterDecls.add(new ParameterDecl("id", BasicType.INT, null));
    }

    return new FunctionDefinition(new FunctionPrototype("SAFE_FLOAT_RESULT", basicType,
        parameterDecls),
        new BlockStmt(Collections.singletonList(new ReturnStmt(assignExpr)), true));
  }
}
