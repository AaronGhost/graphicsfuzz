package com.graphicsfuzz.glslsmith.postprocessing;

import com.graphicsfuzz.common.ast.decl.Declaration;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.glslsmith.config.ConfigInterface;
import com.graphicsfuzz.glslsmith.util.TriFunction;

public enum Wrapper {
  SAFE_ABS(WrapperGenerator::generateAbsWrapper, "SAFE_ABS", false, 1, 0),
  SAFE_DIV(WrapperGenerator::generateDivWrapper, "SAFE_DIV", false, 1, 1),
  SAFE_DIV_ASSIGN(WrapperGenerator::generateDivAssignWrapper, "SAFE_DIV_ASSIGN", true, 1, 1),
  SAFE_LSHIFT(WrapperGenerator::generateLShiftWrapper, "SAFE_LSHIFT", false, 1, 1),
  SAFE_LSHIFT_ASSIGN(WrapperGenerator::generateLShiftAssignWrapper, "SAFE_LSHIFT_ASSIGN", true,
      1, 1),
  SAFE_RSHIFT(WrapperGenerator::generateRShiftWrapper, "SAFE_RSHIFT", false, 1, 1),
  SAFE_RSHIFT_ASSIGN(WrapperGenerator::generateRShiftAssignWrapper, "SAFE_RSHIFT_ASSIGN", true,
      1, 1),
  SAFE_MOD(WrapperGenerator::generateModWrapper, "SAFE_MOD", false, 1, 1),
  SAFE_MOD_ASSIGN(WrapperGenerator::generateModAssignWrapper, "SAFE_MOD_ASSIGN", true, 1, 1),

  // STD undefined behaviours
  SAFE_BITFIELD_INSERT(WrapperGenerator::generateBitInsertWrapper, "SAFE_BITFIELD_INSERT", false,
      2, 2),
  SAFE_BITFIELD_EXTRACT(WrapperGenerator::generateBitExtractWrapper, "SAFE_BITFIELD_EXTRACT",
      false, 1, 2),
  SAFE_CLAMP(WrapperGenerator::generateClampWrapper, "SAFE_CLAMP", false, 1, 2),

  // Float undefined behaviours
  SAFE_ADD_ASSIGN(WrapperGenerator::generateAddAssignWrapper, "SAFE_ADD_ASSIGN", true, 1, 1),
  SAFE_SUB_ASSIGN(WrapperGenerator::generateSubAssignWrapper, "SAFE_SUB_ASSIGN", true, 1, 1),
  SAFE_MUL_ASSIGN(WrapperGenerator::generateMulAssignWrapper, "SAFE_MUL_ASSIGN", true, 1, 1),
  SAFE_PRE_INC(WrapperGenerator::generatePreIncWrapper, "SAFE_PRE_INC", true, 1, 0),
  SAFE_PRE_DEC(WrapperGenerator::generatePreDecWrapper, "SAFE_PRE_DEC", true, 1, 0),
  SAFE_POST_INC(WrapperGenerator::generatePostIncWrapper, "SAFE_POST_INC", true, 1, 0),
  SAFE_POST_DEC(WrapperGenerator::generatePostDecWrapper, "SAFE_POST_DEC", true, 1, 0),
  SAFE_FLOAT_RESULT(WrapperGenerator::generateFloatResultWrapper, "SAFE_FLOAT_RESULT", false, 1, 0);

  public final TriFunction<BasicType, BasicType, ConfigInterface.RunType, Declaration> generator;
  public final String name;
  public final boolean inoutA;
  public final int nbA;
  public final int nbB;

  Wrapper(TriFunction<BasicType, BasicType, ConfigInterface.RunType, Declaration> generator,
          String name,
          boolean inoutA, int nbA, int nbB) {
    this.generator = generator;
    this.name = name;
    this.inoutA = inoutA;
    this.nbA = nbA;
    this.nbB = nbB;
  }
}
