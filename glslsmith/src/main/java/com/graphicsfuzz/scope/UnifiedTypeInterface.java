package com.graphicsfuzz.scope;

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


}
