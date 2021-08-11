package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.common.ast.expr.FunctionCallExpr;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.Type;

public class StdWrapperBuilder extends BaseWrapperBuilder {

  @Override
  public void visitFunctionCallExpr(FunctionCallExpr functionCallExpr) {
    String callee = functionCallExpr.getCallee();
    if (callee.equals("bitfieldExtract") || callee.equals("bitfieldInsert") || callee.equals(
        "clamp")) {
      Type valueType = typer.lookupType(functionCallExpr.getArg(0)).getWithoutQualifiers();
      if (valueType instanceof BasicType) {
        if (callee.equals("bitfieldExtract")) {
          functionCallExpr.setCallee("SAFE_BITFIELD_EXTRACT");
          programState.registerWrapper(Wrapper.SAFE_BITFIELD_EXTRACT,
              (BasicType) valueType, null);
        } else if (callee.equals("bitfieldInsert")) {
          functionCallExpr.setCallee("SAFE_BITFIELD_INSERT");
          programState.registerWrapper(Wrapper.SAFE_BITFIELD_INSERT,
              (BasicType) valueType, null);
        } else {
          Type extremumType = typer.lookupType(functionCallExpr.getArg(1)).getWithoutQualifiers();
          if (extremumType instanceof BasicType) {
            functionCallExpr.setCallee("SAFE_CLAMP");
            programState.registerWrapper(Wrapper.SAFE_CLAMP,
                (BasicType) valueType, (BasicType) extremumType);
          } else {
            throw new RuntimeException("Wrong operand type");
          }
        }
      } else {
        throw new RuntimeException("Wrong operand type");
      }
    }
    super.visitFunctionCallExpr(functionCallExpr);
  }
}
