package org.graphicsfuzz;

import com.graphicsfuzz.common.ast.decl.ArrayInfo;
import com.graphicsfuzz.common.ast.decl.InterfaceBlock;
import com.graphicsfuzz.common.ast.expr.IntConstantExpr;
import com.graphicsfuzz.common.ast.type.ArrayType;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.LayoutQualifierSequence;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.common.util.IRandom;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;

public abstract class ShaderGenerator {
  protected IRandom randomGenerator;

  public ShaderGenerator(IRandom randomGenerator) {
    this.randomGenerator = randomGenerator;
  }

  public abstract void generateShader(ProgramState programState);

  protected InterfaceBlock generateInterfaceBlockFromBuffer(Symbol interfaceBlockSymbol) {
    assert (interfaceBlockSymbol instanceof Buffer);
    Buffer buffer = (Buffer) interfaceBlockSymbol;
    return new InterfaceBlock(Optional.ofNullable(buffer.getLayoutQualifiers()),
        buffer.getInterfaceQualifier(),
        buffer.getName(),
        buffer.getMemberNames(),
        buffer.getMemberTypes(),
        Optional.of("")
    );
  }

  protected ImmutablePair<Type, Integer> generateRandomIntType() {
    int randomTypeIndex = randomGenerator.nextInt(4);
    switch (randomTypeIndex) {
      case 0:
        return new ImmutablePair<>(BasicType.INT, 1);
      case 1:
        return new ImmutablePair<>(BasicType.UINT, 1);
      default:
        //TODO limit size to an intelligent value
        int arrayLength = randomGenerator.nextPositiveInt(10);
        //TODO generate with random constant expr
        ArrayInfo arrayInfo = new ArrayInfo(new IntConstantExpr(String.valueOf(arrayLength)));
        arrayInfo.setConstantSizeExpr(arrayLength);
        return new ImmutablePair<>(new ArrayType(randomTypeIndex == 3 ? BasicType.INT :
            BasicType.UINT, arrayInfo), arrayLength);
    }
  }
}
