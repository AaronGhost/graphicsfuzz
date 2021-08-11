package com.graphicsfuzz.random;

import com.graphicsfuzz.common.ast.expr.BinOp;
import com.graphicsfuzz.common.ast.expr.UnOp;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.QualifiedType;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.scope.UnifiedTypeInterface;
import com.graphicsfuzz.scope.UnifiedTypeProxy;
import java.util.ArrayList;

public class MokeRandomTypeGenerator implements IRandomType {

  private BasicType rightTypeForOp;
  private BasicType typeFromReturn;
  private BinOp boolBinaryOp = BinOp.EQ;
  private BinOp intAssignOp = BinOp.ASSIGN;
  private BinOp floatBinaryOp = BinOp.ADD;
  private BinOp floatAssignOp = BinOp.ASSIGN;
  private UnOp floatUnaryOp = UnOp.MINUS;
  private BinOp noAssignIntBinaryOp = BinOp.ADD;
  private BinOp intVectBinaryOp = BinOp.ADD;
  private BinOp assignIntBinaryOp = BinOp.ASSIGN;
  private UnOp noDecrIntUnaryOp = UnOp.MINUS;
  private UnOp incrIntUnaryOp = UnOp.POST_DEC;
  private BasicType randomBaseInt = BasicType.INT;
  private BasicType randomType = BasicType.BOOL;
  private BinOp comparisonOp = BinOp.GT;
  private BasicType randomIntType = randomBaseInt;
  private UnifiedTypeInterface randomNewType = new UnifiedTypeProxy(BasicType.INT);
  private BasicType targetType = BasicType.INT;

  public void setRightTypeForOp(BasicType rightTypeForOp) {
    this.rightTypeForOp = rightTypeForOp;
  }

  public void setTypeFromReturn(BasicType typeFromReturn) {
    this.typeFromReturn = typeFromReturn;
  }

  public void setBoolBinaryOp(BinOp boolBinaryOp) {
    this.boolBinaryOp = boolBinaryOp;
  }

  public void setIntAssignOp(BinOp intAssignOp) {
    this.intAssignOp = intAssignOp;
  }

  public void setNoAssignIntBinaryOp(BinOp noAssignIntBinaryOp) {
    this.noAssignIntBinaryOp = noAssignIntBinaryOp;
  }

  public void setAssignIntBinaryOp(BinOp assignIntBinaryOp) {
    this.assignIntBinaryOp = assignIntBinaryOp;
  }

  public void setNoDecrIntUnaryOp(UnOp noDecrIntUnaryOp) {
    this.noDecrIntUnaryOp = noDecrIntUnaryOp;
  }

  public void setIncrIntUnaryOp(UnOp incrIntUnaryOp) {
    this.incrIntUnaryOp = incrIntUnaryOp;
  }

  public void setRandomBaseInt(BasicType randomBaseInt) {
    this.randomBaseInt = randomBaseInt;
  }

  public void setRandomType(BasicType randomType) {
    this.randomType = randomType;
  }

  public void setComparisonOp(BinOp comparisonOp) {
    this.comparisonOp = comparisonOp;
  }

  public void setRandomIntType(BasicType randomIntType) {
    this.randomIntType = randomIntType;
  }

  public void setRandomNewType(UnifiedTypeInterface randomNewType) {
    this.randomNewType = randomNewType;
  }

  public void setTargetType(BasicType targetType) {
    this.targetType = targetType;
  }

  public void setIntVectBinaryOp(BinOp intVectBinaryOp) {
    this.intVectBinaryOp = intVectBinaryOp;
  }

  public void setFloatBinaryOp(BinOp binaryOp) {
    this.floatBinaryOp = binaryOp;
  }

  @Override
  public BasicType getAvailableTypeFromOp(BasicType currentType, BinOp op, BasicType returnType) {
    if (rightTypeForOp != null) {
      return rightTypeForOp;
    }
    return currentType;
  }

  @Override
  public BasicType getAvailableTypeFromReturnType(BasicType returnType) {
    if (typeFromReturn != null) {
      return typeFromReturn;
    }
    return returnType;
  }

  @Override
  public BinOp getRandomBaseBoolBinaryOp() {
    return boolBinaryOp;
  }

  @Override
  public BinOp getRandomBaseFloatAssignOp() {
    return floatAssignOp;
  }

  @Override
  public BinOp getRandomBaseIntAssignOp() {
    return intAssignOp;
  }

  @Override
  public BinOp getRandomBaseFloatBinaryOp(BasicType returnType, boolean canGenerateAssign) {
    return floatBinaryOp;
  }

  @Override
  public BinOp getRandomBaseIntBinaryOp(BasicType returnType,
                                        BasicType leftType, boolean canGenerateAssign) {
    if (canGenerateAssign) {
      return assignIntBinaryOp;
    }
    return noAssignIntBinaryOp;
  }

  @Override
  public UnOp getRandomBaseFloatUnaryOp(boolean canGenerateIncrDec) {
    return floatUnaryOp;
  }

  @Override
  public UnOp getRandomBaseIntUnaryOp(boolean canGenerateIncrDec) {
    if (canGenerateIncrDec) {
      return incrIntUnaryOp;
    }
    return noDecrIntUnaryOp;
  }

  @Override
  public BinOp getRandomBaseIntVectBinaryOp() {
    return intVectBinaryOp;
  }

  @Override
  public UnifiedTypeInterface getBufferElementType(boolean noReadOnly) {
    return new UnifiedTypeProxy(new QualifiedType(getRandomBaseType(true), new ArrayList<>()));
  }

  @Override
  public BasicType getRandomBaseType(boolean restrictToInteger) {
    if (restrictToInteger) {
      return randomBaseInt;
    }
    return randomType;
  }

  @Override
  public BinOp getRandomComparisonOp() {
    return comparisonOp;
  }

  @Override
  public Type getRandomIntType() {
    return randomIntType;
  }

  @Override
  public UnifiedTypeInterface getRandomArrayOrBaseType(boolean restrictToInteger) {
    return randomNewType;
  }

  @Override
  public UnifiedTypeInterface getRandomQualifiedProxyType() {
    return new UnifiedTypeProxy(new QualifiedType(getRandomArrayOrBaseType(false).getRealType(),
        new ArrayList<>()));
  }

  @Override
  public BasicType getRandomScalarInteger() {
    return randomIntType;
  }

  @Override
  public BasicType getRandomTargetType(BasicType baseType) {
    return targetType;
  }
}
