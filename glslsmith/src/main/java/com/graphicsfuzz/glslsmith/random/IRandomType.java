package com.graphicsfuzz.glslsmith.random;

import com.graphicsfuzz.common.ast.expr.BinOp;
import com.graphicsfuzz.common.ast.expr.UnOp;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.glslsmith.scope.UnifiedTypeInterface;

public interface IRandomType {
  BasicType getAvailableTypeFromOp(BasicType currentType, BinOp op, BasicType returnType);

  BasicType getAvailableTypeFromReturnType(BasicType returnType);

  BinOp getRandomBaseBoolBinaryOp();

  BinOp getRandomBaseFloatAssignOp();

  BinOp getRandomBaseIntAssignOp();

  BinOp getRandomBaseFloatBinaryOp(BasicType returnType, boolean canGenerateAssign);

  BinOp getRandomBaseIntBinaryOp(BasicType returnType, BasicType leftType,
                                 boolean canGenerateAssign);

  UnOp getRandomBaseFloatUnaryOp(boolean canGenerateIncrDec);

  UnOp getRandomBaseIntUnaryOp(boolean canGenerateIncrDec);

  BinOp getRandomBaseIntVectBinaryOp();

  default BasicType getRandomBaseType() {
    return getRandomBaseType(true);
  }

  BasicType getRandomBaseType(boolean restrictToInteger);

  UnifiedTypeInterface getBufferElementType(boolean noReadonly, BasicType basicType);

  BinOp getRandomComparisonOp();

  Type getRandomIntType();

  BinOp getRandomForOp(BasicType initType);

  UnifiedTypeInterface getRandomArrayOrBaseType(boolean restrictToInteger);

  UnifiedTypeInterface getRandomArrayOrBaseType(BasicType baseType);

  UnifiedTypeInterface getRandomQualifiedProxyType();

  BasicType getRandomScalarInteger();

  BasicType getRandomTargetType(BasicType baseType);
}
