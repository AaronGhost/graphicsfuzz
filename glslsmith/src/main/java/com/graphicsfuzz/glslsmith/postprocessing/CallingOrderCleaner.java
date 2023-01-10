package com.graphicsfuzz.glslsmith.postprocessing;

import com.graphicsfuzz.common.ast.decl.ArrayInfo;
import com.graphicsfuzz.common.ast.decl.FunctionPrototype;
import com.graphicsfuzz.common.ast.decl.Initializer;
import com.graphicsfuzz.common.ast.decl.ParameterDecl;
import com.graphicsfuzz.common.ast.decl.VariableDeclInfo;
import com.graphicsfuzz.common.ast.decl.VariablesDeclaration;
import com.graphicsfuzz.common.ast.expr.ArrayConstructorExpr;
import com.graphicsfuzz.common.ast.expr.ArrayIndexExpr;
import com.graphicsfuzz.common.ast.expr.BinOp;
import com.graphicsfuzz.common.ast.expr.BinaryExpr;
import com.graphicsfuzz.common.ast.expr.Expr;
import com.graphicsfuzz.common.ast.expr.FunctionCallExpr;
import com.graphicsfuzz.common.ast.expr.TernaryExpr;
import com.graphicsfuzz.common.ast.expr.UnaryExpr;
import com.graphicsfuzz.common.ast.expr.VariableIdentifierExpr;
import com.graphicsfuzz.common.ast.stmt.BlockStmt;
import com.graphicsfuzz.common.ast.stmt.DeclarationStmt;
import com.graphicsfuzz.common.ast.stmt.DoStmt;
import com.graphicsfuzz.common.ast.stmt.ExprStmt;
import com.graphicsfuzz.common.ast.stmt.ForStmt;
import com.graphicsfuzz.common.ast.stmt.IfStmt;
import com.graphicsfuzz.common.ast.stmt.ReturnStmt;
import com.graphicsfuzz.common.ast.stmt.Stmt;
import com.graphicsfuzz.common.ast.stmt.SwitchStmt;
import com.graphicsfuzz.common.ast.stmt.WhileStmt;
import com.graphicsfuzz.common.ast.type.ArrayType;
import com.graphicsfuzz.common.ast.type.QualifiedType;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.common.ast.type.TypeQualifier;
import com.graphicsfuzz.common.ast.visitors.StandardVisitor;
import com.graphicsfuzz.common.typing.Typer;
import com.graphicsfuzz.glslsmith.ProgramState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
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
  private final Set<String> tempExprReadEntries = new TreeSet<>();
  private final Set<String> tempExprWrittenEntries = new TreeSet<>();
  private final Set<String> seenExprReadEntries = new TreeSet<>();
  private final Set<String> seenExprWrittenEntries = new TreeSet<>();
  private final Set<String> currentInitExprReadEntries = new HashSet<>();
  private final Set<String> currentInitExprWrittenEntries = new HashSet<>();
  private final Set<String> seenInitReadEntries = new HashSet<>();
  private final Set<String> seenInitWrittenEntries = new HashSet<>();
  private final Stack<Set<String>> seenFunCallEntries = new Stack<>();
  private final Set<String> seenFunCallEntriesInThatArg = new HashSet<>();
  private final Map<String, Pair<String, Type>> tempInitMap = new HashMap<>();

  @Override
  public void visitArrayIndexExpr(ArrayIndexExpr arrayIndexExpr) {
    // Save the original context of entry
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
    // Save the forbidden expressions for all inner expressions
    final Set<String> seenWrittenEntriesBeforeFunCall = new TreeSet<>(seenExprWrittenEntries);
    final Set<String> seenReadEntriesBeforeFunCall = new TreeSet<>(seenExprReadEntries);

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

      // Reset temp entries and add test expressions to it
      updateTempEntriesAndRestore(seenWrittenEntriesBeforeFunCall, seenReadEntriesBeforeFunCall,
          !parametersIterator.hasPrevious());
      exprCounter++;
    }
    // Add the value from the temp to the entries
    seenExprReadEntries.addAll(tempExprReadEntries);
    seenExprWrittenEntries.addAll(tempExprWrittenEntries);

    isFunCall--;
    seenFunCallEntries.pop();
    funCounter++;
  }

  @Override
  public void visitInitializer(Initializer initializer) {
    super.visitInitializer(initializer);

    // Clear the expressions sets
    seenExprReadEntries.clear();
    seenExprWrittenEntries.clear();
    exprCounter++;
  }

  @Override
  public void visitVariableIdentifierExpr(VariableIdentifierExpr variableIdentifierExpr) {
    final String variableName = variableIdentifierExpr.getName();

    // Check if rewriting is necessary and provide the rewriting name
    String tempName = null;

    // Write after read/ write in the same expr
    if (isExprSideEffecting && (seenExprReadEntries.contains(variableName)
        || seenExprWrittenEntries.contains(variableName))) {
      tempName = variableName + "_expr_" + exprCounter + "_temp_" + tempCounter;
      tempCounter += 1;

    // read after write in the same expr
    } else if (seenExprWrittenEntries.contains(variableName)) {
      tempName = variableName + "_expr_" + exprCounter + "_temp_read";

    // Write after read/ write in a different member of an initializer
    } else if (isInitializer > 0 && isExprSideEffecting
        && (seenInitReadEntries.contains(variableName)
        || seenInitWrittenEntries.contains(variableName))) {
      tempName = variableName + "_init_" + initializerCounter + "_temp_" + tempCounter;
      tempCounter += 1;

    // Read after write in a different member of an initializer
    } else if (isInitializer > 0 && (seenInitWrittenEntries.contains(variableName))) {
      tempName = variableName + "_init_" + initializerCounter + "_temp_read";

    // Write after write as out paramters of a function
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
      seenExprWrittenEntries.add(variableName);
    } else {
      seenExprReadEntries.add(variableName);
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


  private void updateTempEntriesAndRestore(Set<String> seenWrittenEntriesBeforeExpr,
                                                      Set<String> seenReadEntriesBeforeExpr,
                                                      boolean clearSets) {
    // Clear the temp Sets before adding current values to them
    if (clearSets) {
      tempExprReadEntries.clear();
      tempExprWrittenEntries.clear();
    }

    // Store the current values of the entries to the temp
    tempExprReadEntries.addAll(seenExprReadEntries);
    tempExprWrittenEntries.addAll(seenExprWrittenEntries);

    // Restore with the values provided as arguments
    seenExprWrittenEntries.clear();
    seenExprWrittenEntries.addAll(seenWrittenEntriesBeforeExpr);
    seenExprReadEntries.clear();
    seenExprReadEntries.addAll(seenReadEntriesBeforeExpr);
  }

  @Override
  public void visitTernaryExpr(TernaryExpr ternaryExpr) {

    // Save the forbidden expressions for all inner expressions
    final Set<String> seenWrittenEntriesBeforeOp = new TreeSet<>(seenExprWrittenEntries);
    final Set<String> seenReadEntriesBeforeOp = new TreeSet<>(seenExprReadEntries);

    // Visit test
    visitChildFromParent(ternaryExpr.getTest(), ternaryExpr);

    // Reset temp entries and add test expressions to it
    updateTempEntriesAndRestore(seenWrittenEntriesBeforeOp, seenReadEntriesBeforeOp,
        true);

    // Visit left operand
    visitChildFromParent(ternaryExpr.getThenExpr(), ternaryExpr);

    // Add left expressions to the temp Entries
    updateTempEntriesAndRestore(seenWrittenEntriesBeforeOp, seenReadEntriesBeforeOp, false);

    // Visit right operand
    visitChildFromParent(ternaryExpr.getElseExpr(), ternaryExpr);

    // Add the value from the temp to the entries
    seenExprReadEntries.addAll(tempExprReadEntries);
    seenExprWrittenEntries.addAll(tempExprWrittenEntries);
  }

  @Override
  public void visitBinaryExpr(BinaryExpr binaryExpr) {
    BinOp binOp = binaryExpr.getOp();

    // Check if the expression has a well-defined evaluation order
    if (binOp.isSideEffecting() || binOp == BinOp.LAND || binOp == BinOp.LOR
        || binOp == BinOp.COMMA) {
      // Save the forbidden expressions for both left and right operands on binary operator with
      // defined evaluation order
      final Set<String> seenWrittenEntriesBeforeOp = new TreeSet<>(seenExprWrittenEntries);
      final Set<String> seenReadEntriesBeforeOp = new TreeSet<>(seenExprReadEntries);

      // Examine if op is side-effecting and updates visitor state for the left operand
      if (binOp.isSideEffecting()) {
        isExprSideEffecting = true;
      }
      visitChildFromParent(binaryExpr.getLhs(), binaryExpr);

      // Store the expressions of the left operand and restore the list of forbidden expressions
      updateTempEntriesAndRestore(seenWrittenEntriesBeforeOp, seenReadEntriesBeforeOp, true);

      isExprSideEffecting = false;
      visitChildFromParent(binaryExpr.getRhs(), binaryExpr);

      // Add back the removed expressions from the left expression
      seenExprWrittenEntries.addAll(tempExprWrittenEntries);
      seenExprReadEntries.addAll(tempExprReadEntries);

    } else {
      super.visitBinaryExpr(binaryExpr);
    }
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
    boolean firstArg = true;

    // Store the forbidden expression from the beginning of the expression first
    final Set<String> seenExprWrittenEntriesBeforeOp = new TreeSet<>(seenExprWrittenEntries);
    final Set<String> seenExprReadEntriesBeforeOp = new TreeSet<>(seenExprReadEntries);
    for (Expr e : arrayConstructorExpr.getArgs()) {
      visitChildFromParent(e, arrayConstructorExpr);
      seenInitWrittenEntries.addAll(currentInitExprWrittenEntries);
      seenInitReadEntries.addAll(currentInitExprReadEntries);
      currentInitExprReadEntries.clear();
      currentInitExprWrittenEntries.clear();

      // Update the temp expr with the expr from the current value
      updateTempEntriesAndRestore(seenExprWrittenEntriesBeforeOp, seenExprReadEntriesBeforeOp,
          firstArg);
      firstArg = false;
    }
    // Exit initializer
    seenExprWrittenEntries.addAll(tempExprWrittenEntries);
    seenExprReadEntries.addAll(tempExprReadEntries);

    // update counters
    initializerCounter++;
    isInitializer--;
    // If we are out of the initializer we clear current rewriting
    if (isInitializer == 0) {
      seenInitWrittenEntries.clear();
      seenInitReadEntries.clear();
    }
  }

  @Override
  public void visitExprStmt(ExprStmt exprStmt) {
    super.visitExprStmt(exprStmt);

    seenExprReadEntries.clear();
    seenExprWrittenEntries.clear();
    exprCounter++;
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

    visitChildFromParent(ifStmt.getCondition(), ifStmt);

    // Clear the expressions sets
    seenExprReadEntries.clear();
    seenExprWrittenEntries.clear();
    exprCounter++;
  }

  @Override
  public void visitWhileStmt(WhileStmt whileStmt) {
    visitChildFromParent(whileStmt.getBody(), whileStmt);
    // We can't have an array constructor in the condition part
    if (!tempInitMap.isEmpty()) {
      whileStmt.setBody(buildBlockStmt(whileStmt.getBody(),
          tempInitMap));
      tempInitMap.clear();
    }

    visitChildFromParent(whileStmt.getCondition(), whileStmt);

    // Clear the expressions sets
    seenExprReadEntries.clear();
    seenExprWrittenEntries.clear();
    exprCounter++;
  }

  @Override
  public void visitForStmt(ForStmt forStmt) {
    visitChildFromParent(forStmt.getBody(), forStmt);
    if (!tempInitMap.isEmpty()) {
      forStmt.setBody(buildBlockStmt(forStmt.getBody(),
          tempInitMap));
      tempInitMap.clear();
    }

    visitChildFromParent(forStmt.getInit(), forStmt);

    if (forStmt.hasIncrement()) {
      visitChildFromParent(forStmt.getIncrement(), forStmt);
    }

    // Clear the expressions sets
    seenExprReadEntries.clear();
    seenExprWrittenEntries.clear();
    exprCounter++;

    if (forStmt.hasCondition()) {
      visitChildFromParent(forStmt.getCondition(), forStmt);
    }

    // Clear the expressions sets
    seenExprReadEntries.clear();
    seenExprWrittenEntries.clear();
    exprCounter++;
  }


  @Override
  public void visitSwitchStmt(SwitchStmt switchStmt) {
    visitChildFromParent(switchStmt.getBody(), switchStmt);

    visitChildFromParent(switchStmt.getExpr(), switchStmt);

    // Clear the expressions sets
    seenExprReadEntries.clear();
    seenExprWrittenEntries.clear();
    exprCounter++;
  }

  @Override
  public void visitDoStmt(DoStmt doStmt) {
    visitChildFromParent(doStmt.getBody(), doStmt);
    if (!tempInitMap.isEmpty()) {
      doStmt.setBody(buildBlockStmt(doStmt.getBody(),
          tempInitMap));
      tempInitMap.clear();
    }

    visitChildFromParent(doStmt.getCondition(), doStmt);

    // Clear the expressions sets
    seenExprReadEntries.clear();
    seenExprWrittenEntries.clear();
    exprCounter++;
  }

  @Override
  public void visitReturnStmt(ReturnStmt returnStmt) {
    super.visitReturnStmt(returnStmt);

    // Clear the expressions sets
    seenExprReadEntries.clear();
    seenExprWrittenEntries.clear();
    exprCounter++;
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
