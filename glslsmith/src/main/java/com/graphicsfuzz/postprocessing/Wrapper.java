package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.common.ast.decl.Declaration;
import com.graphicsfuzz.common.ast.type.BasicType;
import java.util.function.BiFunction;

public enum Wrapper {
  SAFE_ABS(WrapperGenerator::generateAbsWrapper, "SAFE_ABS", false),
  SAFE_DIV(WrapperGenerator::generateDivWrapper, "SAFE_DIV", false),
  SAFE_DIV_ASSIGN(WrapperGenerator::generateDivAssignWrapper, "SAFE_DIV_ASSIGN", true),
  SAFE_LSHIFT(WrapperGenerator::generateLShiftWrapper, "SAFE_LSHIFT", false),
  SAFE_LSHIFT_ASSIGN(WrapperGenerator::generateLShiftAssignWrapper, "SAFE_LSHIFT_ASSIGN", true),
  SAFE_RSHIFT(WrapperGenerator::generateRShiftWrapper, "SAFE_RSHIFT", false),
  SAFE_RSHIFT_ASSIGN(WrapperGenerator::generateRShiftAssignWrapper, "SAFE_RSHIFT_ASSIGN", true),
  SAFE_MOD(WrapperGenerator::generateModWrapper, "SAFE_MOD", false),
  SAFE_MOD_ASSIGN(WrapperGenerator::generateModAssignWrapper, "SAFE_MOD_ASSIGN", true),

  // STD undefined behaviours
  SAFE_BITFIELD_INSERT(WrapperGenerator::generateBitInsertWrapper, "SAFE_BITFIELD_INSERT", false),
  SAFE_BITFIELD_EXTRACT(WrapperGenerator::generateBitExtractWrapper, "SAFE_BITFIELD_EXTRACT",
      false),
  SAFE_CLAMP(WrapperGenerator::generateClampWrapper, "SAFE_CLAMP", false),

  // Float undefined behaviours
  SAFE_ADD_ASSIGN(WrapperGenerator::generateAddAssignWrapper, "SAFE_ADD_ASSIGN", true),
  SAFE_SUB_ASSIGN(WrapperGenerator::generateSubAssignWrapper, "SAFE_SUB_ASSIGN", true),
  SAFE_MUL_ASSIGN(WrapperGenerator::generateMulAssignWrapper, "SAFE_MUL_ASSIGN", true),
  SAFE_PRE_INC(WrapperGenerator::generatePreIncWrapper, "SAFE_PRE_INC", true),
  SAFE_PRE_DEC(WrapperGenerator::generatePreDecWrapper, "SAFE_PRE_DEC", true),
  SAFE_POST_INC(WrapperGenerator::generatePostIncWrapper, "SAFE_POST_INC", true),
  SAFE_POST_DEC(WrapperGenerator::generatePostDecWrapper, "SAFE_POST_DEC", true),
  SAFE_FLOAT_RESULT(WrapperGenerator::generateFloatResultWrapper, "SAFE_FLOAT_RESULT", false);

  public final BiFunction<BasicType, BasicType, Declaration> generator;
  public final String name;
  public final boolean inoutA;

  Wrapper(BiFunction<BasicType, BasicType, Declaration> generator, String name,
          boolean inoutA) {
    this.generator = generator;
    this.name = name;
    this.inoutA = inoutA;
  }
}
