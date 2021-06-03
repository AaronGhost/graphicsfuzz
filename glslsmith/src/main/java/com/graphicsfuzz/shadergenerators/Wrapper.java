package com.graphicsfuzz.shadergenerators;

import com.graphicsfuzz.FuzzerConstants;
import com.graphicsfuzz.common.ast.decl.Declaration;
import com.graphicsfuzz.common.ast.decl.FunctionDefinition;
import com.graphicsfuzz.common.ast.decl.FunctionPrototype;
import com.graphicsfuzz.common.ast.decl.ParameterDecl;
import com.graphicsfuzz.common.ast.expr.ArrayIndexExpr;
import com.graphicsfuzz.common.ast.expr.BinOp;
import com.graphicsfuzz.common.ast.expr.BinaryExpr;
import com.graphicsfuzz.common.ast.expr.Expr;
import com.graphicsfuzz.common.ast.expr.FunctionCallExpr;
import com.graphicsfuzz.common.ast.expr.IntConstantExpr;
import com.graphicsfuzz.common.ast.expr.ParenExpr;
import com.graphicsfuzz.common.ast.expr.TernaryExpr;
import com.graphicsfuzz.common.ast.expr.TypeConstructorExpr;
import com.graphicsfuzz.common.ast.expr.UIntConstantExpr;
import com.graphicsfuzz.common.ast.expr.VariableIdentifierExpr;
import com.graphicsfuzz.common.ast.stmt.BlockStmt;
import com.graphicsfuzz.common.ast.stmt.ExprStmt;
import com.graphicsfuzz.common.ast.stmt.ReturnStmt;
import com.graphicsfuzz.common.ast.stmt.Stmt;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.QualifiedType;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.common.ast.type.TypeQualifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;


public abstract class Wrapper {
  public enum Operation {
    SAFE_ABS(Wrapper::generateAbsWrapper, "SAFE_ABS", false),
    SAFE_DIV(Wrapper::generateDivWrapper, "SAFE_DIV", false),
    SAFE_DIV_ASSIGN(Wrapper::generateDivAssignWrapper, "SAFE_DIV_ASSIGN", true),
    SAFE_LSHIFT(Wrapper::generateLShiftWrapper, "SAFE_LSHIFT", false),
    SAFE_LSHIFT_ASSIGN(Wrapper::generateLShiftAssignWrapper, "SAFE_LSHIFT_ASSIGN", true),
    SAFE_RSHIFT(Wrapper::generateRShiftWrapper, "SAFE_RSHIFT", false),
    SAFE_RSHIFT_ASSIGN(Wrapper::generateRShiftAssignWrapper, "SAFE_RSHIFT_ASSIGN", true),
    SAFE_MOD(Wrapper::generateModWrapper, "SAFE_MOD", false),
    SAFE_MOD_ASSIGN(Wrapper::generateModAssignWrapper, "SAFE_MOD_ASSIGN", true);

    public final BiFunction<BasicType, BasicType, Declaration> generator;
    public final String name;
    public final boolean inoutA;

    Operation(BiFunction<BasicType, BasicType, Declaration> generator, String name,
              boolean inoutA) {
      this.generator = generator;
      this.name = name;
      this.inoutA = inoutA;
    }
  }

  public static Declaration generateDeclaration(Operation op, BasicType typeA, BasicType typeB) {
    Type parArgA = typeA;
    if (op.inoutA) {
      parArgA = new QualifiedType(typeA, Collections.singletonList(TypeQualifier.INOUT_PARAM));
    }
    if (typeB == null) {
      return new FunctionPrototype(op.name, typeA, parArgA);
    }
    if (! typeA.isVector() && typeB.isVector()) {
      return new FunctionPrototype(op.name, typeB, parArgA, typeB);
    }
    return new FunctionPrototype(op.name, typeA, parArgA, typeB);
  }

