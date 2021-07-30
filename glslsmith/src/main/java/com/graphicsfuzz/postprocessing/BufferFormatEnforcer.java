package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.ProgramState;
import com.graphicsfuzz.common.ast.decl.InterfaceBlock;
import com.graphicsfuzz.common.ast.type.BindingLayoutQualifier;
import com.graphicsfuzz.common.ast.type.LayoutQualifier;
import com.graphicsfuzz.common.ast.type.LayoutQualifierSequence;
import com.graphicsfuzz.common.ast.type.Std430LayoutQualifier;
import com.graphicsfuzz.common.ast.visitors.StandardVisitor;
import java.util.ArrayList;
import java.util.List;

public class BufferFormatEnforcer extends StandardVisitor implements PostProcessorInterface {

  @Override
  public void visitInterfaceBlock(InterfaceBlock interfaceBlock) {
    super.visitInterfaceBlock(interfaceBlock);
    if (interfaceBlock.isShaderStorageBlock() && interfaceBlock.hasLayoutQualifierSequence()) {
      LayoutQualifierSequence sequence = interfaceBlock.getLayoutQualifierSequence();
      List<LayoutQualifier> qualifiers = new ArrayList<>(sequence.getLayoutQualifiers());
      boolean std430IsPresent = false;
      boolean bindingIsPresent = false;
      for (LayoutQualifier qualifier : qualifiers) {
        if (qualifier instanceof Std430LayoutQualifier) {
          std430IsPresent = true;
        } else if (qualifier instanceof BindingLayoutQualifier) {
          bindingIsPresent = true;
        }
      }
      if (bindingIsPresent && !std430IsPresent) {
        qualifiers.add(new Std430LayoutQualifier());
      }
      interfaceBlock.setLayoutQualifierSequence(new LayoutQualifierSequence(qualifiers));
    }
  }

  @Override
  public ProgramState process(ProgramState state) {
    this.visitTranslationUnit(state.getTranslationUnit());
    return state;
  }
}
