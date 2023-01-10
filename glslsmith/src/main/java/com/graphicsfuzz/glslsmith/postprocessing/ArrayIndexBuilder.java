package com.graphicsfuzz.glslsmith.postprocessing;

import com.graphicsfuzz.common.ast.expr.ArrayIndexExpr;
import com.graphicsfuzz.common.ast.expr.BinOp;
import com.graphicsfuzz.common.ast.expr.BinaryExpr;
import com.graphicsfuzz.common.ast.expr.Expr;
import com.graphicsfuzz.common.ast.expr.FunctionCallExpr;
import com.graphicsfuzz.common.ast.expr.IntConstantExpr;
import com.graphicsfuzz.common.ast.expr.LengthExpr;
import com.graphicsfuzz.common.ast.expr.ParenExpr;
import com.graphicsfuzz.common.ast.expr.VariableIdentifierExpr;
import com.graphicsfuzz.common.ast.type.ArrayType;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.glslsmith.config.ConfigInterface;

public class ArrayIndexBuilder extends BaseWrapperBuilder {

  private final boolean useClamp;

  public ArrayIndexBuilder(boolean useClamp) {
    this.useClamp = useClamp;
  }

  @Override
  public void visitArrayIndexExpr(ArrayIndexExpr arrayIndexExpr) {
    Expr arrayExpr = arrayIndexExpr.getArray();
    if (typer.lookupType(arrayExpr).getWithoutQualifiers() instanceof ArrayType
        && !(arrayExpr instanceof VariableIdentifierExpr
        && ((VariableIdentifierExpr) arrayExpr).getName().equals("ids"))) {
      Expr indexExpr = arrayIndexExpr.getIndex();
      if (useClamp) {
        arrayIndexExpr.setIndex(new FunctionCallExpr("clamp", indexExpr, new IntConstantExpr("0"),
            new BinaryExpr(new LengthExpr(arrayExpr), new IntConstantExpr("1"),
                BinOp.SUB)));
      } else {
        int id = programState.wrapperCounterPostIncrement();
        // No necessary wrappers on that case
        if (programState.getRunType() == ConfigInterface.RunType.REDUCED_WRAPPERS
            && programState.lookupIds(id)) {
          arrayIndexExpr.setIndex(new BinaryExpr(new ParenExpr(indexExpr),
              new LengthExpr(arrayExpr),
              BinOp.MOD));
          // Abs wrapper with id
        } else if (programState.getRunType() == ConfigInterface.RunType.ADDED_ID) {
          programState.registerWrapper(Wrapper.SAFE_ABS, BasicType.INT, null);
          arrayIndexExpr.setIndex(new BinaryExpr(new FunctionCallExpr("SAFE_ABS", indexExpr,
              new IntConstantExpr(String.valueOf(id))), new LengthExpr(arrayExpr), BinOp.MOD));
        } else {
          programState.registerWrapper(Wrapper.SAFE_ABS, BasicType.INT, null);
          arrayIndexExpr.setIndex(new BinaryExpr(new FunctionCallExpr("SAFE_ABS", indexExpr),
              new LengthExpr(arrayExpr), BinOp.MOD));
        }
      }
    }
    super.visitArrayIndexExpr(arrayIndexExpr);
  }
}
