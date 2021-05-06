package com.graphicsfuzz.scope;

import com.graphicsfuzz.common.ast.expr.Expr;
import com.graphicsfuzz.common.ast.type.ArrayType;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.common.ast.type.TypeQualifier;
import com.graphicsfuzz.common.ast.visitors.IAstVisitor;
import com.graphicsfuzz.common.typing.Scope;

public class UnifiedTypeProxy extends Type implements UnifiedTypeInterface {

  private final Type realType;
  private final UnifiedTypeInterface childType;
  private final BasicType baseType;
  private final int baseTypeSize;
  private final int currentTypeSize;
  private final boolean isArray;

  public UnifiedTypeProxy(BasicType realType) {
    this.realType = realType;
    this.childType = null;
    this.baseType = realType;
    this.currentTypeSize = 1;
    this.baseTypeSize = 1;
    this.isArray = false;
  }

  public UnifiedTypeProxy(ArrayType realType) {
    this.realType = realType;
    if (realType.getBaseType() instanceof BasicType) {
      this.childType = new UnifiedTypeProxy((BasicType) realType.getBaseType());
    } else if (realType.getBaseType() instanceof ArrayType) {
      this.childType = new UnifiedTypeProxy((ArrayType) realType.getBaseType());
    } else {
      throw new RuntimeException("Currently unsupported type sent to the proxy");
    }
    baseType = childType.getBaseType();
    if (realType.getArrayInfo().hasConstantSize()) {
      currentTypeSize = realType.getArrayInfo().getConstantSize();
      baseTypeSize = childType.getBaseTypeSize() * realType.getArrayInfo().getConstantSize();
    } else {
      baseTypeSize = 0;
      currentTypeSize = 0;
    }
    isArray = true;
  }


  @Override
  public void accept(IAstVisitor visitor) {
    realType.accept(visitor);
  }

  @Override
  public Type clone() {
    return realType.clone();
  }

  @Override
  public Type getRealType() {
    return realType;
  }

  @Override
  public UnifiedTypeInterface getChildType() {
    return childType;
  }

  @Override
  public BasicType getBaseType() {
    return baseType;
  }

  @Override
  public int getBaseTypeSize() {
    return baseTypeSize;
  }

  @Override
  public int getCurrentTypeSize() {
    return currentTypeSize;
  }

  @Override
  public int getElementSize() {
    return baseType.getNumElements() * baseTypeSize;
  }


  @Override
  public boolean isArray() {
    return isArray;
  }

  @Override
  public boolean hasCanonicalConstant(Scope scope) {
    return realType.hasCanonicalConstant(scope);
  }

  @Override
  public Expr getCanonicalConstant(Scope scope) {
    return realType.getCanonicalConstant(scope);
  }

  @Override
  public Type getWithoutQualifiers() {
    return realType.getWithoutQualifiers();
  }

  @Override
  public boolean hasQualifier(TypeQualifier qualifier) {
    return realType.hasQualifier(qualifier);
  }
}
