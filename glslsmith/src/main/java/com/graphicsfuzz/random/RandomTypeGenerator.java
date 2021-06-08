package com.graphicsfuzz.random;

import com.graphicsfuzz.config.ConfigInterface;
import com.graphicsfuzz.config.FuzzerConstants;
import com.graphicsfuzz.common.ast.decl.ArrayInfo;
import com.graphicsfuzz.common.ast.expr.BinOp;
import com.graphicsfuzz.common.ast.expr.IntConstantExpr;
import com.graphicsfuzz.common.ast.expr.UnOp;
import com.graphicsfuzz.common.ast.type.ArrayType;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.common.util.IRandom;
import com.graphicsfuzz.scope.UnifiedTypeInterface;
import com.graphicsfuzz.scope.UnifiedTypeProxy;
import java.util.Collections;
import java.util.Optional;

public class RandomTypeGenerator implements IRandomType {
  private final IRandom randGen;
  private final ConfigInterface configuration;

  public RandomTypeGenerator(IRandom randGen, ConfigInterface configuration) {
    this.randGen = randGen;
    this.configuration = configuration;
  }


  //TODO might need to change the type generation to use former declared constants...
  // or use an overloaded function with given size
  @Override
  public UnifiedTypeInterface getRandomNewType(boolean restrictToInteger) {
    BasicType basicType = this.getRandomBaseType(restrictToInteger);
    if (randGen.nextBoolean()) {
      int arrayLength = randGen.nextPositiveInt(configuration.getMaxArrayLength());
      //Create an empty array info with only the size to generate a size access later
      ArrayInfo arrayInfo =
          new ArrayInfo(Collections.singletonList(Optional.of(
              new IntConstantExpr(String.valueOf(arrayLength)))));
      arrayInfo.setConstantSizeExpr(0, arrayLength);
      return new UnifiedTypeProxy(new ArrayType(basicType, arrayInfo));
    } else {
      return new UnifiedTypeProxy(basicType);
    }
  }

  @Override
  public BasicType getRandomTargetType(BasicType baseType) {
    int numElement = randGen.nextPositiveInt(baseType.getNumElements() + 1);
    return BasicType.makeVectorType(baseType.getElementType(), numElement);
  }

  @Override
  public BinOp getRandomBaseIntAssignOp() {
    switch (randGen.nextInt(11)) {
      case 0:
        return BinOp.MOD_ASSIGN;
      case 1:
        return BinOp.MUL_ASSIGN;
      case 2:
        return BinOp.DIV_ASSIGN;
      case 3:
        return BinOp.ADD_ASSIGN;
      case 4:
        return BinOp.SUB_ASSIGN;
      case 5:
        return BinOp.BAND_ASSIGN;
      case 6:
        return BinOp.BOR_ASSIGN;
      case 7:
        return BinOp.BXOR_ASSIGN;
      case 8:
        return BinOp.SHL_ASSIGN;
      case 9:
        return BinOp.SHR_ASSIGN;
      default:
        return BinOp.ASSIGN;
    }
  }

  @Override
  public BinOp getRandomBaseBoolBinaryOp() {
    switch (randGen.nextInt(5)) {
      case 0:
        return BinOp.LOR;
      case 1:
        return BinOp.LAND;
      case 2:
        return BinOp.LXOR;
      case 3:
        return BinOp.EQ;
      default:
        return BinOp.NE;
    }
  }

  @Override
  public BinOp getRandomComparisonOp() {
    switch (randGen.nextInt(6)) {
      case 0:
        return BinOp.EQ;
      case 1:
        return BinOp.NE;
      case 2:
        return BinOp.GE;
      case 3:
        return BinOp.GT;
      case 4:
        return BinOp.LE;
      default:
        return BinOp.LT;
    }
  }

