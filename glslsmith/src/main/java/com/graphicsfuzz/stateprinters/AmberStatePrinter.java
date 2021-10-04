package com.graphicsfuzz.stateprinters;

import com.graphicsfuzz.Buffer;
import com.graphicsfuzz.ProgramState;
import com.graphicsfuzz.common.ast.decl.ArrayInfo;
import com.graphicsfuzz.common.ast.expr.IntConstantExpr;
import com.graphicsfuzz.common.ast.type.ArrayType;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.BindingLayoutQualifier;
import com.graphicsfuzz.common.ast.type.LayoutQualifierSequence;
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

// TODO extend to collect a GLSL comment and hide that we are executing SPIR-V
public class AmberStatePrinter implements StatePrinter {

  //TODO rewrite to get spir-v disassembly instead of the GLSL code
  @Override
  public String getShaderCodeFromHarness(String fileContent) {
    Pattern pattern =
        Pattern.compile("SHADER (compute|vertex|fragment) (.*?) GLSL\n(.*?)END\n", Pattern.DOTALL);
    Matcher matcher = pattern.matcher(fileContent);
    if (matcher.find()) {
      return matcher.group(3);
    } else {
      throw  new UnsupportedOperationException("Given file is not a amberscript code");
    }
  }

  @Override
  public String printWrapper(ProgramState programState) {
    if (programState.getShaderKind() == ShaderKind.COMPUTE) {
      // Dump the shader code
      final String amberPrefix = "#!amber\n"
          + "SHADER compute computeShader GLSL\n";
      StringBuilder amberSuffix = new StringBuilder("END\n");

      // Dump the buffer code
      for (Buffer buffer : programState.getBuffers()) {
        amberSuffix.append(printBufferWrapper(buffer));
      }

      // Dump the pipeline code, bind buffers and execute command
      amberSuffix.append("\nPIPELINE compute computePipeline\n"
          + "  ATTACH computeShader\n");
      for (Buffer buffer : programState.getBuffers()) {
        amberSuffix.append("  BIND BUFFER ").append(buffer.getName()).append(" AS storage "
            + "DESCRIPTOR_SET 0 ").append("BINDING ").append(buffer.getBinding()).append("\n");
      }
      amberSuffix.append("END\n\nRUN computePipeline 1 1 1");
      return amberPrefix + programState.getShaderCode() + amberSuffix;
    } else {
      throw new UnsupportedOperationException("Not implemented yet");
    }
  }

  //TODO rewrite with unified proxy type
  public String printBufferWrapper(Buffer buffer) {
    List<Type> types = buffer.getMemberTypes();

    // Get the type of the buffer elements from the first type
    Type firstType = types.get(0).getWithoutQualifiers();
    if (firstType instanceof ArrayType) {
      firstType = ((ArrayType) firstType).getBaseType();
    }
    if (firstType instanceof BasicType) {
      BasicType storageBaseType = (BasicType) firstType;
      StringBuilder bufferBuilder = new StringBuilder("BUFFER " + buffer.getName() + " DATA_TYPE ");

      // Data type of the full buffer
      if (storageBaseType == BasicType.INT) {
        bufferBuilder.append("int32");
      } else if (storageBaseType == BasicType.UINT) {
        bufferBuilder.append("uint32");
      } else {
        assert storageBaseType == BasicType.FLOAT;
        bufferBuilder.append("float");
      }

      // Produce a comment with the size of the inner elements
      bufferBuilder.append(" DATA\n # DATA_SIZE");
      for (Type memberType : buffer.getMemberTypes()) {
        memberType = memberType.getWithoutQualifiers();
        if (memberType instanceof ArrayType) {
          bufferBuilder.append(" ")
              .append(((ArrayType) memberType).getArrayInfo().getConstantSize(0));
        } else if (memberType instanceof BasicType) {
          bufferBuilder.append(" ").append(1);
        }
      }
      bufferBuilder.append("\n");
      // Add all the values of the member to the buffer instruction
      for (Number value : buffer.getValues()) {
        bufferBuilder.append(" ").append(correctlyPrintNumber(value));
      }

      bufferBuilder.append("\nEND\n");
      return bufferBuilder.toString();
      // Verify that all other elements are of the same type
    } else {
      throw new UnsupportedOperationException("Structures are not supported");
    }
  }

  @Override
  public List<Buffer> getBuffersFromHarness(String fileContent) {
    Pattern bufferPattern = Pattern.compile("BUFFER ([^ ]+) DATA_TYPE (int32|uint32|float) DATA\n("
            + ".*?)END\n",
        Pattern.DOTALL);
    Pattern innerPattern = Pattern.compile(" # DATA_SIZE(( [0-9]+)+)\n");
    Matcher matcher = bufferPattern.matcher(fileContent);
    List<Buffer> buffers = new ArrayList<>();
    int membersBinding = 0;

    // Match the next buffer
    while (matcher.find()) {
      // Find the name and the base types of all the members
      String bufferName = matcher.group(1);
      BasicType type;
      switch (matcher.group(2)) {
        case "int32":
          type = BasicType.INT;
          break;
        case "uint32":
          type = BasicType.UINT;
          break;
        default:
          type = BasicType.FLOAT;
          break;
      }

      // Produce inner members
      List<Integer> memberValues = new ArrayList<>();
      List<String> memberNames = new ArrayList<>();
      List<Type> memberTypes = new ArrayList<>();

      // Parse the comment with the inner size
      Matcher innerMatcher = innerPattern.matcher(matcher.group(3));
      if (innerMatcher.find()) {
        // First element is empty (beginning by a space)
        List<String> members = List.of(innerMatcher.group(1).split(" "));
        for (String memberLengthText : members.subList(1, members.size())) {

          // Parse the type of the current member
          int memberLength = Integer.parseInt(memberLengthText);
          if (memberLength == 1) {
            memberTypes.add(type.clone());
          } else {
            ArrayInfo arrayInfo = new ArrayInfo(Collections.singletonList(Optional.of(
                new IntConstantExpr(memberLengthText))));
            arrayInfo.setConstantSizeExpr(0, memberLength);
            memberTypes.add(new ArrayType(type.clone(), arrayInfo));
          }
          // Add a new Member name
          memberNames.add("ext_" + membersBinding);
          // TODO parse back the correct values
          memberValues.add(0);
          membersBinding++;
        }

        // Look for the binding instruction
        Pattern bindingPattern = Pattern.compile("BIND BUFFER " + Pattern.quote(bufferName)
            + " AS storage DESCRIPTOR_SET ([0-9]+) BINDING ([0-9]+)");
        Matcher bindingMatcher = bindingPattern.matcher(fileContent);
        if (bindingMatcher.find()) {
          int bufferBinding = Integer.parseInt(bindingMatcher.group(2));
          buffers.add(new Buffer(bufferName,
              new LayoutQualifierSequence(Arrays.asList(new Std430LayoutQualifier(),
                  new BindingLayoutQualifier(bufferBinding))), memberValues,
              Collections.singletonList(TypeQualifier.BUFFER),
              memberNames, memberTypes, "", true, bufferBinding));
        } else {
          throw new RuntimeException("Buffer " + bufferName + " is not bound to the shader");
        }
      } else {
        throw new RuntimeException(bufferName + " does not contain a comment with the size of the"
            + " buffer elements");
      }
    }
    return buffers;
  }
}
