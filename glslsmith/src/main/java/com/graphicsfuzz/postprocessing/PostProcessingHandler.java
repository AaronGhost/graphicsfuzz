package com.graphicsfuzz.postprocessing;

import com.graphicsfuzz.ProgramState;
import com.graphicsfuzz.common.ast.TranslationUnit;
import com.graphicsfuzz.common.util.ParseHelper;
import com.graphicsfuzz.common.util.ShaderKind;
import com.graphicsfuzz.config.DefaultConfig;
import com.graphicsfuzz.stateprinters.ShaderTrapStatePrinter;
import com.graphicsfuzz.stateprinters.StatePrinter;
import java.nio.file.Files;
import java.nio.file.Path;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class PostProcessingHandler {

  public static void updateFile(String src, String dest) {
    try {
      //Parse the file to collect the glsl code
      String harnessText = Files.readString(Path.of(src));
      //TODO adapt to any state printer
      StatePrinter shaderPrinter = new ShaderTrapStatePrinter();
      String glslCode = shaderPrinter.getShaderCodeFromHarness(harnessText);
      //Setup the Program state and the TU using the parsed code
      //TODO see if the config interface is necessary to the program state
      ProgramState programState = new ProgramState(new DefaultConfig());
      TranslationUnit unit = ParseHelper.parse(glslCode);
      programState.programInitialization(unit, ShaderKind.COMPUTE);
      //TODO setup the buffers according to the given values
      //Pipeline post-processing
      WrapperBuilder builder = new WrapperBuilder();
      programState = builder.process(programState);
      LoopLimiter limiter = new LoopLimiter(true, 100);
      programState = limiter.process(programState);
      //Replace the glsl code from the shadertrap
      String newGlslCode = programState.getShaderCode();
      String newHarnessText = shaderPrinter.changeShaderFromHarness(harnessText, newGlslCode);
      Files.write(Path.of(dest), newHarnessText.getBytes());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws ArgumentParserException {
    //Parse Arguments to know what to look at
    ArgumentParser parser = ArgumentParsers.newArgumentParser("GLSLsmith postprocessing")
        .defaultHelp(true).description("Applies post-processing steps to shader tests");
    parser.addArgument("--src")
        .dest("src")
        .type(String.class)
        .setDefault("test.shadertrap")
        .help("Source file to parse");
    parser.addArgument("--dest")
        .type(String.class)
        .setDefault("test.shadertrap")
        .help("Destination file for the new shader");
    Namespace ns = parser.parseArgs(args);
    updateFile(ns.getString("src"), ns.getString("dest"));
    System.out.println("SUCCESS!");
  }
}
