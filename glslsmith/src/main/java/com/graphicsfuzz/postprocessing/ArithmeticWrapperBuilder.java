package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.common.ast.IAstNode;
import com.graphicsfuzz.common.ast.expr.BinOp;
import com.graphicsfuzz.common.ast.expr.BinaryExpr;
import com.graphicsfuzz.common.ast.expr.Expr;
import com.graphicsfuzz.common.ast.expr.FunctionCallExpr;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.Type;
import java.util.HashMap;
import java.util.Map;

public class ArithmeticWrapperBuilder extends BaseWrapperBuilder {
  protected Map<IAstNode, IAstNode> parentMap = new HashMap<>();
  private final Map<BinOp, Operation> wrapperOpMap = new HashMap<>();

  public ArithmeticWrapperBuilder() {
    super();
    wrapperOpMap.put(BinOp.DIV, Operation.SAFE_DIV);
    wrapperOpMap.put(BinOp.DIV_ASSIGN, Operation.SAFE_DIV_ASSIGN);
    wrapperOpMap.put(BinOp.SHL, Operation.SAFE_LSHIFT);
    wrapperOpMap.put(BinOp.SHL_ASSIGN, Operation.SAFE_LSHIFT_ASSIGN);
    wrapperOpMap.put(BinOp.SHR, Operation.SAFE_RSHIFT);
    wrapperOpMap.put(BinOp.SHR_ASSIGN, Operation.SAFE_RSHIFT_ASSIGN);
    wrapperOpMap.put(BinOp.MOD, Operation.SAFE_MOD);
    wrapperOpMap.put(BinOp.MOD_ASSIGN, Operation.SAFE_MOD_ASSIGN);
  }

  @Override
  protected void visitChildFromParent(IAstNode child, IAstNode parent) {
    parentMap.put(child, parent);
    super.visitChildFromParent(child, parent);
  }

  //TODO handle constant cases for left and right operands where the wrappers are not necessary
  @Override
  public void visitBinaryExpr(BinaryExpr binaryExpr) {
    BinOp op = binaryExpr.getOp();
    Type lhsType = typer.lookupType(binaryExpr.getLhs()).getWithoutQualifiers();
    Type rhsType = typer.lookupType(binaryExpr.getRhs()).getWithoutQualifiers();
    // We look for the operation and cast the two operand to their real type
    if (wrapperOpMap.containsKey(op)
        && lhsType instanceof BasicType && rhsType instanceof BasicType) {
      BasicType leftType = (BasicType) lhsType;
      BasicType rightType = (BasicType) rhsType;
      Operation wrapperOp = wrapperOpMap.get(op);

      // Register wrapper for generation with the correct operand type
      programState.registerWrapper(wrapperOp, leftType,
          rightType);

      // Build the replacement Expr and switch the child on the AST using the reference in the map
      Expr replacementExpr = new FunctionCallExpr(wrapperOp.name, binaryExpr.getLhs(),
          binaryExpr.getRhs());
      parentMap.get(binaryExpr).replaceChild(binaryExpr, replacementExpr);

      // Visit the left and right operand as "children" of the new replacement Expr
      visitChildFromParent(binaryExpr.getLhs(), replacementExpr);
      visitChildFromParent(binaryExpr.getRhs(), replacementExpr);
    } else {
      super.visitBinaryExpr(binaryExpr);
    }
  }

}
