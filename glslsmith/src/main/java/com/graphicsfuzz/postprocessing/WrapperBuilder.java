package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.ProgramState;
import com.graphicsfuzz.common.ast.IAstNode;
import com.graphicsfuzz.common.ast.TranslationUnit;
import com.graphicsfuzz.common.ast.expr.BinOp;
import com.graphicsfuzz.common.ast.expr.BinaryExpr;
import com.graphicsfuzz.common.ast.expr.FunctionCallExpr;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.common.ast.visitors.StandardVisitor;
import com.graphicsfuzz.common.typing.Typer;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutableTriple;

public class WrapperBuilder extends StandardVisitor implements PostProcessorInterface {
  protected ProgramState programState;
  protected Typer typer;
  protected Map<IAstNode, IAstNode> parentMap = new HashMap<>();

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
    if (lhsType instanceof BasicType && rhsType instanceof BasicType) {
      BasicType leftType = (BasicType) lhsType;
      BasicType rightType = (BasicType) rhsType;
      if (op == BinOp.DIV) {
        programState.registerWrapper(Wrapper.Operation.SAFE_DIV, leftType,
            rightType);
        parentMap.get(binaryExpr).replaceChild(binaryExpr, new FunctionCallExpr(
            "SAFE_DIV", binaryExpr.getLhs(), binaryExpr.getRhs()));
      } else if (op == BinOp.DIV_ASSIGN) {
        programState.registerWrapper(Wrapper.Operation.SAFE_DIV_ASSIGN, leftType,
            rightType);
        parentMap.get(binaryExpr).replaceChild(binaryExpr, new FunctionCallExpr(
            "SAFE_DIV_ASSIGN", binaryExpr.getLhs(), binaryExpr.getRhs()));
      } else if (op == BinOp.SHL) {
        programState.registerWrapper(Wrapper.Operation.SAFE_LSHIFT, leftType,
            rightType);
        parentMap.get(binaryExpr).replaceChild(binaryExpr, new FunctionCallExpr(
            "SAFE_LSHIFT", binaryExpr.getLhs(), binaryExpr.getRhs()));
      } else if (op == BinOp.SHL_ASSIGN) {
        programState.registerWrapper(Wrapper.Operation.SAFE_LSHIFT_ASSIGN, leftType,
            rightType);
        parentMap.get(binaryExpr).replaceChild(binaryExpr, new FunctionCallExpr(
            "SAFE_LSHIFT_ASSIGN", binaryExpr.getLhs(), binaryExpr.getRhs()));
      } else if (op == BinOp.SHR) {
        programState.registerWrapper(Wrapper.Operation.SAFE_RSHIFT, leftType,
            rightType);
        parentMap.get(binaryExpr).replaceChild(binaryExpr, new FunctionCallExpr(
            "SAFE_RSHIFT", binaryExpr.getLhs(), binaryExpr.getRhs()));
      } else if (op == BinOp.SHR_ASSIGN) {
        programState.registerWrapper(Wrapper.Operation.SAFE_RSHIFT_ASSIGN, leftType,
            rightType);
        parentMap.get(binaryExpr).replaceChild(binaryExpr, new FunctionCallExpr(
            "SAFE_RSHIFT_ASSIGN", binaryExpr.getLhs(), binaryExpr.getRhs()));
      } else if (op == BinOp.MOD) {
        programState.registerWrapper(Wrapper.Operation.SAFE_MOD, leftType,
            rightType);
        parentMap.get(binaryExpr).replaceChild(binaryExpr, new FunctionCallExpr(
            "SAFE_MOD", binaryExpr.getLhs(), binaryExpr.getRhs()));
      } else if (op == BinOp.MOD_ASSIGN) {
        programState.registerWrapper(Wrapper.Operation.SAFE_MOD_ASSIGN, leftType,
            rightType);
        parentMap.get(binaryExpr).replaceChild(binaryExpr, new FunctionCallExpr(
            "SAFE_MOD_ASSIGN", binaryExpr.getLhs(), binaryExpr.getRhs()));
      }
    }
    super.visitBinaryExpr(binaryExpr);
  }

  @Override
  public ProgramState process(ProgramState state) {
    TranslationUnit tu = state.getTranslationUnit();
    programState = state;
    //Type the expressions
    typer = new Typer(tu);
    //Change all necessary binary operators
    visitTranslationUnit(tu);
    //Add the necessary wrapper declaration in order
    //Generate safe math wrappers functions
    for (ImmutableTriple<Wrapper.Operation, BasicType, BasicType> wrapperFunction :
        programState.getWrappers()) {
      tu.addDeclarationBefore(wrapperFunction.left.generator.apply(wrapperFunction.middle,
          wrapperFunction.right), tu.getMainFunction());
    }

    //Generate safe math wrappers prototypes
    for (ImmutableTriple<Wrapper.Operation, BasicType, BasicType> wrapperFunction :
        programState.getWrappers()) {
      tu.addDeclaration(Wrapper.generateDeclaration(wrapperFunction.left,
          wrapperFunction.middle, wrapperFunction.right));
    }

    state.programInitialization(tu, state.getShaderKind());
    return state;
  }
}
