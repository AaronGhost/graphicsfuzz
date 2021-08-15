package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.ProgramState;
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
    List<ImmutableTriple<Wrapper, BasicType, BasicType>> necessaryWrappers =
        new ArrayList<>();

    // Checks the wrappers that are necessary to add (prevent from adding twice the same wrapper)
    for (ImmutableTriple<Wrapper, BasicType, BasicType> wrapperFunction :
        programState.getWrappers()) {
      FunctionPrototype wrapperPrototype = WrapperGenerator.generateDeclaration(
          wrapperFunction.left, wrapperFunction.middle, wrapperFunction.right);
      if (tu.getTopLevelDeclarations().stream()
          .noneMatch(t -> t instanceof FunctionPrototype
              && (((FunctionPrototype) t).matches(wrapperPrototype)))) {
        necessaryWrappers.add(wrapperFunction);
      }
    }

    // Add the necessary wrappers prototypes in any order
    for (ImmutableTriple<Wrapper, BasicType, BasicType> wrapperFunction :
        necessaryWrappers) {
      tu.addDeclaration(WrapperGenerator.generateDeclaration(wrapperFunction.left,
          wrapperFunction.middle, wrapperFunction.right));
    }

    //Add the necessary wrapper bodies in any order before the main function
    for (ImmutableTriple<Wrapper, BasicType, BasicType> wrapperFunction :
        necessaryWrappers) {
      tu.addDeclarationBefore(wrapperFunction.left.generator.apply(wrapperFunction.middle,
          wrapperFunction.right), tu.getMainFunction());
    }

    state.programInitialization(tu);
    return state;
  }
}
