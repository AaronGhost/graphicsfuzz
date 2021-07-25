package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.Operation;
import com.graphicsfuzz.ProgramState;
import com.graphicsfuzz.Wrapper;
import com.graphicsfuzz.common.ast.TranslationUnit;
import com.graphicsfuzz.common.ast.decl.FunctionPrototype;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.visitors.StandardVisitor;
import com.graphicsfuzz.common.typing.Typer;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutableTriple;

public abstract class BaseWrapperBuilder extends StandardVisitor implements PostProcessorInterface {
  protected ProgramState programState;
  protected Typer typer;

  @Override
  public ProgramState process(ProgramState state) {
    TranslationUnit tu = state.getTranslationUnit();
    typer = new Typer(tu);
    programState = state;
    //Change all necessary binary operators
    visitTranslationUnit(tu);
    List<ImmutableTriple<Operation, BasicType, BasicType>> necessaryWrappers =
        new ArrayList<>();

    // Checks the wrappers that are necessary to add (prevent from adding twice the same wrapper)
    for (ImmutableTriple<Operation, BasicType, BasicType> wrapperFunction :
        programState.getWrappers()) {
      FunctionPrototype wrapperPrototype = Wrapper.generateDeclaration(wrapperFunction.left,
          wrapperFunction.middle, wrapperFunction.right);
      if (tu.getTopLevelDeclarations().stream()
          .noneMatch(t -> t instanceof FunctionPrototype
              && (((FunctionPrototype) t).matches(wrapperPrototype)))) {
        necessaryWrappers.add(wrapperFunction);
      }
    }

    // We add functions from the start of the file in order to that the first added line will be
    // the last one at the end

    //Add the necessary wrapper body in any order
    for (ImmutableTriple<Operation, BasicType, BasicType> wrapperFunction :
        necessaryWrappers) {
      tu.addDeclaration(wrapperFunction.left.generator.apply(wrapperFunction.middle,
          wrapperFunction.right));
    }

    // Add the necessary wrappers prototypes in any order
    for (ImmutableTriple<Operation, BasicType, BasicType> wrapperFunction :
        necessaryWrappers) {
      tu.addDeclaration(Wrapper.generateDeclaration(wrapperFunction.left,
            wrapperFunction.middle, wrapperFunction.right));
    }

    state.programInitialization(tu, state.getShaderKind());
    return state;
  }
}
