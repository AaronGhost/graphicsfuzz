package com.graphicsfuzz.stateprinters;

import com.graphicsfuzz.Buffer;
import com.graphicsfuzz.ProgramState;
import com.graphicsfuzz.common.ast.type.ArrayType;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.common.util.ShaderKind;
import java.util.List;

//TODO check support for vector types withing shadertrap
public class ShaderTrapStatePrinter implements StatePrinter {

  @Override
  public String printWrapper(ProgramState programState) {
    if (programState.getShaderKind() == ShaderKind.COMPUTE) {
      StringBuilder shaderTrapPrefix = new StringBuilder();
      //TODO pick correct GLES version
      shaderTrapPrefix.append("GLES 3.1\n");
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
        for (int j = 0; j < arrayType.getArrayInfo().getDimensionality(); j++) {
          for (int i = 0; i < arrayType.getArrayInfo().getConstantSize(j); i++) {
            createInstruction.append(values.get(offset)).append(" ");
            offset++;
          }
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
    return createInstruction.toString().trim() + "\n" + bindingInstruction;
  }

  public String printDumpBuffer(Buffer buffer) {
    if (buffer.isInput()) {
      return "";
    } else {
      StringBuilder dumpInstruction =
          new StringBuilder("DUMP_BUFFER_TEXT BUFFER " + buffer.getName()
          + " FILE \"" + buffer.getName() + ".txt\" FORMAT \"" + buffer.getName() + " \" ");
      for (Type type : buffer.getMemberTypes()) {
        if (type instanceof ArrayType) {
          ArrayType arrayType = (ArrayType) type;
          for (int j = 0; j < arrayType.getArrayInfo().getDimensionality(); j++) {
            dumpInstruction.append(arrayType.getBaseType().getText()).append(" ");
            if (arrayType.getBaseType() instanceof BasicType) {
              BasicType baseType = (BasicType) arrayType.getBaseType();
              dumpInstruction.append(
                  arrayType.getArrayInfo().getConstantSize(j) * baseType.getNumElements())
                  .append(" ");
            }
          }
        } else if (type instanceof BasicType) {
          BasicType basicType = (BasicType) type;
          dumpInstruction.append(basicType.getElementType()).append(" ");
          dumpInstruction.append(basicType.getNumElements()).append(" ");
        }
        dumpInstruction.append("\" \" ");
      }
      String dumpString = dumpInstruction.toString();
      return dumpString.substring(0, dumpString.length() - 5) + "\n";
    }
  }

}


