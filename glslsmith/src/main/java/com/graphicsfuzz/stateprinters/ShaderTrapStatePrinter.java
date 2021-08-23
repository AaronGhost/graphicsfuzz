package com.graphicsfuzz.stateprinters;

import com.graphicsfuzz.Buffer;
import com.graphicsfuzz.ProgramState;
import com.graphicsfuzz.common.ast.decl.ArrayInfo;
import com.graphicsfuzz.common.ast.expr.IntConstantExpr;
import com.graphicsfuzz.common.ast.type.ArrayType;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.BindingLayoutQualifier;
import com.graphicsfuzz.common.ast.type.LayoutQualifierSequence;
import com.graphicsfuzz.common.ast.type.QualifiedType;
import com.graphicsfuzz.common.ast.type.Std430LayoutQualifier;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.common.ast.type.TypeQualifier;
import com.graphicsfuzz.common.util.ShaderKind;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO rewrite without instanceof using UnifiedProxy
public class ShaderTrapStatePrinter implements StatePrinter {

  @Override
  public String changeShaderFromHarness(String harnessText, String newGlslCode) {
    String oldCode = getShaderCodeFromHarness(harnessText);
    return harnessText.replace(oldCode, newGlslCode + "\n");
  }

  @Override
  public String getShaderCodeFromHarness(String fileContent) {
    Pattern pattern = Pattern.compile("KIND (COMPUTE|FRAG|VERT)\n(.*?)END", Pattern.DOTALL);
    Matcher matcher = pattern.matcher(fileContent);
    if (matcher.find()) {
      return matcher.group(2);
    } else {
      throw new UnsupportedOperationException("Given file is not a shadertrap code");
    }
  }

  @Override
  public String printWrapper(ProgramState programState) {
    if (programState.getShaderKind() == ShaderKind.COMPUTE) {
      StringBuilder shaderTrapPrefix = new StringBuilder();
      String version = parseVersion(programState.getShadingLanguageVersion());
      shaderTrapPrefix.append(version).append("\n");
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

  @Override
  public List<Buffer> getBuffersFromHarness(String fileContent) {
    Pattern pattern = Pattern.compile("CREATE_BUFFER ([^ ]+) SIZE_BYTES ([0-9]+) "
        + "INIT_VALUES (.*)");
    Pattern typePattern = Pattern.compile("(int|uint|float)( (-?)[0-9]+(\\.[0-9]+)?)+");
    Matcher matcher = pattern.matcher(fileContent);
    List<Buffer> buffers = new ArrayList<>();
    int membersBinding = 0;
    // Match the next buffer
    while (matcher.find()) {
      // Find the name and its complete Size
      String bufferName = matcher.group(1);
      int fullSize = Integer.parseInt(matcher.group(2)) / 4;

      // Find its binding point using the name
      Pattern bindingPattern =
          Pattern.compile("BIND_SHADER_STORAGE_BUFFER BUFFER " + Pattern.quote(bufferName)
              + " BINDING ([0-9]+)");
      Matcher bindingMatcher = bindingPattern.matcher(fileContent);
      if (bindingMatcher.find()) {
        int bufferBinding = Integer.parseInt(bindingMatcher.group(1));
        List<Integer> memberValues = new ArrayList<>();
        List<String> memberNames = new ArrayList<>();
        List<Type> memberTypes = new ArrayList<>();
        Matcher innerMatcher = typePattern.matcher(matcher.group(3));
        while (innerMatcher.find()) {

          // Match internal values to determine current inner type
          BasicType type;
          switch (innerMatcher.group(1)) {
            case "int":
              type = BasicType.INT;
              break;
            case "uint":
              type = BasicType.UINT;
              break;
            default:
              type = BasicType.FLOAT;
          }
          int innerSize = innerMatcher.group().split(" ").length - 1;
          fullSize -= innerSize;

          // Build an array type and fold it if necessary
          if (innerSize != 1) {
            ArrayInfo arrayInfo = new ArrayInfo(Collections.singletonList(Optional.of(
                new IntConstantExpr(String.valueOf(innerSize)))));
            arrayInfo.setConstantSizeExpr(0, innerSize);
            memberTypes.add(new ArrayType(type, arrayInfo));
          } else {
            memberTypes.add(type);
          }

          // Add a new Member name
          memberNames.add("ext_" + membersBinding);
          //TODO parse back the correct values
          memberValues.add(0);
          membersBinding++;
        }

        assert  fullSize == 0;
        buffers.add(new Buffer(bufferName,
            new LayoutQualifierSequence(Arrays.asList(new Std430LayoutQualifier(),
                new BindingLayoutQualifier(bufferBinding))), memberValues,
            Collections.singletonList(TypeQualifier.BUFFER),
            memberNames, memberTypes, "", true, bufferBinding));
      } else {
        throw new RuntimeException("Buffer " + bufferName + " is not bound to the shader");
      }
    }
    return buffers;
  }


  public String parseVersion(String shaderVersion) {
    String[] versioning = shaderVersion.split(" ");
    String versionNumber = String.format("%.1f", (Integer.parseInt(versioning[0]) / 100.));
    if (versioning.length == 2) {
      return "GLES " + versionNumber;
    } else {
      return "GL " + versionNumber;
    }
  }

  private String correctlyPrintNumber(Number value) {
    if (value instanceof Float) {
      return String.format("%.1f", value);
    }
    return String.valueOf(value);
  }

  public String printBufferWrapper(Buffer buffer) {
    List<? extends Number> values = buffer.getValues();
    StringBuilder createInstruction = new StringBuilder("CREATE_BUFFER " + buffer.getName() + " "
        + "SIZE_BYTES "
        + 4 * buffer.getLength() + " INIT_VALUES ");
    int offset = 0;
    for (Type type : buffer.getMemberTypes()) {
      if (type instanceof QualifiedType) {
        type = ((QualifiedType) type).getTargetType();
      }
      if (type instanceof ArrayType) {
        ArrayType arrayType = (ArrayType) type;
        createInstruction.append(arrayType.getBaseType().getText()).append(" ");
        for (int j = 0; j < arrayType.getArrayInfo().getDimensionality(); j++) {
          for (int i = 0; i < arrayType.getArrayInfo().getConstantSize(j); i++) {
            createInstruction.append(correctlyPrintNumber(values.get(offset))).append(" ");
            offset++;
          }
        }
      } else if (type instanceof BasicType) {
        createInstruction.append(type).append(" ");
        createInstruction.append(correctlyPrintNumber(values.get(offset))).append(" ");
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
        if (type instanceof QualifiedType) {
          type = ((QualifiedType) type).getTargetType();
        }
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


