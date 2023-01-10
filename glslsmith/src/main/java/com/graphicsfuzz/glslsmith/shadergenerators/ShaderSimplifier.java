package com.graphicsfuzz.glslsmith.shadergenerators;

import com.graphicsfuzz.common.ast.expr.BinOp;
import com.graphicsfuzz.common.ast.expr.BinaryExpr;
import com.graphicsfuzz.common.ast.expr.Expr;
import com.graphicsfuzz.common.ast.visitors.CheckPredicateVisitor;
import java.util.List;

public class ShaderSimplifier {

  public static boolean doesExprContainsBinOp(Expr testExpr, List<BinOp> opList) {
    return new CheckPredicateVisitor() {
      @Override
      public void visitBinaryExpr(BinaryExpr binaryExpr) {
        if (opList.contains(binaryExpr.getOp())) {
          predicateHolds();
        }
        visitChildFromParent(binaryExpr.getLhs(), binaryExpr);
        visitChildFromParent(binaryExpr.getRhs(), binaryExpr);
      }
    }.test(testExpr);
  }
}
