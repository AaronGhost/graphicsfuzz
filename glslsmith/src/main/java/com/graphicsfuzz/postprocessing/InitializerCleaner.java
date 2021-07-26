package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.ProgramState;
import com.graphicsfuzz.common.ast.decl.ArrayInfo;
import com.graphicsfuzz.common.ast.decl.Initializer;
import com.graphicsfuzz.common.ast.decl.VariableDeclInfo;
import com.graphicsfuzz.common.ast.decl.VariablesDeclaration;
import com.graphicsfuzz.common.ast.expr.ArrayConstructorExpr;
import com.graphicsfuzz.common.ast.expr.BinaryExpr;
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
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.common.ast.visitors.StandardVisitor;
import com.graphicsfuzz.common.typing.Typer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

//TODO add support for inout Funcall using the getPrototype method from the Typer
public class InitializerCleaner extends StandardVisitor implements PostProcessorInterface {
  private boolean isInitializer;
  private boolean isExprSideEffecting;
  private int tempCounter = 0;
  private int initializerCounter = 0;
  private Typer typer;
  private final Set<String> readEntries = new HashSet<>();
  private final Set<String> writtenEntries = new HashSet<>();
  private final Map<String, Pair<String, Type>> tempInitMap = new HashMap<>();
  private final Map<DeclarationStmt, Map<String, Pair<String, Type>>> changingVariablesDecls =
      new HashMap<>();

  @Override
  public void visitVariableIdentifierExpr(VariableIdentifierExpr variableIdentifierExpr) {
    String variableName = variableIdentifierExpr.getName();
    if (isInitializer) {

      // Current expression is a write operation and the value is already read / written left from
      // current initializer expression
      if (isExprSideEffecting && (writtenEntries.contains(variableName)
          || readEntries.contains(variableName))) {
        // Allocate the new name
        String tempName = variableName + "_init_" + initializerCounter + "_temp_" + tempCounter;
        tempCounter += 1;

        // Save the data to link the temp variable to the real one
        tempInitMap.put(tempName, new ImmutablePair<>(variableName,
            typer.lookupType(variableIdentifierExpr)));
        variableIdentifierExpr.setName(tempName);

        // Current expression is a read after a write, we create a temp if necessary and rewrites
        // the variable identifier

      } else if (writtenEntries.contains(variableName)) {
        String tempName = variableName + "_init_" + initializerCounter + "_temp_read";
        if (!tempInitMap.containsKey(tempName)) {
          tempInitMap.put(tempName, new ImmutablePair<>(variableName,
              typer.lookupType(variableIdentifierExpr)));
        }
        variableIdentifierExpr.setName(tempName);
      }
      // Current expression is something that has not been seen yet so we add it to either the
      // read or write variable
      if (isExprSideEffecting) {
        writtenEntries.add(variableName);
      } else {
        readEntries.add(variableName);
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
    isInitializer = true;
    readEntries.clear();
    writtenEntries.clear();
    super.visitArrayConstructorExpr(arrayConstructorExpr);
    initializerCounter += 1;
    isInitializer = false;
  }

  @Override
  public void visitDeclarationStmt(DeclarationStmt declarationStmt) {
    super.visitDeclarationStmt(declarationStmt);
    // if there is a non-zero values from current initialization we save the current declaration
    // stmt for later with all the information from before
    if (!tempInitMap.isEmpty()) {
      Map<String, Pair<String, Type>> copyMap = new HashMap<>(tempInitMap);
      changingVariablesDecls.put(declarationStmt,  copyMap);
    }
    tempInitMap.clear();
  }

  @Override
  public void visitBlockStmt(BlockStmt stmt) {
    super.visitBlockStmt(stmt);
    if (!changingVariablesDecls.isEmpty()) {
      for (DeclarationStmt declStmt : changingVariablesDecls.keySet()) {
        Map<String, Pair<String, Type>> currentVarDeclVariables =
            changingVariablesDecls.get(declStmt);
        for (String tempVariableName : currentVarDeclVariables.keySet()) {
          Pair<String, Type> variableInfo = currentVarDeclVariables.get(tempVariableName);
          stmt.insertBefore(declStmt, new DeclarationStmt(buildCopyVardDecl(tempVariableName,
              variableInfo.getLeft(), variableInfo.getRight())));
        }
      }
    }
    changingVariablesDecls.clear();
  }

  @Override
  public void visitIfStmt(IfStmt ifStmt) {
    visitChildFromParent(ifStmt.getCondition(), ifStmt);
    visitChildFromParent(ifStmt.getThenStmt(), ifStmt);
    if (!changingVariablesDecls.isEmpty()) {
      ifStmt.setThenStmt(buildBlockStmt(ifStmt.getThenStmt(), getUniqueVarDeclMapOrFail()));
      changingVariablesDecls.clear();
    }
    visitChildFromParent(ifStmt.getElseStmt(), ifStmt);
    if (!changingVariablesDecls.isEmpty()) {
      ifStmt.setElseStmt(buildBlockStmt(ifStmt.getThenStmt(), getUniqueVarDeclMapOrFail()));
      changingVariablesDecls.clear();
    }
  }

  @Override
  public void visitWhileStmt(WhileStmt whileStmt) {
    super.visitWhileStmt(whileStmt);
    // We can't have an array constructor in the condition part
    if (!changingVariablesDecls.isEmpty()) {
      whileStmt.setBody(buildBlockStmt(whileStmt.getBody(),
          getUniqueVarDeclMapOrFail()));
      changingVariablesDecls.clear();
    }
  }

  @Override
  public void visitForStmt(ForStmt forStmt) {
    super.visitForStmt(forStmt);
    if (!changingVariablesDecls.isEmpty()) {
      forStmt.setBody(buildBlockStmt(forStmt.getBody(),
          getUniqueVarDeclMapOrFail()));
      changingVariablesDecls.clear();
    }
  }

  @Override
  public void visitDoStmt(DoStmt doStmt) {
    super.visitDoStmt(doStmt);
    if (!changingVariablesDecls.isEmpty()) {
      doStmt.setBody(buildBlockStmt(doStmt.getBody(),
          getUniqueVarDeclMapOrFail()));
      changingVariablesDecls.clear();
    }
  }

  private Map<String, Pair<String, Type>> getUniqueVarDeclMapOrFail() {
    if (changingVariablesDecls.size() != 1) {
      throw new RuntimeException("Attempt to change a statement child with multiple declarations");
    } else {
      return changingVariablesDecls.values().iterator().next();
    }
  }

  private VariablesDeclaration buildCopyVardDecl(String newName, String oldName,
                                                 Type variableType) {
    ArrayInfo arrayInfo = null;
    if (variableType.getWithoutQualifiers() instanceof ArrayType) {
      arrayInfo = ((ArrayType) variableType.getWithoutQualifiers()).getArrayInfo();
    }
    return new VariablesDeclaration(variableType, new VariableDeclInfo(newName, arrayInfo,
        new Initializer(new VariableIdentifierExpr(oldName))));
  }

  private BlockStmt buildBlockStmt(Stmt currentStmt,
                                   Map<String, Pair<String, Type>> changingVariables) {
    List<Stmt> declStmts = new ArrayList<>();
    for (String tempVariableName : changingVariables.keySet()) {
      Pair<String, Type> variableInfo = changingVariables.get(tempVariableName);
      declStmts.add(new DeclarationStmt(buildCopyVardDecl(tempVariableName,
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