  @Override
  public BinOp getRandomBaseIntBinaryOp(boolean canGenerateAssign) {
    switch (randGen.nextInt(canGenerateAssign ? 21
        : 10)) {
      case 0:
        return BinOp.MOD;
      case 1:
        return BinOp.MUL;
      case 2:
        return BinOp.DIV;
      case 3:
        return BinOp.ADD;
      case 4:
        return BinOp.SUB;
      case 5:
        return BinOp.BAND;
      case 6:
        return BinOp.BOR;
      case 7:
        return BinOp.BXOR;
      case 8:
        return BinOp.SHL;
      case 9:
        return BinOp.SHR;
      case 10:
        return BinOp.MOD_ASSIGN;
      case 11:
        return BinOp.MUL_ASSIGN;
      case 12:
        return BinOp.DIV_ASSIGN;
      case 13:
        return BinOp.ADD_ASSIGN;
      case 14:
        return BinOp.SUB_ASSIGN;
      case 15:
        return BinOp.BAND_ASSIGN;
      case 16:
        return BinOp.BOR_ASSIGN;
      case 17:
        return BinOp.BXOR_ASSIGN;
      case 18:
        return BinOp.SHL_ASSIGN;
      case 19:
        return BinOp.SHR_ASSIGN;
      default:
        return BinOp.ASSIGN;
    }
  }

  @Override
  public BasicType getAvailableTypeFromOp(BasicType leftOperandType, BinOp op,
                                          BasicType returnType) {
    if (returnType.isVector() && leftOperandType.isVector() && op != BinOp.ASSIGN) {
      return randGen.nextBoolean() ? returnType : returnType.getElementType();
    } else if (returnType.isVector() && !leftOperandType.isVector()) {
      return returnType;
    }
    if (op == BinOp.SHL_ASSIGN || op == BinOp.SHL || op == BinOp.SHR_ASSIGN || op == BinOp.SHR) {
      return getRandomBaseType();
    } else {
      return leftOperandType;
    }
  }

  @Override
  public BasicType getAvailableTypeFromReturnType(BasicType returnType) {
    if (returnType.equals(BasicType.BOOL)) {
      switch (randGen.nextInt(3)) {
        case 0:
          return BasicType.BOOL;
        case 1:
          return BasicType.INT;
        default:
          return BasicType.UINT;
      }
    }
    if (returnType.isVector()) {
      return randGen.nextBoolean() ? returnType : returnType.getElementType();
    }
    return returnType;
  }

  @Override
  public BasicType getRandomBaseType(boolean restrictToInteger) {
    if (restrictToInteger) {
      return randGen.nextInt(2) == 1 ? BasicType.UINT : BasicType.INT;
    }
    switch (randGen.nextInt(12)) {
      case 0:
        return BasicType.INT;
      case 1:
        return BasicType.UINT;
      case 2:
        return BasicType.BOOL;
      case 3:
        return BasicType.IVEC2;
      case 4:
        return BasicType.IVEC3;
      case 5:
        return BasicType.IVEC4;
      case 6:
        return BasicType.UVEC2;
      case 7:
        return BasicType.UVEC3;
      case 8:
        return BasicType.UVEC4;
      case 9:
        return BasicType.BVEC2;
      case 10:
        return BasicType.BVEC3;
      default:
        return BasicType.BVEC4;
    }
  }


  @Override
  public UnOp getRandomBaseIntUnaryOp(boolean canGenerateIncrDec) {
    switch (randGen.nextInt(canGenerateIncrDec ? 7 : 3)) {
      case 0:
        return UnOp.BNEG;
      case 1:
        return UnOp.MINUS;
      case 2:
        return UnOp.PLUS;
      case 3:
        return UnOp.POST_DEC;
      case 4:
        return UnOp.POST_INC;
      case 5:
        return UnOp.PRE_DEC;
      default:
        return UnOp.PRE_INC;
    }
  }

  @Override
  public BinOp getRandomBaseIntVectBinaryOp() {
    switch (randGen.nextInt(8)) {
      case 0:
        return BinOp.MOD;
      case 1:
        return BinOp.MUL;
      case 2:
        return BinOp.DIV;
      case 3:
        return BinOp.ADD;
      case 4:
        return BinOp.SUB;
      case 5:
        return BinOp.BAND;
      case 6:
        return BinOp.BOR;
      default:
        return BinOp.BXOR;
    }
  }

  @Override
  public Type getRandomIntType() {
    int randomTypeIndex = randGen.nextInt(8);
    switch (randomTypeIndex) {
      case 0:
        return BasicType.INT;
      case 1:
        return BasicType.UINT;
      case 2:
        return BasicType.IVEC2;
      case 3:
        return BasicType.IVEC3;
      case 4:
        return BasicType.IVEC4;
      case 5:
        return BasicType.UVEC2;
      case 6:
        return BasicType.UVEC3;
      default:
        return BasicType.UVEC4;
    }
  }

}
