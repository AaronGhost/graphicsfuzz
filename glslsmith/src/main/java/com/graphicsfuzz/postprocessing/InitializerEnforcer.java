package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.ProgramState;
import com.graphicsfuzz.common.ast.TranslationUnit;
import com.graphicsfuzz.common.ast.decl.Initializer;
import com.graphicsfuzz.common.ast.decl.VariableDeclInfo;
import com.graphicsfuzz.common.ast.decl.VariablesDeclaration;
import com.graphicsfuzz.common.ast.type.ArrayType;
import com.graphicsfuzz.common.typing.ScopeTrackingVisitor;

public class InitializerEnforcer extends ScopeTrackingVisitor implements PostProcessorInterface {

  @Override
  public void visitVariablesDeclaration(VariablesDeclaration variablesDeclaration) {
    super.visitVariablesDeclaration(variablesDeclaration);
    for (VariableDeclInfo variableDeclInfo: variablesDeclaration.getDeclInfos()) {
      if (!variableDeclInfo.hasInitializer()) {
        if (variableDeclInfo.hasArrayInfo()) {
          ArrayType builtType = new ArrayType(variablesDeclaration.getBaseType().getWithoutQualifiers(),
              variableDeclInfo.getArrayInfo());
          if (builtType.hasCanonicalConstant(getCurrentScope())) {
            variableDeclInfo.setInitializer(new Initializer(builtType.getCanonicalConstant(getCurrentScope())));
          }
        } else {
          variableDeclInfo.setInitializer(new Initializer(variablesDeclaration.getBaseType()
              .getWithoutQualifiers().getCanonicalConstant(getCurrentScope())));
        }
      }
    }
  }

  @Override
  public ProgramState process(ProgramState state) {
    TranslationUnit tu = state.getTranslationUnit();
    visitTranslationUnit(tu);
    state.programInitialization(tu);
    return state;
  }
}
