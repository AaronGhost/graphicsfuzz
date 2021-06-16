package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.ProgramState;
import com.graphicsfuzz.common.ast.TranslationUnit;
import com.graphicsfuzz.common.ast.decl.Initializer;
import com.graphicsfuzz.common.ast.decl.VariableDeclInfo;
import com.graphicsfuzz.common.ast.decl.VariablesDeclaration;
import com.graphicsfuzz.common.ast.expr.BinOp;
import com.graphicsfuzz.common.ast.expr.BinaryExpr;
import com.graphicsfuzz.common.ast.expr.IntConstantExpr;
import com.graphicsfuzz.common.ast.expr.UnOp;
import com.graphicsfuzz.common.ast.expr.UnaryExpr;
import com.graphicsfuzz.common.ast.expr.VariableIdentifierExpr;
import com.graphicsfuzz.common.ast.stmt.BlockStmt;
import com.graphicsfuzz.common.ast.stmt.BreakStmt;
import com.graphicsfuzz.common.ast.stmt.ExprStmt;
import com.graphicsfuzz.common.ast.stmt.IfStmt;
import com.graphicsfuzz.common.ast.stmt.Stmt;
import com.graphicsfuzz.common.ast.stmt.WhileStmt;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.visitors.StandardVisitor;
import java.util.Arrays;
import java.util.Collections;

public class LoopLimiter extends StandardVisitor implements PostProcessorInterface {
  ProgramState programState;
  int maxLoopBudget;
  boolean isGlobalLimiter;

  public LoopLimiter(boolean isGlobalLimiter, int maxLoopBudget) {
    this.isGlobalLimiter = isGlobalLimiter;
    this.maxLoopBudget = maxLoopBudget;
  }

  @Override
  public void visitWhileStmt(WhileStmt whileStmt) {
    super.visitWhileStmt(whileStmt);
    Stmt incrStmt = new ExprStmt(new UnaryExpr(new VariableIdentifierExpr("global_limiter"),
        UnOp.POST_INC));
    Stmt thenStmt = isGlobalLimiter ? new BreakStmt() :
        new BlockStmt(Arrays.asList(new ExprStmt(new BinaryExpr(new VariableIdentifierExpr(
            "global_limiter"),
            new IntConstantExpr("0"), BinOp.ASSIGN)), new BreakStmt()), false);
    Stmt limiterStmt = new IfStmt(new BinaryExpr(new VariableIdentifierExpr("global_limiter"),
        new IntConstantExpr(String.valueOf(maxLoopBudget)), BinOp.GT), thenStmt, null);
    Stmt bodyStmt = whileStmt.getBody();
    if (bodyStmt instanceof BlockStmt) {
      BlockStmt bodyBlockStmt = ((BlockStmt) bodyStmt);
      bodyBlockStmt.addStmt(incrStmt);
      bodyBlockStmt.addStmt(limiterStmt);
      return;
    }
    Stmt newBodyStmt = new BlockStmt(Arrays.asList(bodyStmt, incrStmt, limiterStmt), false);
    whileStmt.setBody(newBodyStmt);
  }

  @Override
  public ProgramState process(ProgramState state) {
    TranslationUnit tu = state.getTranslationUnit();
    programState = state;
    visitTranslationUnit(tu);
    tu.addDeclarationBefore(new VariablesDeclaration(BasicType.INT,
            Collections.singletonList(new VariableDeclInfo("global_limiter", null,
                new Initializer(new IntConstantExpr("0"))))),
        tu.getMainFunction());
    state.programInitialization(tu, state.getShaderKind());
    return programState;
  }
}
