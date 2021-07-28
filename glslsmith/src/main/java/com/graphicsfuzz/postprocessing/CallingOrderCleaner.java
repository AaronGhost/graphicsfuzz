package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.ProgramState;
import com.graphicsfuzz.common.ast.decl.ArrayInfo;
import com.graphicsfuzz.common.ast.decl.FunctionPrototype;
import com.graphicsfuzz.common.ast.decl.Initializer;
import com.graphicsfuzz.common.ast.decl.ParameterDecl;
import com.graphicsfuzz.common.ast.decl.VariableDeclInfo;
import com.graphicsfuzz.common.ast.decl.VariablesDeclaration;
import com.graphicsfuzz.common.ast.expr.ArrayConstructorExpr;
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
  private Typer typer;
  private final Set<String> currentInitExprReadEntries = new HashSet<>();
  private final Set<String> currentInitExprWrittenEntries = new HashSet<>();
  private final Set<String> seenInitReadEntries = new HashSet<>();
  private final Set<String> seenInitWrittenEntries = new HashSet<>();
  private final Stack<Set<String>> seenFunCallEntries = new Stack<>();
  private final Set<String> seenFunCallEntriesInThatArg = new HashSet<>();
  private final Map<String, Pair<String, Type>> tempInitMap = new HashMap<>();

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

    // Handle initializers
    if (isInitializer > 0) {

      // Current expression is a write operation and the value is already read / written left from
      // current initializer expression
      if (isExprSideEffecting && (seenInitReadEntries.contains(variableName)
          || seenInitWrittenEntries.contains(variableName))) {
        // Allocate the new name
        String tempName = variableName + "_init_" + initializerCounter + "_temp_" + tempCounter;
        tempCounter += 1;

        // Save the data to link the temp variable to the real one
        tempInitMap.put(tempName, new ImmutablePair<>(variableName,
            typer.lookupType(variableIdentifierExpr)));
        variableIdentifierExpr.setName(tempName);

        // Variable registering for FunCalls and early exit (no double rewriting of variables)
        if (isFunCall > 0 && isOut) {
          seenFunCallEntriesInThatArg.add(variableName);
        }
        return;

        // Current expression is a read after a write, we create a temp if necessary and rewrites
        // the variable identifier
      } else if (seenInitWrittenEntries.contains(variableName)) {
        String tempName = variableName + "_init_" + initializerCounter + "_temp_read";
        if (!tempInitMap.containsKey(tempName)) {
          tempInitMap.put(tempName, new ImmutablePair<>(variableName,
              typer.lookupType(variableIdentifierExpr)));
        }
        variableIdentifierExpr.setName(tempName);

        // Variable registering for FunCalls and early exit (no double rewriting of variables)
        if (isFunCall > 0 && isOut) {
          seenFunCallEntriesInThatArg.add(variableName);
        }
        return;

        // Current expression is something that has not been seen yet so we add it to either the
        // read or write in current Expr
      } else if (isExprSideEffecting) {
        currentInitExprWrittenEntries.add(variableName);
      } else {
        currentInitExprReadEntries.add(variableName);
      }
    }

    // Handle Funcall: As initializers has been before, we check that the
    if (isFunCall > 0 && isOut) {
      // Current expression has been already seen in an out param before so we rewrite it
      if (seenFunCallEntries.peek().contains(variableName)) {
        String tempName = variableName + "_func_" + funCounter + "_temp_" + tempCounter;
        tempCounter += 1;
        // Save the data to link the temp variable to the real one
        tempInitMap.put(tempName, new ImmutablePair<>(variableName,
            typer.lookupType(variableIdentifierExpr)));
        variableIdentifierExpr.setName(tempName);

        // Current expression is an unseen out param
      } else {
        seenFunCallEntriesInThatArg.add(variableName);
      }
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
