package com.graphicsfuzz.glslsmith.scope;

import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.Type;

public interface UnifiedTypeInterface {

  Type getRealType();

  UnifiedTypeInterface getChildType();

  BasicType getBaseType();

  int getCurrentTypeSize();

  int getBaseTypeSize();

  int getElementSize();

  boolean isArray();

  boolean isReadOnly();

  boolean isConstOnly();

  boolean isCoherent();

  boolean isWriteOnly();

  boolean isVoid();

  boolean isOut();

  void setReadOnly(boolean readOnly);

  void setWriteOnly(boolean writeOnly);

  void setCoherent(boolean coherent);

}
