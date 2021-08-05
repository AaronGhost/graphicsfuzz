package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.Buffer;
import com.graphicsfuzz.ProgramState;
import com.graphicsfuzz.common.ast.decl.ArrayInfo;
import com.graphicsfuzz.common.ast.decl.InterfaceBlock;
import com.graphicsfuzz.common.ast.type.ArrayType;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.type.BindingLayoutQualifier;
import com.graphicsfuzz.common.ast.type.LayoutQualifier;
import com.graphicsfuzz.common.ast.type.LayoutQualifierSequence;
import com.graphicsfuzz.common.ast.type.Std430LayoutQualifier;
import com.graphicsfuzz.common.ast.type.Type;
import com.graphicsfuzz.common.ast.visitors.StandardVisitor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//TODO extend with correct type qualifiers once they are available on buffers
public class BufferFormatEnforcer extends StandardVisitor implements PostProcessorInterface {
  private final Map<InterfaceBlock, InterfaceBlock> changingInterfaces = new HashMap<>();

  @Override
  public void visitInterfaceBlock(InterfaceBlock interfaceBlock) {
    super.visitInterfaceBlock(interfaceBlock);
    if (interfaceBlock.isShaderStorageBlock()) {
      boolean needToBeReplaced = false;

      // Check if the buffer exist in the program State (ie it is generated as part of the harness)
      if (programState.hasBuffer(interfaceBlock.getStructName())) {
        Buffer buffer = programState.getBuffer(interfaceBlock.getStructName());

        // Check if the binding and std430 are declared as layout qualifiers
        boolean std430IsPresent = false;
        boolean bindingIsPresent = false;
        List<LayoutQualifier> qualifiers = new ArrayList<>();
        if (interfaceBlock.hasLayoutQualifierSequence()) {
          qualifiers.addAll(interfaceBlock.getLayoutQualifierSequence().getLayoutQualifiers());

          for (LayoutQualifier qualifier : qualifiers) {
            if (qualifier instanceof Std430LayoutQualifier) {
              std430IsPresent = true;
            } else if (qualifier instanceof BindingLayoutQualifier) {
              bindingIsPresent = true;
            }
          }
        }
        if (!std430IsPresent) {
          qualifiers.add(new Std430LayoutQualifier());
          needToBeReplaced = true;
        }
        if (!bindingIsPresent) {
          qualifiers.add(new BindingLayoutQualifier(buffer.getBinding()));
          needToBeReplaced = true;
        }

        List<Type> types = new ArrayList<>(interfaceBlock.getMemberTypes());

        // Ensure that all the components are correctly defined for the interface
        int interfaceBlockSize = interfaceBlock.getMemberTypes().size();
        if (interfaceBlockSize != buffer.getMemberTypes().size()) {
          needToBeReplaced = true;
          types.clear();
          for (String name : buffer.getMemberNames()) {
            // Check if the every member is defined and provide a default one if there is none
            Optional<Type> memberType =  interfaceBlock.getMemberType(name);
            if (memberType.isEmpty()) {
              types.add(buffer.getMemberType(name));
              // Enforce a size on array types (last member)
            } else if (memberType.get()
                == interfaceBlock.getMemberTypes().get(interfaceBlockSize - 1)
                && memberType.get().getWithoutQualifiers() instanceof ArrayType) {

              ArrayType lastMemberType = (ArrayType) memberType.get().getWithoutQualifiers();
              ArrayInfo lastMemberInfo = lastMemberType.getArrayInfo();

              // The buffer might have seen a single value as a BasicType
              Type bufferType = buffer.getMemberType(name).getWithoutQualifiers();
              if (bufferType instanceof BasicType) {
                lastMemberInfo.setConstantSizeExpr(0, 1);
                lastMemberInfo.resetSizeExprToConstant(0);
              } else if (bufferType instanceof ArrayType) {
                lastMemberInfo.setConstantSizeExpr(0,
                    ((ArrayType) bufferType).getArrayInfo().getConstantSize(0));
                lastMemberInfo.resetSizeExprToConstant(0);
              } else {
                throw new RuntimeException("Unsupported type passed as buffer type");
              }
              types.add(lastMemberType);
            } else {
              // We just add the normal type
              types.add(memberType.get());
            }
          }
        }

        if (needToBeReplaced) {
          // Register the interface block as dirty to rewrite it
          changingInterfaces.put(interfaceBlock,
              new InterfaceBlock(Optional.of(new LayoutQualifierSequence(qualifiers)),
                  new ArrayList<>(interfaceBlock.getInterfaceQualifiers()),
                  buffer.getName(),
                  buffer.getMemberNames(),
                  types,
                  Optional.empty()
              ));
        }
      }
    }
  }

  @Override
  public ProgramState process(ProgramState state) {
    programState = state;
    this.visitTranslationUnit(state.getTranslationUnit());
    for (InterfaceBlock block : changingInterfaces.keySet()) {
      state.getTranslationUnit().updateTopLevelDeclaration(changingInterfaces.get(block), block);
    }
    return state;
  }

  private ProgramState programState;
}
