package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.ProgramState;
import com.graphicsfuzz.common.ast.decl.ArrayInfo;
import com.graphicsfuzz.common.ast.decl.FunctionPrototype;
import com.graphicsfuzz.common.ast.decl.Initializer;
import com.graphicsfuzz.common.ast.decl.ParameterDecl;
import com.graphicsfuzz.common.ast.decl.VariableDeclInfo;
import com.graphicsfuzz.common.ast.decl.VariablesDeclaration;
import com.graphicsfuzz.common.ast.expr.ArrayConstructorExpr;
import com.graphicsfuzz.common.ast.expr.ArrayIndexExpr;
import com.graphicsfuzz.common.ast.expr.BinaryExpr;
import com.graphicsfuzz.common.ast.expr.Expr;
import com.graphicsfuzz.common.ast.expr.FunctionCallExpr;
import com.graphicsfuzz.common.ast.expr.UnaryExpr;
import com.graphicsfuzz.common.ast.expr.VariableIdentifierExpr;
import com.graphicsfuzz.common.ast.stmt.BlockStmt;
import com.graphicsfuzz.common.ast.stmt.DeclarationStmt;
import com.graphicsfuzz.common.ast.stmt.DoStmt;
import com.graphicsfuzz.common.ast.stmt.ForStmt;
import com.graphicsfuzz.common.ast.stmt.IfStmt;
import com.graphicsfuzz.common.ast.stmt.Stmt;
import com.graphicsfuzz.common.ast.stmt.WhileStmt;
import com.graphicsfuzz.common.ast.type.ArrayType;
import com.graphicsfuzz.common.ast.type.QualifiedType;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.common.ast.type.TypeQualifier;
import com.graphicsfuzz.common.ast.visitors.StandardVisitor;
import com.graphicsfuzz.common.typing.Typer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class CallingOrderCleaner extends StandardVisitor implements PostProcessorInterface {
  private int isInitializer;
  private boolean isOut;
  private int isFunCall;
  private boolean isExprSideEffecting;
  private int tempCounter = 0;
  private int initializerCounter = 0;
  private int funCounter = 0;
  private int exprCounter = 0;
  private Typer typer;
  private final Set<String> currentExprReadEntries = new HashSet<>();
  private final Set<String> currentExprWrittenEntries = new HashSet<>();
  private final Set<String> seenExprReadEntries = new HashSet<>();
  private final Set<String> seenExprWrittenEntries = new HashSet<>();
  private final Set<String> currentInitExprReadEntries = new HashSet<>();
  private final Set<String> currentInitExprWrittenEntries = new HashSet<>();
  private final Set<String> seenInitReadEntries = new HashSet<>();
  private final Set<String> seenInitWrittenEntries = new HashSet<>();
  private final Stack<Set<String>> seenFunCallEntries = new Stack<>();
  private final Set<String> seenFunCallEntriesInThatArg = new HashSet<>();
  private final Map<String, Pair<String, Type>> tempInitMap = new HashMap<>();

  @Override
  public void visitArrayIndexExpr(ArrayIndexExpr arrayIndexExpr) {
    super.visitChildFromParent(arrayIndexExpr.getArray(), arrayIndexExpr);
    // Save the current writing context before entering the index expression
    final boolean wasExprSideEffecting = isExprSideEffecting;
    isExprSideEffecting = false;
    super.visitChildFromParent(arrayIndexExpr.getIndex(), arrayIndexExpr);
    // Restore the current writing context
    isExprSideEffecting = wasExprSideEffecting;
  }

  @Override
  public void visitFunctionCallExpr(FunctionCallExpr functionCallExpr) {
    seenFunCallEntries.push(new HashSet<>());
    Set<FunctionPrototype> possiblePrototypes = typer.getPrototypes(functionCallExpr.getCallee());
    FunctionPrototype correctPrototype = null;
    for (FunctionPrototype prototype : possiblePrototypes) {
      if (typer.prototypeMatches(prototype, functionCallExpr)) {
        correctPrototype = prototype;
      }
    }
    isFunCall++;
    if (correctPrototype == null) {
      throw new RuntimeException("Function call of an undeclared function: "
          + functionCallExpr.getCallee());
    }
    ListIterator<Expr> parametersIterator = functionCallExpr.getArgs().listIterator();
    for (ParameterDecl formalParameter : correctPrototype.getParameters()) {
      Expr realParameter = parametersIterator.next();
      if (formalParameter.getType().hasQualifier(TypeQualifier.OUT_PARAM)
          || formalParameter.getType().hasQualifier(TypeQualifier.INOUT_PARAM)) {
        isOut = true;
        isExprSideEffecting = true;
      }
      visitChildFromParent(realParameter, functionCallExpr);
      isOut = false;
      isExprSideEffecting = false;
      seenFunCallEntries.peek().addAll(seenFunCallEntriesInThatArg);
      seenFunCallEntriesInThatArg.clear();
    }
    isFunCall--;
    seenFunCallEntries.pop();
    funCounter++;
  }

  @Override
  public void visitVariableIdentifierExpr(VariableIdentifierExpr variableIdentifierExpr) {
    final String variableName = variableIdentifierExpr.getName();

    String tempName = null;
    if (isExprSideEffecting && (seenExprReadEntries.contains(variableName)
        || seenExprWrittenEntries.contains(variableName))) {
      tempName = variableName + "_temp_" + exprCounter;
      exprCounter += 1;
    } else if (seenExprWrittenEntries.contains(variableName)) {
      tempName = variableName + "_temp_read";
    } else if (isInitializer > 0 && isExprSideEffecting && (seenInitReadEntries.contains(variableName)
        || seenInitWrittenEntries.contains(variableName))) {
      tempName = variableName + "_init_" + initializerCounter + "_temp_" + tempCounter;
      tempCounter += 1;
    } else if (isInitializer > 0 && (seenInitWrittenEntries.contains(variableName))) {
      tempName = variableName + "_init_" + initializerCounter + "_temp_read";
    } else if (isFunCall > 0 && isOut && seenFunCallEntries.peek().contains(variableName)) {
      tempName = variableName + "_func_" + funCounter + "_temp_" + tempCounter;
      tempCounter += 1;
    }

    // Updates the variable if any condition is reached before
    if (tempName != null) {
      tempInitMap.put(tempName, new ImmutablePair<>(variableName,
          typer.lookupType(variableIdentifierExpr)));
      variableIdentifierExpr.setName(tempName);
    }

    // Always add to the expr sets
    if (isExprSideEffecting) {
      currentExprWrittenEntries.add(variableName);
    } else {
      currentExprReadEntries.add(variableName);
    }

    // If in an initializer add to the initializer sets
    if (isInitializer > 0) {
      if (isExprSideEffecting) {
        currentInitExprWrittenEntries.add(variableName);
      } else {
        currentInitExprReadEntries.add(variableName);
      }
    }

    // If in a Funcall add to the current funcall set
    if (isFunCall > 0 && isOut) {
      seenFunCallEntriesInThatArg.add(variableName);
    }
  }

  @Override
  public void visitBinaryExpr(BinaryExpr binaryExpr) {
    // Examine if op is side-effecting and updates visitor state for the left operand
    if (binaryExpr.getOp().isSideEffecting()) {
      isExprSideEffecting = true;
    }
    visitChildFromParent(binaryExpr.getLhs(), binaryExpr);
    isExprSideEffecting = false;
    visitChildFromParent(binaryExpr.getRhs(), binaryExpr);
  }

  @Override
  public void visitUnaryExpr(UnaryExpr unaryExpr) {
    if (unaryExpr.getOp().isSideEffecting()) {
      isExprSideEffecting = true;
    }
    visitChildFromParent(unaryExpr.getExpr(), unaryExpr);
    isExprSideEffecting = false;
  }

  @Override
  public void visitArrayConstructorExpr(ArrayConstructorExpr arrayConstructorExpr) {
    isInitializer++;
    // Enter an initializer
    for (Expr e : arrayConstructorExpr.getArgs()) {
      visitChildFromParent(e, arrayConstructorExpr);
      seenInitWrittenEntries.addAll(currentInitExprWrittenEntries);
      seenInitReadEntries.addAll(currentInitExprReadEntries);
      currentInitExprReadEntries.clear();
      currentInitExprWrittenEntries.clear();
    }
    // Exit initializer
    initializerCounter++;
    isInitializer--;
    // If we are out of the initializer we clear current rewriting
    if (isInitializer == 0) {
      seenInitWrittenEntries.clear();
      seenInitReadEntries.clear();
    }
  }

  @Override
  public void visitBlockStmt(BlockStmt stmt) {
    List<Stmt> children = new ArrayList<>(stmt.getStmts());
    for (Stmt child : children) {
      visitChildFromParent(child, stmt);
      if (!tempInitMap.isEmpty()) {
        for (String tempVariableName : tempInitMap.keySet()) {
          Pair<String, Type> variableInfo = tempInitMap.get(tempVariableName);
          stmt.insertBefore(child, new DeclarationStmt(buildCopyVarDecl(tempVariableName,
              variableInfo.getLeft(), variableInfo.getRight())));
        }
        tempInitMap.clear();
      }
    }
  }

  @Override
  public void visitIfStmt(IfStmt ifStmt) {
    visitChildFromParent(ifStmt.getCondition(), ifStmt);
    visitChildFromParent(ifStmt.getThenStmt(), ifStmt);
    if (!tempInitMap.isEmpty()) {
      ifStmt.setThenStmt(buildBlockStmt(ifStmt.getThenStmt(), tempInitMap));
      tempInitMap.clear();
    }
    if (ifStmt.hasElseStmt()) {
      visitChildFromParent(ifStmt.getElseStmt(), ifStmt);
      if (!tempInitMap.isEmpty()) {
        ifStmt.setElseStmt(buildBlockStmt(ifStmt.getThenStmt(), tempInitMap));
        tempInitMap.clear();
      }
    }
  }

  @Override
  public void visitWhileStmt(WhileStmt whileStmt) {
    super.visitWhileStmt(whileStmt);
    // We can't have an array constructor in the condition part
    if (!tempInitMap.isEmpty()) {
      whileStmt.setBody(buildBlockStmt(whileStmt.getBody(),
          tempInitMap));
      tempInitMap.clear();
    }
  }

  @Override
  public void visitForStmt(ForStmt forStmt) {
    super.visitForStmt(forStmt);
    if (!tempInitMap.isEmpty()) {
      forStmt.setBody(buildBlockStmt(forStmt.getBody(),
          tempInitMap));
      tempInitMap.clear();
    }
  }

  @Override
  public void visitDoStmt(DoStmt doStmt) {
    super.visitDoStmt(doStmt);
    if (!tempInitMap.isEmpty()) {
      doStmt.setBody(buildBlockStmt(doStmt.getBody(),
          tempInitMap));
      tempInitMap.clear();
    }
  }

  private VariablesDeclaration buildCopyVarDecl(String newName, String oldName,
                                                Type variableType) {
    ArrayInfo arrayInfo = null;
    Type declType = variableType;
    if (variableType.getWithoutQualifiers() instanceof ArrayType) {
      arrayInfo = ((ArrayType) variableType.getWithoutQualifiers()).getArrayInfo().clone();
      declType = new QualifiedType(((ArrayType) variableType.getWithoutQualifiers()).getBaseType(),
          new ArrayList<>());
    }
    return new VariablesDeclaration(new QualifiedType(declType.getWithoutQualifiers(),
        new ArrayList<>()),
        new VariableDeclInfo(newName, arrayInfo,
            new Initializer(new VariableIdentifierExpr(oldName))));
  }

  private BlockStmt buildBlockStmt(Stmt currentStmt,
                                   Map<String, Pair<String, Type>> changingVariables) {
    List<Stmt> declStmts = new ArrayList<>();
    for (String tempVariableName : changingVariables.keySet()) {
      Pair<String, Type> variableInfo = changingVariables.get(tempVariableName);
      declStmts.add(new DeclarationStmt(buildCopyVarDecl(tempVariableName,
          variableInfo.getLeft(), variableInfo.getRight())));
    }
    declStmts.add(currentStmt);
    return new BlockStmt(declStmts, false);
  }


  @Override
  public ProgramState process(ProgramState state) {
    typer = new Typer(state.getTranslationUnit());
    this.visitTranslationUnit(state.getTranslationUnit());
    return state;
  }
}
