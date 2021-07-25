package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.common.ast.decl.Declaration;
import com.graphicsfuzz.common.ast.type.BasicType;
import java.util.function.BiFunction;

public enum Operation {
  SAFE_ABS(Wrapper::generateAbsWrapper, "SAFE_ABS", false),
  SAFE_DIV(Wrapper::generateDivWrapper, "SAFE_DIV", false),
  SAFE_DIV_ASSIGN(Wrapper::generateDivAssignWrapper, "SAFE_DIV_ASSIGN", true),
  SAFE_LSHIFT(Wrapper::generateLShiftWrapper, "SAFE_LSHIFT", false),
  SAFE_LSHIFT_ASSIGN(Wrapper::generateLShiftAssignWrapper, "SAFE_LSHIFT_ASSIGN", true),
  SAFE_RSHIFT(Wrapper::generateRShiftWrapper, "SAFE_RSHIFT", false),
  SAFE_RSHIFT_ASSIGN(Wrapper::generateRShiftAssignWrapper, "SAFE_RSHIFT_ASSIGN", true),
  SAFE_MOD(Wrapper::generateModWrapper, "SAFE_MOD", false),
  SAFE_MOD_ASSIGN(Wrapper::generateModAssignWrapper, "SAFE_MOD_ASSIGN", true),
  SAFE_BITFIELD_INSERT(Wrapper::generateBitInsertWrapper, "SAFE_BITFIELD_INSERT", false),
  SAFE_BITFIELD_EXTRACT(Wrapper::generateBitExtractWrapper, "SAFE_BITFIELD_EXTRACT", false);

  public final BiFunction<BasicType, BasicType, Declaration> generator;
  public final String name;
  public final boolean inoutA;

  Operation(BiFunction<BasicType, BasicType, Declaration> generator, String name,
            boolean inoutA) {
    this.generator = generator;
    this.name = name;
    this.inoutA = inoutA;
  }
}
