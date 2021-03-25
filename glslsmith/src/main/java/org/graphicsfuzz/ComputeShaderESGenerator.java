package org.graphicsfuzz;


import com.graphicsfuzz.common.ast.TranslationUnit;
import com.graphicsfuzz.common.ast.decl.Declaration;
import com.graphicsfuzz.common.ast.decl.FunctionDefinition;
import com.graphicsfuzz.common.ast.decl.FunctionPrototype;

import com.graphicsfuzz.common.ast.decl.InterfaceBlock;
import com.graphicsfuzz.common.ast.decl.ParameterDecl;
import com.graphicsfuzz.common.ast.stmt.BlockStmt;
import com.graphicsfuzz.common.ast.stmt.Stmt;
import com.graphicsfuzz.common.ast.type.LayoutQualifier;
import com.graphicsfuzz.common.ast.type.LayoutQualifierSequence;
import com.graphicsfuzz.common.ast.type.LocalSizeLayoutQualifier;
import com.graphicsfuzz.common.ast.type.TypeQualifier;
import com.graphicsfuzz.common.ast.type.VoidType;
import com.graphicsfuzz.common.glslversion.ShadingLanguageVersion;
import com.graphicsfuzz.common.util.ShaderKind;
import java.util.ArrayList;
import java.util.Optional;

public class ComputeShaderESGenerator {
  public void generateEmptyProgram(ProgramState programState) {
    ArrayList<LayoutQualifier> local_sizes = new ArrayList<>();
    local_sizes.add(new LocalSizeLayoutQualifier("x",1));
    local_sizes.add(new LocalSizeLayoutQualifier("y",1));
    local_sizes.add(new LocalSizeLayoutQualifier("z",1));
    InterfaceBlock inVariable =
        new InterfaceBlock(Optional.of(new LayoutQualifierSequence(local_sizes)),
         TypeQualifier.IN_PARAM, "", new ArrayList<>(), new ArrayList<>(), Optional.empty());
    FunctionDefinition mainFunction = new FunctionDefinition(new FunctionPrototype("main",
        VoidType.VOID, new ArrayList<>()),new BlockStmt(new ArrayList<Stmt>(),true));
    ArrayList<Declaration> declList = new ArrayList<>();
    declList.add(inVariable);
    declList.add(mainFunction);
    TranslationUnit translationUnit = new TranslationUnit(ShaderKind.COMPUTE,
        Optional.of(ShadingLanguageVersion.ESSL_320),declList);
    programState.programInitialization(translationUnit);
  }
}
