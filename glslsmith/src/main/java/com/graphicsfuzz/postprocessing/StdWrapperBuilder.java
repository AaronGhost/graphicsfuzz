package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.common.ast.expr.Expr;
import com.graphicsfuzz.common.ast.expr.FunctionCallExpr;
import com.graphicsfuzz.common.ast.expr.TypeConstructorExpr;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.Type;
import java.util.HashMap;
import java.util.Map;

public class StdWrapperBuilder extends BaseWrapperBuilder {
  private final Map<String, Wrapper> intBaseFunMap = new HashMap<>();

  public StdWrapperBuilder() {
    super();
    // Add functions with integer based undefined behaviours
    intBaseFunMap.put("bitfieldExtract", Wrapper.SAFE_BITFIELD_EXTRACT);
    intBaseFunMap.put("bitfieldInsert", Wrapper.SAFE_BITFIELD_INSERT);
    intBaseFunMap.put("clamp", Wrapper.SAFE_CLAMP);
  }

  protected BasicType getBasicArg(Expr funcCallExpr, int index) {
    if (funcCallExpr.getNumChildren() > index) {
      Type argType = typer.lookupType(funcCallExpr.getChild(index)).getWithoutQualifiers();
      if (argType instanceof BasicType) {
        return (BasicType) argType;
      } else {
        throw new RuntimeException("Wrong operand type");
      }
    }
    return null;
  }

  @Override
  public void visitTypeConstructorExpr(TypeConstructorExpr typeConstructorExpr) {
    super.visitTypeConstructorExpr(typeConstructorExpr);
    // Conversion from a float to an uint needs an extra abs
    String targetType = typeConstructorExpr.getTypename();
    if (targetType.equals("uint") || targetType.equals("uvec2")
        || targetType.equals("uvec3") || targetType.equals("uvec4")) {
      for (int i = 0; i < typeConstructorExpr.getNumArgs(); i++) {
        if (getBasicArg(typeConstructorExpr, i).getElementType().equals(BasicType.FLOAT)) {
          typeConstructorExpr.setChild(i, new FunctionCallExpr("abs",
              typeConstructorExpr.getArg(i)));
        }
      }
    }
  }

  @Override
  public void visitFunctionCallExpr(FunctionCallExpr functionCallExpr) {
    String callee = functionCallExpr.getCallee();
    Type funCallType = typer.lookupType(functionCallExpr).getWithoutQualifiers();

    // Look up for the return type of the function to decide the correct thing to look at
    if (funCallType instanceof BasicType) {
      BasicType returnType = (BasicType) funCallType;

      // Look up for the return  type of the function to decide the needed expression
      if (returnType.getElementType().equals(BasicType.INT)
          || returnType.getElementType().equals(BasicType.UINT)) {

        // Regular integer to integer functions
        if (intBaseFunMap.containsKey(callee)) {
          Wrapper necessaryWrapper = intBaseFunMap.get(callee);
          functionCallExpr.setCallee(necessaryWrapper.name);
          programState.registerWrapper(necessaryWrapper, getBasicArg(functionCallExpr, 0),
              getBasicArg(functionCallExpr, necessaryWrapper.nbA));
        }
      }
    }
    super.visitFunctionCallExpr(functionCallExpr);
  }
}
