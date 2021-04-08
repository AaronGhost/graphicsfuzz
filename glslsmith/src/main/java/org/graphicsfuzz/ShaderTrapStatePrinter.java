package org.graphicsfuzz;

import com.graphicsfuzz.common.ast.type.ArrayType;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.common.util.ShaderKind;
import java.util.List;


public class ShaderTrapStatePrinter implements StatePrinter {

  @Override
  public String printWrapper(ProgramState programState) {
    if (programState.getShaderKind() == ShaderKind.COMPUTE) {
      StringBuilder shaderTrapPrefix = new StringBuilder();
      //Generate the buffers declaration and the buffers binding
      for (Symbol buffer : programState.getSymbolByType("Buffer")) {
        shaderTrapPrefix.append(printBufferWrapper(buffer));
      }
      shaderTrapPrefix.append("DECLARE_SHADER shader KIND COMPUTE\n\n");

      StringBuilder shaderTrapSuffix = new StringBuilder("END\n\n"
          + "COMPILE_SHADER shader_compiled SHADER shader\n"
          + "CREATE_PROGRAM compute_prog SHADERS shader_compiled\n"
          + "RUN_COMPUTE PROGRAM compute_prog NUM_GROUPS 1 1 1\n");
      for (Symbol buffer : programState.getSymbolByType("Buffer")) {
        shaderTrapSuffix.append(printDumpBuffer(buffer));
      }
      return shaderTrapPrefix + programState.getShaderCode() + shaderTrapSuffix;
    } else {
      throw new UnsupportedOperationException("Not implemented yet");
    }
  }

  private String printBufferWrapper(Symbol symbol) {
    assert (symbol instanceof Buffer);
    Buffer buffer = (Buffer) symbol;
    List<? extends Number> values = buffer.getValues();
    StringBuilder createInstruction = new StringBuilder("CREATE_BUFFER " + buffer.getName() + " "
        + "SIZE_BYTES "
        + 4 * buffer.getLength() + " INIT_VALUES ");
    int offset = 0;
    for (Type type : buffer.getMemberTypes()) {
      if (type instanceof ArrayType) {
        ArrayType arrayType = (ArrayType) type;
        createInstruction.append(arrayType.getBaseType().getText()).append(" ");
        for (int i = 0; i < arrayType.getArrayInfo().getConstantSize(); i++) {
          createInstruction.append(values.get(offset)).append(" ");
          offset++;
        }
      } else if (type instanceof BasicType) {
        createInstruction.append(type).append(" ");
        createInstruction.append(values.get(offset)).append(" ");
        offset++;
      }
    }
    createInstruction.append("\n");
    String bindingInstruction =
        "BIND_SHADER_STORAGE_BUFFER " + buffer.getName() + " BINDING " + buffer.getBinding()
            + "\n\n";
    return  createInstruction + bindingInstruction;
  }

  private String printDumpBuffer(Symbol symbol) {
    assert (symbol instanceof Buffer);
    Buffer buffer = (Buffer) symbol;
    if (buffer.isInput()) {
      return "";
    } else {
      return "DUMP " + buffer.getName() + "\n";
    }
  }

}


