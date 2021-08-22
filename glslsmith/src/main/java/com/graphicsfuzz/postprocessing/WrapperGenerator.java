package com.graphicsfuzz.postprocessing;

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
import com.graphicsfuzz.common.ast.stmt.ReturnStmt;
import com.graphicsfuzz.common.ast.stmt.Stmt;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.QualifiedType;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.common.ast.type.TypeQualifier;
import com.graphicsfuzz.config.FuzzerConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public abstract class WrapperGenerator {

  public static FunctionPrototype generateDeclaration(Wrapper wrapper, BasicType typeA,
                                                      BasicType typeB) {
    final List<ParameterDecl> parameterDecls = new ArrayList<>();
    for (int i = 0; i < wrapper.nbA; i++) {
      Type parArgA = typeA;
      if (wrapper.inoutA) {
        parArgA = new QualifiedType(typeA, Collections.singletonList(TypeQualifier.INOUT_PARAM));
      }
      parameterDecls.add(new ParameterDecl("p" + i, parArgA, null));
    }

    for (int i = wrapper.nbA; i < wrapper.nbA + wrapper.nbB; i++) {
      parameterDecls.add(new ParameterDecl("p" + i, typeB, null));
    }
    BasicType returnType = typeA.isScalar() && typeB != null && typeB.isVector() ? typeB : typeA;
    return new FunctionPrototype(wrapper.name, returnType, parameterDecls);
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
    return type.isScalar() ? constantExpr : new TypeConstructorExpr(type.getText(), constantExpr);
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

  public static Declaration generateClampWrapper(BasicType valueType, BasicType extremumType) {
    Expr comparisonExpr;
    if (extremumType.isVector()) {
      comparisonExpr = new FunctionCallExpr("any", new FunctionCallExpr("greaterThan",
          new VariableIdentifierExpr("minVal"), new VariableIdentifierExpr("maxVal")));
    } else {
      comparisonExpr = new BinaryExpr(
          new VariableIdentifierExpr("minVal"), new VariableIdentifierExpr("maxVal"), BinOp.GT);
    }
    Expr returnExpr = new TernaryExpr(
        comparisonExpr,
        new FunctionCallExpr("clamp", new VariableIdentifierExpr("value"),
            new FunctionCallExpr("min", new VariableIdentifierExpr("minVal"),
                new VariableIdentifierExpr("maxVal")),
            new FunctionCallExpr("max", new VariableIdentifierExpr("minVal"),
                new VariableIdentifierExpr("maxVal"))),
        new FunctionCallExpr("clamp", new VariableIdentifierExpr("value"),
            new VariableIdentifierExpr("minVal"), new VariableIdentifierExpr("maxVal")));

    return new FunctionDefinition(new FunctionPrototype("SAFE_CLAMP", valueType,
        Arrays.asList(
          new ParameterDecl("value", valueType, null),
          new ParameterDecl("minVal", extremumType, null),
          new ParameterDecl("maxVal", extremumType, null))),
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
      return new BinaryExpr(btest0Expr, new BinaryExpr(atestExpr, btest1Expr,
          BinOp.LAND), BinOp.LOR);
    }
    return typeB.isVector() ? generateVectorComparison(typeB, "B", "0") :
        new BinaryExpr(new VariableIdentifierExpr("B"), generateConstant(typeB, "0"), BinOp.EQ);
  }


  //Arithmetic Wrapper Function declaration generator
  public static Declaration generateDivWrapper(BasicType typeA, BasicType typeB) {
    BasicType functionReturnType = typeB.isVector() ? typeB : typeA;
    String intText = "2";
    Expr divExpr = new TernaryExpr(
        generateDivTestExpr(typeA, typeB),
        new BinaryExpr(new VariableIdentifierExpr("A"), generateConstant(typeB, intText),
            BinOp.DIV),
        new BinaryExpr(new VariableIdentifierExpr("A"), new VariableIdentifierExpr("B"),
            BinOp.DIV));
    return new FunctionDefinition(new FunctionPrototype("SAFE_DIV", functionReturnType,
        Arrays.asList(
            new ParameterDecl("A", typeA, null),
            new ParameterDecl("B", typeB, null))),
        new BlockStmt(Collections.singletonList(new ReturnStmt(divExpr)), true));
  }

  public static Declaration generateDivAssignWrapper(BasicType typeA, BasicType typeB) {
    String intText = "2";
    Expr divAssignExpr = new TernaryExpr(
        generateDivTestExpr(typeA, typeB),
        new ParenExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
            generateConstant(typeB, intText), BinOp.DIV_ASSIGN)),
        new ParenExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
            new VariableIdentifierExpr("B"), BinOp.DIV_ASSIGN)));
    return new FunctionDefinition(new FunctionPrototype("SAFE_DIV_ASSIGN", typeA, Arrays.asList(
        new ParameterDecl("A", new QualifiedType(
            typeA, Collections.singletonList(TypeQualifier.INOUT_PARAM)), null),
        new ParameterDecl("B", typeB, null))),
        new BlockStmt(Collections.singletonList(new ReturnStmt(divAssignExpr)), true));
  }

  private static Expr generateShiftTestExpr(BasicType typeB) {
    Expr noMoreThan32 = typeB.isVector() ? generateVectorComparison(typeB, "B", "32",
        "greaterThan") :
        new BinaryExpr(new VariableIdentifierExpr("B"), generateConstant(typeB, "32"),
            BinOp.GE);
    if (typeB.getElementType() == BasicType.INT) {
      return new BinaryExpr(
          noMoreThan32,
          typeB.isVector() ? generateVectorComparison(typeB, "B", "0", "lessThan") :
              new BinaryExpr(new VariableIdentifierExpr("B"), generateConstant(typeB, "0"),
                  BinOp.LT),
          BinOp.LOR);
    }
    return noMoreThan32;
  }

  private static Declaration generateShiftWrapper(BasicType typeA, BasicType typeB, BinOp op,
                                                  String funName) {
    Expr shiftExpr = new TernaryExpr(
        generateShiftTestExpr(typeB),
        new BinaryExpr(new VariableIdentifierExpr("A"), generateConstant(typeB, "16"), op),
        new BinaryExpr(new VariableIdentifierExpr("A"), new VariableIdentifierExpr("B"),
            op));
    return new FunctionDefinition(new FunctionPrototype(funName,
        typeA, Arrays.asList(new ParameterDecl("A", typeA, null),
          new ParameterDecl("B", typeB, null))),
        new BlockStmt(Collections.singletonList(new ReturnStmt(shiftExpr)), true));
  }

  private static Declaration generateShiftAssignWrapper(BasicType typeA, BasicType typeB, BinOp op,
                                                        String funName) {
    Expr shiftAssignExpr = new TernaryExpr(
        generateShiftTestExpr(typeB),
        new ParenExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
            generateConstant(typeB, "16"), op)),
        new ParenExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
            new VariableIdentifierExpr("B"), op)));
    return new FunctionDefinition(new FunctionPrototype(funName,
        typeA, Arrays.asList(new ParameterDecl("A", new QualifiedType(typeA,
            Collections.singletonList(TypeQualifier.INOUT_PARAM)), null),
          new ParameterDecl("B", typeB, null))),
        new BlockStmt(Collections.singletonList(new ReturnStmt(shiftAssignExpr)), true));
  }

  public static Declaration generateLShiftWrapper(BasicType typeA, BasicType typeB) {
    return generateShiftWrapper(typeA, typeB, BinOp.SHL, "SAFE_LSHIFT");
  }

  public static Declaration generateLShiftAssignWrapper(BasicType typeA, BasicType typeB) {
    return generateShiftAssignWrapper(typeA, typeB, BinOp.SHL_ASSIGN, "SAFE_LSHIFT_ASSIGN");
  }

  public static Declaration generateRShiftWrapper(BasicType typeA, BasicType typeB) {
    return generateShiftWrapper(typeA, typeB, BinOp.SHR, "SAFE_RSHIFT");
  }

  public static Declaration generateRShiftAssignWrapper(BasicType typeA, BasicType typeB) {
    return generateShiftAssignWrapper(typeA, typeB, BinOp.SHR_ASSIGN, "SAFE_RSHIFT_ASSIGN");
  }

  private static Expr generateModTestExpr(BasicType typeB) {
    return typeB.isVector() ? generateVectorComparison(typeB, "B", "0") :
        new BinaryExpr(new VariableIdentifierExpr("B"),
            generateConstant(typeB, "0"), BinOp.EQ);
  }

  public static Declaration generateModWrapper(BasicType typeA, BasicType typeB) {
    Expr modExpr;
    BasicType functionReturnType = typeB.isVector() ? typeB : typeA;
    if (typeA.getElementType() == BasicType.INT) {
      modExpr = new TernaryExpr(generateModTestExpr(typeB),
          new BinaryExpr(new FunctionCallExpr("SAFE_ABS", new VariableIdentifierExpr("A")),
              generateConstant(typeB, String.valueOf(FuzzerConstants.MAX_INT_VALUE - 1)),
              BinOp.MOD),
          new BinaryExpr(new FunctionCallExpr("SAFE_ABS", new VariableIdentifierExpr("A")),
              new FunctionCallExpr("SAFE_ABS", new VariableIdentifierExpr("B")), BinOp.MOD));
    } else {
      modExpr = new TernaryExpr(generateModTestExpr(typeB),
          new BinaryExpr(new VariableIdentifierExpr("A"), generateConstant(typeB,
              String.valueOf(FuzzerConstants.MAX_INT_VALUE - 1)), BinOp.MOD),
          new BinaryExpr(new VariableIdentifierExpr("A"), new VariableIdentifierExpr("B"),
              BinOp.MOD));
    }
    return new FunctionDefinition(new FunctionPrototype("SAFE_MOD", functionReturnType,
        Arrays.asList(new ParameterDecl("A", typeA, null), new ParameterDecl("B", typeB, null))),
        new BlockStmt(Collections.singletonList(new ReturnStmt(modExpr)), true));
  }

  public static Declaration generateModAssignWrapper(BasicType typeA, BasicType typeB) {
    List<Stmt> stmts;
    if (typeA.getElementType() == BasicType.INT) {
      stmts = Arrays.asList(
          new ExprStmt(new BinaryExpr(new VariableIdentifierExpr("A"),
              new FunctionCallExpr("SAFE_ABS", new VariableIdentifierExpr("A")), BinOp.ASSIGN)),
          new ReturnStmt(new TernaryExpr(generateModTestExpr(typeB),
              new ParenExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
                  generateConstant(typeB, String.valueOf(FuzzerConstants.MAX_INT_VALUE - 1)),
                  BinOp.MOD_ASSIGN)),
              new ParenExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
                  new FunctionCallExpr("SAFE_ABS", new VariableIdentifierExpr("B")),
                  BinOp.MOD_ASSIGN)))));
    } else {
      stmts = Collections.singletonList(new ReturnStmt(new TernaryExpr(
          generateModTestExpr(typeB),
          new ParenExpr(new BinaryExpr(new VariableIdentifierExpr("A"), generateConstant(typeB,
              String.valueOf(FuzzerConstants.MAX_INT_VALUE - 1)), BinOp.MOD_ASSIGN)),
          new ParenExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
              new VariableIdentifierExpr("B"), BinOp.MOD_ASSIGN)))));
    }
    return new FunctionDefinition(new FunctionPrototype("SAFE_MOD_ASSIGN", typeA,
        Arrays.asList(new ParameterDecl("A", new QualifiedType(typeA,
                Collections.singletonList(TypeQualifier.INOUT_PARAM)), null),
            new ParameterDecl("B", typeB, null))),
        new BlockStmt(stmts, true));
  }

  public static Declaration generateAbsWrapper(BasicType type, BasicType useless) {
    List<Stmt> stmts = new ArrayList<>();
    assert type.getElementType() == BasicType.INT;
    if (type.isScalar()) {
      stmts.add((new ReturnStmt(new TernaryExpr(new BinaryExpr(
          new VariableIdentifierExpr("A"),
          new IntConstantExpr(String.valueOf(FuzzerConstants.MIN_INT_VALUE)), BinOp.EQ),
          new IntConstantExpr(String.valueOf(FuzzerConstants.MAX_INT_VALUE)),
          new FunctionCallExpr("abs", new VariableIdentifierExpr(
              "A"))))));
    } else {
      for (int i = 0; i < type.getNumElements(); i++) {
        stmts.add(new ExprStmt(new BinaryExpr(new ArrayIndexExpr(new VariableIdentifierExpr("A"),
            new IntConstantExpr(String.valueOf(i))), new TernaryExpr(new BinaryExpr(
                new ArrayIndexExpr(new VariableIdentifierExpr("A"),
                    new IntConstantExpr(String.valueOf(i))),
                new IntConstantExpr(String.valueOf(FuzzerConstants.MIN_INT_VALUE)), BinOp.EQ),
                new IntConstantExpr(String.valueOf(FuzzerConstants.MAX_INT_VALUE)),
                new FunctionCallExpr("abs",  new ArrayIndexExpr(new VariableIdentifierExpr("A"),
                    new IntConstantExpr(String.valueOf(i))))), BinOp.ASSIGN)));
      }
      stmts.add(new ReturnStmt(new VariableIdentifierExpr("A")));
    }
    return new FunctionDefinition(new FunctionPrototype("SAFE_ABS", type,
        Collections.singletonList(new ParameterDecl("A", type, null))),
        new BlockStmt(stmts, true));
  }

  private static List<Stmt> generateStdBitWrapperBase() {
    // Declares a safe new offset variable
    List<Stmt> declarationList = new ArrayList<>();
    VariablesDeclaration offsetDeclaration = new VariablesDeclaration(BasicType.INT,
        Collections.singletonList(new VariableDeclInfo(
            "safe_offset", null,
            new Initializer(new BinaryExpr(new FunctionCallExpr("SAFE_ABS",
                new VariableIdentifierExpr("offset")), new IntConstantExpr("32"), BinOp.MOD)))));
    declarationList.add(new DeclarationStmt(offsetDeclaration));
    // Declare a safe new bits variable
    VariablesDeclaration bitsDeclaration = new VariablesDeclaration(BasicType.INT,
        Collections.singletonList(new VariableDeclInfo(
            "safe_bits", null,
            new Initializer(new BinaryExpr(new FunctionCallExpr("SAFE_ABS",
                new VariableIdentifierExpr("bits")),
                new ParenExpr(new BinaryExpr(new IntConstantExpr("32"),
                    new VariableIdentifierExpr("safe_offset"), BinOp.SUB)),
                BinOp.MOD)))));
    declarationList.add(new DeclarationStmt(bitsDeclaration));
    return declarationList;
  }

  public static Declaration generateBitInsertWrapper(BasicType valueType, BasicType useless) {
    List<Stmt> body = generateStdBitWrapperBase();
    Expr funCallExpr = new FunctionCallExpr("bitfieldInsert",
        new VariableIdentifierExpr("base"), new VariableIdentifierExpr("insert"),
        new VariableIdentifierExpr("safe_offset"), new VariableIdentifierExpr("safe_bits"));
    body.add(new ReturnStmt(funCallExpr));
    return new FunctionDefinition(new FunctionPrototype("SAFE_BITFIELD_INSERT", valueType,
        Arrays.asList(new ParameterDecl("base", valueType, null),
            new ParameterDecl("insert", valueType, null),
            new ParameterDecl("offset", BasicType.INT, null),
            new ParameterDecl("bits", BasicType.INT, null))),
        new BlockStmt(body, true));
  }

  public static Declaration generateBitExtractWrapper(BasicType valueType, BasicType useless) {
    List<Stmt> body = generateStdBitWrapperBase();
    Expr funCallExpr = new FunctionCallExpr("bitfieldExtract", new VariableIdentifierExpr("value"),
        new VariableIdentifierExpr("safe_offset"), new VariableIdentifierExpr("safe_bits"));
    body.add(new ReturnStmt(funCallExpr));
    return new FunctionDefinition(new FunctionPrototype("SAFE_BITFIELD_EXTRACT", valueType,
        Arrays.asList(new ParameterDecl("value", valueType, null),
            new ParameterDecl("offset", BasicType.INT, null),
            new ParameterDecl("bits", BasicType.INT, null))),
        new BlockStmt(body, true));
  }

  private static Expr generateFloatTestExpr(Expr innerTestExpr, BasicType resultType) {
    if (resultType.equals(BasicType.FLOAT)) {
      return new BinaryExpr(new BinaryExpr(new FunctionCallExpr("abs", innerTestExpr.clone()),
          new FloatConstantExpr((1 << 24) + ".0f"), BinOp.GE),
          new BinaryExpr(new FunctionCallExpr("abs", innerTestExpr.clone()), new FloatConstantExpr(
              "1.0f"), BinOp.LT), BinOp.LOR);
    } else {
      return new BinaryExpr(
          new FunctionCallExpr("any", new FunctionCallExpr("greaterThanEqual",
            innerTestExpr.clone(), generateConstant(resultType, (1 << 24) + ".0"))),
          new FunctionCallExpr("any", new FunctionCallExpr("lessThan", innerTestExpr.clone(),
              generateConstant(resultType, "1.0"))), BinOp.LOR);
    }
  }

  // TODO Add support for matrices
  public static Declaration generateAddAssignWrapper(BasicType leftType, BasicType rightType) {
    Expr addAssignExpr =
        new TernaryExpr(generateFloatTestExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
            new VariableIdentifierExpr("B"), BinOp.ADD), leftType),
            new BinaryExpr(new VariableIdentifierExpr("A"), generateConstant(leftType, "8.0"),
                BinOp.ASSIGN),
            new ParenExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
                new VariableIdentifierExpr("B"), BinOp.ADD_ASSIGN)));
    return new FunctionDefinition(new FunctionPrototype("SAFE_ADD_ASSIGN", leftType,
        Arrays.asList(new ParameterDecl("A", new QualifiedType(leftType,
            Collections.singletonList(TypeQualifier.INOUT_PARAM)), null),
          new ParameterDecl("B", rightType, null))),
        new BlockStmt(Collections.singletonList(new ReturnStmt(addAssignExpr)), true));
  }

  public static Declaration generateSubAssignWrapper(BasicType leftType, BasicType rightType) {
    Expr subAssignExpr =
        new TernaryExpr(generateFloatTestExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
            new VariableIdentifierExpr("B"), BinOp.SUB), leftType),
            new BinaryExpr(new VariableIdentifierExpr("A"), generateConstant(leftType, "5.0"),
                BinOp.ASSIGN),
            new ParenExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
                new VariableIdentifierExpr("B"), BinOp.SUB_ASSIGN)));
    return new FunctionDefinition(new FunctionPrototype("SAFE_SUB_ASSIGN", leftType,
        Arrays.asList(new ParameterDecl("A", new QualifiedType(leftType,
                Collections.singletonList(TypeQualifier.INOUT_PARAM)), null),
            new ParameterDecl("B", rightType, null))),
        new BlockStmt(Collections.singletonList(new ReturnStmt(subAssignExpr)), true));
  }

  public static Declaration generateMulAssignWrapper(BasicType leftType, BasicType rightType) {
    Expr mulAssignExpr =
        new TernaryExpr(generateFloatTestExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
            new VariableIdentifierExpr("B"), BinOp.MUL), leftType),
            new BinaryExpr(new VariableIdentifierExpr("A"), generateConstant(leftType, "12.0"),
                BinOp.ASSIGN),
            new ParenExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
                new VariableIdentifierExpr("B"), BinOp.MUL_ASSIGN)));
    return new FunctionDefinition(new FunctionPrototype("SAFE_MUL_ASSIGN", leftType,
        Arrays.asList(new ParameterDecl("A", new QualifiedType(leftType,
                Collections.singletonList(TypeQualifier.INOUT_PARAM)), null),
            new ParameterDecl("B", rightType, null))),
        new BlockStmt(Collections.singletonList(new ReturnStmt(mulAssignExpr)), true));
  }


  //TODO add suuport for matrices
  private static Declaration generateCommonUnaryWrapper(String functionName, BasicType operandType,
                                                        Expr testOperand, Expr defaultOperand,
                                                        Expr correctReturn) {
    Expr unaryExpr = new TernaryExpr(
        generateFloatTestExpr(testOperand, operandType),
        new BinaryExpr(new VariableIdentifierExpr("A"), defaultOperand,
            BinOp.ASSIGN),
        correctReturn);
    return new FunctionDefinition(new FunctionPrototype(functionName,
        operandType,
        Collections.singletonList(new ParameterDecl("A", new QualifiedType(operandType,
        Collections.singletonList(TypeQualifier.INOUT_PARAM)), null))),
            new BlockStmt(Collections.singletonList(new ReturnStmt(unaryExpr)), true));
  }

  public static Declaration generatePreDecWrapper(BasicType operandType, BasicType useless) {
    return generateCommonUnaryWrapper("SAFE_PRE_DEC", operandType,
        new BinaryExpr(new VariableIdentifierExpr("A"),
          new FloatConstantExpr("1.0f"), BinOp.SUB),
        generateConstant(operandType, "3.0"),
        new UnaryExpr(new VariableIdentifierExpr("A"),
        UnOp.PRE_DEC));
  }

  public static Declaration generatePreIncWrapper(BasicType operandType, BasicType useless) {
    return generateCommonUnaryWrapper("SAFE_PRE_INC", operandType,
        new BinaryExpr(new VariableIdentifierExpr("A"),
          new FloatConstantExpr("1.0f"), BinOp.ADD),
        generateConstant(operandType, "7.0"),
        new UnaryExpr(new VariableIdentifierExpr("A"), UnOp.PRE_INC));
  }

  public static Declaration generatePostDecWrapper(BasicType operandType, BasicType useless) {
    return generateCommonUnaryWrapper("SAFE_POST_DEC", operandType,
        new BinaryExpr(new VariableIdentifierExpr("A"),
          new FloatConstantExpr("1.0f"), BinOp.SUB),
        generateConstant(operandType, "2.0"),
        new UnaryExpr(new VariableIdentifierExpr("A"),
        UnOp.POST_DEC));
  }

  public static Declaration generatePostIncWrapper(BasicType operandType, BasicType useless) {
    return generateCommonUnaryWrapper("SAFE_POST_INC", operandType,
        new BinaryExpr(new VariableIdentifierExpr("A"),
          new FloatConstantExpr("1.0f"), BinOp.ADD),
        generateConstant(operandType, "1.0"),
        new UnaryExpr(new VariableIdentifierExpr("A"),
        UnOp.POST_INC));
  }

  //TODO add support for matrices
  public static Declaration generateFloatResultWrapper(BasicType basicType, BasicType useless) {
    Expr assignExpr =
          new TernaryExpr(generateFloatTestExpr(new VariableIdentifierExpr("A"), basicType),
              generateConstant(basicType, "10.0"),
              new VariableIdentifierExpr("A"));
    return new FunctionDefinition(new FunctionPrototype("SAFE_FLOAT_RESULT", basicType,
        Collections.singletonList(new ParameterDecl("A", basicType, null))),
        new BlockStmt(Collections.singletonList(new ReturnStmt(assignExpr)), true));
  }
}
