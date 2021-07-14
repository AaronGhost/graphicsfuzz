package com.graphicsfuzz.random;

import com.graphicsfuzz.common.ast.expr.BinOp;
import com.graphicsfuzz.common.ast.expr.UnOp;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.scope.UnifiedTypeInterface;

public interface IRandomType {
  BasicType getAvailableTypeFromOp(BasicType currentType, BinOp op, BasicType returnType);

  BasicType getAvailableTypeFromReturnType(BasicType returnType);

  BinOp getRandomBaseBoolBinaryOp();

  BinOp getRandomBaseIntAssignOp();

  BinOp getRandomBaseIntBinaryOp(boolean canGenerateAssign);

  UnOp getRandomBaseIntUnaryOp(boolean canGenerateIncrDec);

  BinOp getRandomBaseIntVectBinaryOp();

  default BasicType getRandomBaseType() {
    return getRandomBaseType(true);
  }

  BasicType getRandomBaseType(boolean restrictToInteger);

  UnifiedTypeInterface getBufferElementType(boolean noReadOnly);

  BinOp getRandomComparisonOp();

  Type getRandomIntType();

  UnifiedTypeInterface getRandomArrayOrBaseType(boolean restrictToInteger);

  UnifiedTypeInterface getRandomQualifiedProxyType();

  BasicType getRandomTargetType(BasicType baseType);
}
