package com.graphicsfuzz.shadergenerators;

import com.graphicsfuzz.common.ast.expr.BinOp;
import com.graphicsfuzz.common.ast.expr.BinaryExpr;
import com.graphicsfuzz.common.ast.expr.BoolConstantExpr;
import com.graphicsfuzz.common.ast.expr.Expr;
import com.graphicsfuzz.common.ast.expr.IntConstantExpr;
import com.graphicsfuzz.common.ast.expr.ParenExpr;
import com.graphicsfuzz.common.ast.expr.TernaryExpr;
import com.graphicsfuzz.common.ast.expr.UIntConstantExpr;
import com.graphicsfuzz.common.ast.expr.VariableIdentifierExpr;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class ShaderSimplifierTest {

  @Test
  public void testDoesExprContainsBinOpDirectBinaryExpr() {
    BinaryExpr trueExpr = new BinaryExpr(new VariableIdentifierExpr("A"),
        new IntConstantExpr("3"), BinOp.ADD);
    BinaryExpr falseExpr = new BinaryExpr(new VariableIdentifierExpr("B"), new IntConstantExpr("3"),
        BinOp.MOD);
    Assert.assertTrue(ShaderSimplifier.doesExprContainsBinOp(trueExpr,
        Collections.singletonList(BinOp.ADD)));
    Assert.assertFalse(ShaderSimplifier.doesExprContainsBinOp(falseExpr,
        Collections.singletonList(BinOp.DIV)));
  }

  @Test
  public void testDoesExprContainsBinOpRecursive() {
    Expr expr = new TernaryExpr(new BoolConstantExpr(true),
        new ParenExpr(new BinaryExpr(new VariableIdentifierExpr("X"), new BinaryExpr(
            new VariableIdentifierExpr("Y"), new UIntConstantExpr("12u"), BinOp.SHR),
            BinOp.MOD_ASSIGN)),
        new BinaryExpr(new VariableIdentifierExpr("X"), new VariableIdentifierExpr("Y"),
            BinOp.ADD_ASSIGN));
    Assert.assertTrue(ShaderSimplifier.doesExprContainsBinOp(expr, Arrays.asList(BinOp.MOD_ASSIGN,
        BinOp.ADD)));
    Assert.assertFalse(ShaderSimplifier.doesExprContainsBinOp(expr, Arrays.asList(BinOp.MOD,
        BinOp.ADD, BinOp.BOR)));
  }
}
