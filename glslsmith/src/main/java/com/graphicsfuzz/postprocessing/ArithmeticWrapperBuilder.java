package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.common.ast.IAstNode;
import com.graphicsfuzz.common.ast.expr.BinOp;
import com.graphicsfuzz.common.ast.expr.BinaryExpr;
import com.graphicsfuzz.common.ast.expr.Expr;
import com.graphicsfuzz.common.ast.expr.FunctionCallExpr;
import com.graphicsfuzz.common.ast.expr.IntConstantExpr;
import com.graphicsfuzz.common.ast.expr.Op;
import com.graphicsfuzz.common.ast.expr.TypeConstructorExpr;
import com.graphicsfuzz.common.ast.expr.UnOp;
import com.graphicsfuzz.common.ast.expr.UnaryExpr;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.config.ConfigInterface;
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

  protected void wrapFloatResult(Expr expr, BasicType floatType, int id) {
    programState.registerWrapper(Wrapper.SAFE_FLOAT_RESULT, floatType, null);
    // Build the replacement Expr and exchange the child on the AST using the reference in
    // the map
    Expr replacementExpr;
    if (programState.getRunType() == ConfigInterface.RunType.ADDED_ID) {
      replacementExpr = new FunctionCallExpr(Wrapper.SAFE_FLOAT_RESULT.name, expr,
          new IntConstantExpr(String.valueOf(id)));
    } else {
      replacementExpr = new FunctionCallExpr(Wrapper.SAFE_FLOAT_RESULT.name, expr);
    }
    parentMap.get(expr).replaceChild(expr, replacementExpr);
    // Rebuild the map for the current binaryExpr so that its parent is the newly declared
    // expression
    parentMap.put(replacementExpr, parentMap.get(expr));
    parentMap.replace(expr, replacementExpr);
  }

  @Override
  public void visitTypeConstructorExpr(TypeConstructorExpr typeConstructorExpr) {
    final String typeName = typeConstructorExpr.getTypename();
    if (typeName.equals("float") || typeName.equals("vec2")
        || typeName.equals("vec3") || typeName.equals("vec4")) {
      boolean needsFloatWrapper = false;
      for (Expr arg : typeConstructorExpr.getArgs()) {
        final Type argType = typer.lookupType(arg).getWithoutQualifiers();
        if (argType instanceof BasicType && ((BasicType) argType).isInteger()) {
          needsFloatWrapper = true;
          break;
        }
      }
      if (needsFloatWrapper) {
        // collect the id of the wrapper and check if the wrapper is necessary
        int id = programState.wrapperCounterPostIncrement();
        if (programState.getRunType() != ConfigInterface.RunType.REDUCED_WRAPPERS
            || !programState.lookupIds(id)) {
          switch (typeName) {
            case "float":
              wrapFloatResult(typeConstructorExpr, BasicType.FLOAT, id);
              break;
            case "vec2":
              wrapFloatResult(typeConstructorExpr, BasicType.VEC2, id);
              break;
            case "vec3":
              wrapFloatResult(typeConstructorExpr, BasicType.VEC3, id);
              break;
            default:
              wrapFloatResult(typeConstructorExpr, BasicType.VEC4, id);
              break;
          }
        }
      }
    }
    super.visitTypeConstructorExpr(typeConstructorExpr);
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

        // Collect the id of the current wrapper
        int id = programState.wrapperCounterPostIncrement();

        // If we need a wrapper we add it with the necessary id (or not)
        if (programState.getRunType() != ConfigInterface.RunType.REDUCED_WRAPPERS
            || !programState.lookupIds(id)) {
          // Register wrapper for generation with the correct operand type
          programState.registerWrapper(wrapperOp, leftType,
              rightType);

          // Build the replacement Expr and exchange the child on the AST using the reference in
          // the map
          Expr replacementExpr;
          if (programState.getRunType() == ConfigInterface.RunType.ADDED_ID) {
            replacementExpr = new FunctionCallExpr(wrapperOp.name, binaryExpr.getLhs(),
                binaryExpr.getRhs(), new IntConstantExpr(String.valueOf(id)));
          } else {
            replacementExpr = new FunctionCallExpr(wrapperOp.name, binaryExpr.getLhs(),
                binaryExpr.getRhs());
          }
          parentMap.get(binaryExpr).replaceChild(binaryExpr, replacementExpr);

          // Visit the left and right operand as "children" of the new replacement Expr and return
          // early
          visitChildFromParent(binaryExpr.getLhs(), replacementExpr);
          visitChildFromParent(binaryExpr.getRhs(), replacementExpr);
          return;
        }

        // We apply the default wrapper on the result for FLOAT based value (all side-effecting
        // assign supported are already dealt with)
      } else if (leftType.getElementType().equals(BasicType.FLOAT) && !op.isSideEffecting()) {
        final Type rType = typer.lookupType(binaryExpr).getWithoutQualifiers();
        if (rType instanceof BasicType) {
          final BasicType resultType = (BasicType) rType;
          if (resultType.getElementType().equals(BasicType.FLOAT)) {

            // collect the id of the wrapper and check if the wrapper is necessary
            int id = programState.wrapperCounterPostIncrement();
            if (programState.getRunType() != ConfigInterface.RunType.REDUCED_WRAPPERS
                || !programState.lookupIds(id)) {
              wrapFloatResult(binaryExpr, resultType, id);
            }
          }
        }
      }
    }
    super.visitBinaryExpr(binaryExpr);
  }

  @Override
  public void visitFunctionCallExpr(FunctionCallExpr functionCallExpr) {
    super.visitFunctionCallExpr(functionCallExpr);

    Type funCallType = typer.lookupType(functionCallExpr).getWithoutQualifiers();

    if (funCallType instanceof BasicType) {
      BasicType returnType = (BasicType) funCallType;
      if (returnType.getElementType().equals(BasicType.FLOAT)) {
        // collect the id of the wrapper and check if the wrapper is necessary
        int id = programState.wrapperCounterPostIncrement();
        if (!(programState.getRunType() == ConfigInterface.RunType.REDUCED_WRAPPERS)
            || !programState.lookupIds(id)) {
          wrapFloatResult(functionCallExpr, returnType, id);
        }
      }
    }
  }

  // Expr are necessary on unary expr only in the case of float-based operands
  @Override
  public void visitUnaryExpr(UnaryExpr unaryExpr) {
    Type type = typer.lookupType(unaryExpr.getExpr()).getWithoutQualifiers();
    UnOp op = unaryExpr.getOp();
    if (type instanceof BasicType) {
      BasicType operandType = (BasicType) type;
      if (operandType.getElementType().equals(BasicType.FLOAT) && floatBaseOpMap.containsKey(op)) {
        // Collect the id of the current wrapper
        int id = programState.wrapperCounterPostIncrement();

        // If we need a wrapper we add it with the necessary id (or not)
        if (programState.getRunType() != ConfigInterface.RunType.REDUCED_WRAPPERS
            || !programState.lookupIds(id)) {
          Wrapper wrapperOp = floatBaseOpMap.get(op);
          // Register the side-effecting wrapper

          programState.registerWrapper(wrapperOp, operandType, null);
          // Build the replacement Expr and exchange the child on the AST using the reference in
          // the map

          Expr replacementExpr;
          if (programState.getRunType() == ConfigInterface.RunType.ADDED_ID) {
            replacementExpr = new FunctionCallExpr(wrapperOp.name, unaryExpr.getExpr(),
                new IntConstantExpr(String.valueOf(id)));
          } else {
            replacementExpr = new FunctionCallExpr(wrapperOp.name, unaryExpr.getExpr());
          }
          parentMap.get(unaryExpr).replaceChild(unaryExpr, replacementExpr);

          // Visit the child considering using the replacement expr as parent and return early
          visitChildFromParent(unaryExpr.getExpr(), replacementExpr);
          return;
        }
      }
    }
    super.visitUnaryExpr(unaryExpr);
  }
}