  public static Expr generateConstant(BasicType type, String constant) {
    Expr constantExpr = type.getElementType() == BasicType.INT ? new IntConstantExpr(constant) :
        new UIntConstantExpr(constant);
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

  private static Expr generateDivTestExpr(BasicType typeA, BasicType typeB) {
    if (typeA.getElementType() == BasicType.INT) {
      Expr atestExpr = typeA.isVector() ? generateVectorComparison(typeA, "A",
          String.valueOf(FuzzerConstants.MIN_INT_VALUE)) :
          new BinaryExpr(new VariableIdentifierExpr("A"),
              new IntConstantExpr(String.valueOf(FuzzerConstants.MIN_INT_VALUE)), BinOp.EQ);
      Expr btest0Expr = typeB.isVector() ? generateVectorComparison(typeB, "B", "-1") :
          new BinaryExpr(new VariableIdentifierExpr("B"), new IntConstantExpr("0"), BinOp.EQ);
      Expr btest1Expr = typeB.isVector() ? generateVectorComparison(typeB, "B", "0") :
          new BinaryExpr(new VariableIdentifierExpr("B"), new IntConstantExpr("-1"), BinOp.EQ);
      return new BinaryExpr(btest0Expr, new BinaryExpr(atestExpr, btest1Expr,
          BinOp.LAND), BinOp.LOR);
    }
    return typeB.isVector() ? generateVectorComparison(typeB, "B", "0u") :
        new BinaryExpr(new VariableIdentifierExpr("B"), new UIntConstantExpr("0u"), BinOp.EQ);
  }


  //Arithmetic Wrapper Function declaration generator
  public static Declaration generateDivWrapper(BasicType typeA, BasicType typeB) {
    BasicType functionReturnType = typeB.isVector() ? typeB : typeA;
    String intText = typeB.getElementType() == BasicType.INT ? "2" : "2u";
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
    String intText = typeB == BasicType.INT ? "2" : "2u";
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
    if (typeB.getElementType() == BasicType.INT) {
      return new BinaryExpr(
          typeB.isVector() ? generateVectorComparison(typeB, "B", "32", "greaterThan") :
              new BinaryExpr(new VariableIdentifierExpr("B"), new IntConstantExpr("32"), BinOp.GE),
          typeB.isVector() ? generateVectorComparison(typeB, "B", "0", "lessThan") :
              new BinaryExpr(new VariableIdentifierExpr("B"), new IntConstantExpr("0"), BinOp.LT),
          BinOp.LOR);
    }
    return typeB.isVector() ? generateVectorComparison(typeB, "B", "32u", "greaterThan") :
        new BinaryExpr(new VariableIdentifierExpr("B"), new UIntConstantExpr("32u"),
            BinOp.GE);
  }

  private static Declaration generateShiftWrapper(BasicType typeA, BasicType typeB, BinOp op,
                                                  String funName) {
    String intText = (typeB.getElementType() == BasicType.INT) ? "16" : "16u";
    Expr shiftExpr = new TernaryExpr(
        generateShiftTestExpr(typeB),
        new BinaryExpr(new VariableIdentifierExpr("A"), generateConstant(typeB, intText), op),
        new BinaryExpr(new VariableIdentifierExpr("A"), new VariableIdentifierExpr("B"),
            op));
    return new FunctionDefinition(new FunctionPrototype(funName,
        typeA, Arrays.asList(new ParameterDecl("A", typeA, null),
          new ParameterDecl("B", typeB, null))),
        new BlockStmt(Collections.singletonList(new ReturnStmt(shiftExpr)), true));
  }

  private static Declaration generateShiftAssignWrapper(BasicType typeA, BasicType typeB, BinOp op,
                                                        String funName) {
    String intText = (typeB.getElementType() == BasicType.INT) ? "16" : "16u";
    Expr shiftAssignExpr = new TernaryExpr(
        generateShiftTestExpr(typeB),
        new ParenExpr(new BinaryExpr(new VariableIdentifierExpr("A"),
            generateConstant(typeB, intText), op)),
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
    if (typeB.getElementType() == BasicType.INT) {
      return typeB.isVector() ? generateVectorComparison(typeB, "B", "0") :
          new BinaryExpr(new VariableIdentifierExpr("B"),
              new IntConstantExpr("0"), BinOp.EQ);
    }
    return typeB.isVector() ? generateVectorComparison(typeB, "B", "0u") :
        new BinaryExpr(new VariableIdentifierExpr("B"),
            new UIntConstantExpr("0u"), BinOp.EQ);
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
              (FuzzerConstants.MAX_INT_VALUE - 1) + "u"), BinOp.MOD),
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
              (FuzzerConstants.MAX_INT_VALUE - 1) + "u"), BinOp.MOD_ASSIGN)),
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
}
