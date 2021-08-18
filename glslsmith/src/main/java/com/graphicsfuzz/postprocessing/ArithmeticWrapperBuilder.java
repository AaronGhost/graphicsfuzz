package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.common.ast.IAstNode;
import com.graphicsfuzz.common.ast.expr.BinOp;
import com.graphicsfuzz.common.ast.expr.BinaryExpr;
import com.graphicsfuzz.common.ast.expr.Expr;
import com.graphicsfuzz.common.ast.expr.FunctionCallExpr;
import com.graphicsfuzz.common.ast.expr.Op;
import com.graphicsfuzz.common.ast.expr.UnOp;
import com.graphicsfuzz.common.ast.expr.UnaryExpr;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.Type;
import java.util.HashMap;
import java.util.Map;

public class ArithmeticWrapperBuilder extends BaseWrapperBuilder {
  protected Map<IAstNode, IAstNode> parentMap = new HashMap<>();
  private final Map<Op, Wrapper> intBasedOpMap = new HashMap<>();
  private final Map<Op, Wrapper> floatBaseOpMap = new HashMap<>();

  public ArithmeticWrapperBuilder() {
    super();
    // Operations that need wrappers for integer based operations
    intBasedOpMap.put(BinOp.DIV, Wrapper.SAFE_DIV);
    intBasedOpMap.put(BinOp.DIV_ASSIGN, Wrapper.SAFE_DIV_ASSIGN);
    intBasedOpMap.put(BinOp.SHL, Wrapper.SAFE_LSHIFT);
    intBasedOpMap.put(BinOp.SHL_ASSIGN, Wrapper.SAFE_LSHIFT_ASSIGN);
    intBasedOpMap.put(BinOp.SHR, Wrapper.SAFE_RSHIFT);
    intBasedOpMap.put(BinOp.SHR_ASSIGN, Wrapper.SAFE_RSHIFT_ASSIGN);
    intBasedOpMap.put(BinOp.MOD, Wrapper.SAFE_MOD);
    intBasedOpMap.put(BinOp.MOD_ASSIGN, Wrapper.SAFE_MOD_ASSIGN);

    // Operations that need dedicated wrappers for float based operations
    floatBaseOpMap.put(BinOp.ADD_ASSIGN, Wrapper.SAFE_ADD_ASSIGN);
    floatBaseOpMap.put(BinOp.SUB_ASSIGN, Wrapper.SAFE_SUB_ASSIGN);
    floatBaseOpMap.put(BinOp.MUL_ASSIGN, Wrapper.SAFE_MUL_ASSIGN);
    floatBaseOpMap.put(UnOp.POST_INC, Wrapper.SAFE_POST_INC);
    floatBaseOpMap.put(UnOp.POST_DEC, Wrapper.SAFE_POST_DEC);
    floatBaseOpMap.put(UnOp.PRE_INC, Wrapper.SAFE_PRE_INC);
    floatBaseOpMap.put(UnOp.PRE_DEC, Wrapper.SAFE_PRE_DEC);
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
    final Type lhsType = typer.lookupType(binaryExpr.getLhs()).getWithoutQualifiers();
    final Type rhsType = typer.lookupType(binaryExpr.getRhs()).getWithoutQualifiers();
    // We look for the operation and cast the two operand to their real type
    if (lhsType instanceof BasicType && rhsType instanceof BasicType) {
      BasicType leftType = (BasicType) lhsType;
      BasicType rightType = (BasicType) rhsType;

      // We check if the operation needs a wrapper depending on the type of the left and right
      // operand
      Wrapper wrapperOp = null;
      if ((leftType.getElementType().equals(BasicType.UINT)
          || leftType.getElementType().equals(BasicType.INT)) && intBasedOpMap.containsKey(op)) {
        wrapperOp = intBasedOpMap.get(op);
      } else if (leftType.getElementType().equals(BasicType.FLOAT)
          && floatBaseOpMap.containsKey(op)) {
        wrapperOp = floatBaseOpMap.get(op);
      }

      // If the operation needs some rewriting we do it
      if (wrapperOp != null) {

        // Register wrapper for generation with the correct operand type
        programState.registerWrapper(wrapperOp, leftType,
            rightType);

        // Build the replacement Expr and exchange the child on the AST using the reference in
        // the map
        Expr replacementExpr = new FunctionCallExpr(wrapperOp.name, binaryExpr.getLhs(),
            binaryExpr.getRhs());
        parentMap.get(binaryExpr).replaceChild(binaryExpr, replacementExpr);

        // Visit the left and right operand as "children" of the new replacement Expr and return
        // early
        visitChildFromParent(binaryExpr.getLhs(), replacementExpr);
        visitChildFromParent(binaryExpr.getRhs(), replacementExpr);
        return;

        // We apply the default wrapper on the result for FLOAT based value (all side-effecting
        // assign supported are already dealt with)
      } else if (leftType.getElementType().equals(BasicType.FLOAT) && !op.isSideEffecting()) {
        final Type rType = typer.lookupType(binaryExpr).getWithoutQualifiers();
        if (rType instanceof BasicType) {
          final BasicType resultType = (BasicType) rType;
          if (resultType.getElementType().equals(BasicType.FLOAT)) {
            programState.registerWrapper(Wrapper.SAFE_FLOAT_RESULT, resultType, null);
            // Build the replacement Expr and exchange the child on the AST using the reference in
            // the map
            Expr replacementExpr = new FunctionCallExpr(Wrapper.SAFE_FLOAT_RESULT.name, binaryExpr);
            parentMap.get(binaryExpr).replaceChild(binaryExpr, replacementExpr);
            // Rebuild the map for the current binaryExpr so that its parent is the newly declared
            // expression
            parentMap.put(replacementExpr, parentMap.get(binaryExpr));
            parentMap.replace(binaryExpr, replacementExpr);
          }
        }
      }
    }
    super.visitBinaryExpr(binaryExpr);
  }

  // Expr are necessary on unary expr only in the case of float-based operands
  @Override
  public void visitUnaryExpr(UnaryExpr unaryExpr) {
    Type type = typer.lookupType(unaryExpr.getExpr()).getWithoutQualifiers();
    UnOp op = unaryExpr.getOp();
    if (type instanceof BasicType) {
      BasicType operandType = (BasicType) type;
      if (operandType.getElementType().equals(BasicType.FLOAT) && floatBaseOpMap.containsKey(op)) {
        Wrapper wrapperOp = floatBaseOpMap.get(op);
        // Register the side-effecting wrapper
        programState.registerWrapper(wrapperOp, operandType, null);
        // Build the replacement Expr and exchange the child on the AST using the reference in
        // the map
        Expr replacementExpr = new FunctionCallExpr(wrapperOp.name, unaryExpr.getExpr());
        parentMap.get(unaryExpr).replaceChild(unaryExpr, replacementExpr);

        // Visit the child considering using the replacement expr as parent and return early
        visitChildFromParent(unaryExpr.getExpr(), replacementExpr);
        return;
      }
    }
    super.visitUnaryExpr(unaryExpr);
  }
}

