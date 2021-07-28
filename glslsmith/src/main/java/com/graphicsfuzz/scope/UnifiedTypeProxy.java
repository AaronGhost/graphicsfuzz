package com.graphicsfuzz.scope;

import com.graphicsfuzz.common.ast.expr.Expr;
import com.graphicsfuzz.common.ast.type.ArrayType;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.QualifiedType;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.common.ast.type.TypeQualifier;
import com.graphicsfuzz.common.ast.type.VoidType;
import com.graphicsfuzz.common.ast.visitors.IAstVisitor;
import com.graphicsfuzz.common.typing.Scope;
import java.util.List;

public class UnifiedTypeProxy extends Type implements UnifiedTypeInterface {

  private final Type realType;
  private final UnifiedTypeInterface childType;
  private final BasicType baseType;
  private final int baseTypeSize;
  private final int currentTypeSize;
  private final boolean isArray;
  private final boolean isOut;
  private boolean isReadOnly;
  private boolean isConstOnly;
  private boolean isWriteOnly;
  private boolean isCoherent;
  private boolean isVoid;

  public UnifiedTypeProxy(QualifiedType realType) {
    this.realType = realType;
    Type withoutQualifierType = realType.getWithoutQualifiers();
    if (withoutQualifierType instanceof BasicType) {
      this.childType = new UnifiedTypeProxy((BasicType) withoutQualifierType);
    } else if (withoutQualifierType instanceof ArrayType) {
      this.childType = new UnifiedTypeProxy((ArrayType) withoutQualifierType);
    } else {
      throw new RuntimeException("Currently unsupported type sent to the proxy");
    }
    this.baseType = childType.getBaseType();
    this.currentTypeSize = childType.getCurrentTypeSize();
    this.baseTypeSize = childType.getBaseTypeSize();
    this.isArray = childType.isArray();
    List<TypeQualifier> qualifierList = realType.getQualifiers();
    this.isReadOnly = qualifierList.contains(TypeQualifier.READONLY)
        || qualifierList.contains(TypeQualifier.CONST);
    this.isConstOnly = qualifierList.contains(TypeQualifier.CONST);
    this.isWriteOnly = qualifierList.contains(TypeQualifier.WRITEONLY);
    this.isCoherent = qualifierList.contains(TypeQualifier.COHERENT);
    this.isOut =
        qualifierList.contains(TypeQualifier.OUT_PARAM)
            || qualifierList.contains(TypeQualifier.INOUT_PARAM);
    this.isVoid = false;
  }

  public UnifiedTypeProxy(BasicType realType) {
    this.realType = realType;
    this.childType = null;
    this.baseType = realType;
    this.currentTypeSize = 1;
    this.baseTypeSize = 1;
    this.isArray = false;
    this.isReadOnly = false;
    this.isWriteOnly = false;
    this.isCoherent = false;
    this.isVoid = false;
    this.isOut = false;
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
    if (realType.getArrayInfo().hasConstantSize(0)) {
      currentTypeSize = realType.getArrayInfo().getConstantSize(0);
      baseTypeSize = childType.getBaseTypeSize() * realType.getArrayInfo().getConstantSize(0);
    } else {
      baseTypeSize = 0;
      currentTypeSize = 0;
    }
    isArray = true;
    this.isWriteOnly = false;
    this.isReadOnly = false;
    this.isCoherent = false;
    this.isVoid = false;
    this.isOut = false;
  }

  public UnifiedTypeProxy(VoidType voidType) {
    this.realType = voidType;
    this.childType = null;
    this.baseType = null;
    this.baseTypeSize = 0;
    this.currentTypeSize = 0;
    this.isArray = false;
    this.isWriteOnly = false;
    this.isReadOnly = false;
    this.isConstOnly = false;
    this.isCoherent = false;
    this.isVoid = false;
    this.isOut = false;
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

  @Override
  public boolean isReadOnly() {
    return isReadOnly;
  }

  @Override
  public boolean isCoherent() {
    return isCoherent;
  }

  @Override
  public boolean isConstOnly() {
    return isConstOnly;
  }

  @Override
  public boolean isWriteOnly() {
    return isWriteOnly;
  }

  @Override
  public boolean isVoid() {
    return isVoid;
  }

  @Override
  public boolean isOut() {
    return isOut;
  }

  @Override
  public void setReadOnly(boolean readOnly) {
    this.isReadOnly = readOnly;
  }

  @Override
  public void setWriteOnly(boolean writeOnly) {
    this.isWriteOnly = writeOnly;
  }

  @Override
  public void setCoherent(boolean coherent) {
    this.isCoherent = coherent;
  }

  @Override
  public String toString() {
    return "proxy_" + realType.toString();
  }

  @Override
  public int hashCode() {
    return realType.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Type) {
      if (obj instanceof UnifiedTypeProxy) {
        return ((UnifiedTypeProxy) obj).realType.equals(this.realType);
      }
      if (obj instanceof BasicType || obj instanceof ArrayType || obj instanceof QualifiedType) {
        return (obj).equals(this.realType);
      }
    }
    return false;
  }
}
