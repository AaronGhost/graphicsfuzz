package com.graphicsfuzz.stateprinters;

import com.graphicsfuzz.Buffer;
import com.graphicsfuzz.ProgramState;
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
      for (Buffer buffer : programState.getBuffers()) {
        shaderTrapPrefix.append(printBufferWrapper(buffer));
      }
      shaderTrapPrefix.append("DECLARE_SHADER shader KIND COMPUTE\n");

      StringBuilder shaderTrapSuffix = new StringBuilder("END\n\n"
          + "COMPILE_SHADER shader_compiled SHADER shader\n"
          + "CREATE_PROGRAM compute_prog SHADERS shader_compiled\n"
          + "RUN_COMPUTE PROGRAM compute_prog NUM_GROUPS 1 1 1\n");
      for (Buffer buffer : programState.getBuffers()) {
        shaderTrapSuffix.append(printDumpBuffer(buffer));
      }
      return shaderTrapPrefix + programState.getShaderCode() + shaderTrapSuffix;
    } else {
      throw new UnsupportedOperationException("Not implemented yet");
    }
  }

  public String printBufferWrapper(Buffer buffer) {
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
    String bindingInstruction =
        "BIND_SHADER_STORAGE_BUFFER BUFFER " + buffer.getName() + " BINDING " + buffer.getBinding()
            + "\n\n";
    return createInstruction.toString().trim() + ("\n") + bindingInstruction;
  }

  public String printDumpBuffer(Buffer buffer) {
    return "";
    /* neutralized as the function does not exist yet
    if (buffer.isInput()) {
      return "";
    } else {
      return "DUMP_BUFFER " + buffer.getName() + "\n";
    }
     */
  }

}


