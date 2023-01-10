package com.graphicsfuzz.glslsmith.postprocessing;

import com.graphicsfuzz.common.ast.TranslationUnit;
import com.graphicsfuzz.common.ast.decl.FunctionPrototype;
import com.graphicsfuzz.common.ast.decl.InterfaceBlock;
import com.graphicsfuzz.common.ast.type.BasicType;
import com.graphicsfuzz.common.ast.visitors.StandardVisitor;
import com.graphicsfuzz.common.typing.Typer;
import com.graphicsfuzz.glslsmith.Buffer;
import com.graphicsfuzz.glslsmith.ProgramState;
import com.graphicsfuzz.glslsmith.config.ConfigInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
          wrapperFunction.left, wrapperFunction.middle, wrapperFunction.right,
          programState.getRunType());
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
          wrapperFunction.middle, wrapperFunction.right, programState.getRunType()));
    }

    //Add the necessary wrapper bodies in any order before the main function
    for (ImmutableTriple<Wrapper, BasicType, BasicType> wrapperFunction :
        necessaryWrappers) {
      tu.addDeclarationBefore(wrapperFunction.left.generator.apply(wrapperFunction.middle,
          wrapperFunction.right, programState.getRunType()), tu.getMainFunction());
    }

    // Check the Run type and add the extra ids buffer if necessary
    if (programState.getRunType() == ConfigInterface.RunType.ADDED_ID
        && programState.hasIdsBuffer()) {
      Buffer idsBuffer = programState.getIdsBuffer();
      if (tu.hasBufferDeclaration("buffer_ids")) {
        tu.updateTopLevelDeclaration(
            new InterfaceBlock(Optional.ofNullable(idsBuffer.getLayoutQualifiers()),
                idsBuffer.getInterfaceQualifiers(),
                idsBuffer.getName(),
                idsBuffer.getMemberNames(),
                idsBuffer.getMemberTypes(),
                Optional.of("")
            ),
            tu.getBufferDeclaration("buffer_ids"));
      } else {
        tu.addDeclaration(
            new InterfaceBlock(Optional.ofNullable(idsBuffer.getLayoutQualifiers()),
            idsBuffer.getInterfaceQualifiers(),
            idsBuffer.getName(),
            idsBuffer.getMemberNames(),
            idsBuffer.getMemberTypes(),
            Optional.of("")
        ));
      }
    }
    state.programInitialization(tu);
    return state;
  }
}
