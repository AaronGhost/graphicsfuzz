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
  final int maxLoopBudget;
  final boolean isGlobalLimiter;
  int loopCounter;

  public LoopLimiter(boolean isGlobalLimiter, int maxLoopBudget) {
    this.isGlobalLimiter = isGlobalLimiter;
    this.maxLoopBudget = maxLoopBudget;
    this.loopCounter = 0;
  }

  @Override
  public void visitWhileStmt(WhileStmt whileStmt) {
    super.visitWhileStmt(whileStmt);
    String limiterText = isGlobalLimiter ? "global_limiter" :
        "local_limiter_" + loopCounter;
    loopCounter++;
    Stmt incrStmt = new ExprStmt(new UnaryExpr(new VariableIdentifierExpr(limiterText),
        UnOp.POST_INC));
    Stmt thenStmt = new BreakStmt();
    Stmt limiterStmt = new IfStmt(new BinaryExpr(new VariableIdentifierExpr(limiterText),
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
    if (!isGlobalLimiter) {
      for (int i = 0; i < loopCounter; i++) {
        tu.addDeclarationBefore(new VariablesDeclaration(BasicType.INT,
                Collections.singletonList(new VariableDeclInfo("local_limiter_" + i, null,
                    new Initializer(new IntConstantExpr("0"))))),
            tu.getMainFunction());
      }
    } else if (loopCounter > 0) {
      tu.addDeclarationBefore(new VariablesDeclaration(BasicType.INT,
              Collections.singletonList(new VariableDeclInfo("global_limiter", null,
                  new Initializer(new IntConstantExpr("0"))))),
          tu.getMainFunction());
    }
    state.programInitialization(tu);
    return programState;
  }
}
