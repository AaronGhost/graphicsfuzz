package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.ProgramState;
import com.graphicsfuzz.common.ast.TranslationUnit;
import com.graphicsfuzz.common.ast.expr.ArrayIndexExpr;
import com.graphicsfuzz.common.ast.expr.BinOp;
import com.graphicsfuzz.common.ast.expr.BinaryExpr;
import com.graphicsfuzz.common.ast.expr.Expr;
import com.graphicsfuzz.common.ast.expr.FunctionCallExpr;
import com.graphicsfuzz.common.ast.expr.IntConstantExpr;
import com.graphicsfuzz.common.ast.expr.MemberLookupExpr;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.visitors.StandardVisitor;

public class ArrayIndexBuilder  extends StandardVisitor implements PostProcessorInterface {

  private final boolean useClamp;
  private ProgramState programState;

  public ArrayIndexBuilder(boolean useClamp) {
    this.useClamp = useClamp;
  }

  @Override
  public void visitArrayIndexExpr(ArrayIndexExpr arrayIndexExpr) {
    Expr arrayExpr = arrayIndexExpr.getArray();
    Expr indexExpr = arrayIndexExpr.getIndex();
    if (useClamp) {
      arrayIndexExpr.setIndex(new FunctionCallExpr("clamp", indexExpr, new IntConstantExpr("0"),
          new BinaryExpr(new MemberLookupExpr(arrayExpr, "length()"), new IntConstantExpr("1"),
              BinOp.SUB)));
    } else {
      programState.registerWrapper(Wrapper.Operation.SAFE_ABS, BasicType.INT, null);
      arrayIndexExpr.setIndex(new BinaryExpr(new FunctionCallExpr("SAFE_ABS", indexExpr),
          new MemberLookupExpr(arrayExpr, "length()"), BinOp.MOD));
    }
    super.visitArrayIndexExpr(arrayIndexExpr);
  }

  @Override
  public ProgramState process(ProgramState state) {
    programState = state;
    TranslationUnit tu = programState.getTranslationUnit();
    visitTranslationUnit(tu);
    programState.programInitialization(tu, programState.getShaderKind());
    return programState;
  }
}
