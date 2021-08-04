package com.graphicsfuzz;

import com.graphicsfuzz.common.ast.TranslationUnit;
import com.graphicsfuzz.common.util.ParseHelper;
import com.graphicsfuzz.common.util.ShaderKind;
import com.graphicsfuzz.config.DefaultConfig;
import com.graphicsfuzz.postprocessing.ArithmeticWrapperBuilder;
import com.graphicsfuzz.postprocessing.ArrayIndexBuilder;
import com.graphicsfuzz.postprocessing.BufferFormatEnforcer;
import com.graphicsfuzz.postprocessing.CallingOrderCleaner;
import com.graphicsfuzz.postprocessing.InitializerEnforcer;
import com.graphicsfuzz.postprocessing.LoopLimiter;
import com.graphicsfuzz.postprocessing.PostProcessorInterface;
import com.graphicsfuzz.postprocessing.StdWrapperBuilder;
import com.graphicsfuzz.stateprinters.ShaderTrapStatePrinter;
import com.graphicsfuzz.stateprinters.StatePrinter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class PostProcessingHandler {

  // post-processors need to be registered here
  private static final List<PostProcessorInterface> postProcessors = Arrays.asList(
      new BufferFormatEnforcer(),
      new InitializerEnforcer(),
      new CallingOrderCleaner(),
      new LoopLimiter(true, 100),
      new ArithmeticWrapperBuilder(),
      new StdWrapperBuilder(),
      new ArrayIndexBuilder(false)
  );

  public static void updateFile(String src, String dest) {
    try {
      //Parse the file to collect the glsl code
      String harnessText = Files.readString(Path.of(src));
      //TODO adapt to any state printer
      StatePrinter shaderPrinter = new ShaderTrapStatePrinter();
      String glslCode = shaderPrinter.getShaderCodeFromHarness(harnessText);
      List<Buffer> buffers = shaderPrinter.getBuffersFromHarness(harnessText);
      //TODO determine the kind of shader from the harness
      //Setup the Program state and the TU using the parsed code
      //TODO see if the config interface is necessary to the program state
      ProgramState programState = new ProgramState(new DefaultConfig());
      for (Buffer buffer: buffers) {
        programState.addBuffer(buffer);
      }
      TranslationUnit unit = ParseHelper.parse(glslCode, ShaderKind.COMPUTE);
      programState.programInitialization(unit);
      //TODO setup the buffers according to the given values
      //Pipeline post-processing
      for (PostProcessorInterface postProcessorInterface : postProcessors) {
        programState = postProcessorInterface.process(programState);
      }
      //Replace the glsl code from the shadertrap
      String newGlslCode = programState.getShaderCode();
      String newHarnessText = shaderPrinter.changeShaderFromHarness(harnessText, newGlslCode);
      Files.write(Path.of(dest), newHarnessText.getBytes());
      System.out.println("SUCCESS!");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //TODO specifically call post-processing
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
  }
}
