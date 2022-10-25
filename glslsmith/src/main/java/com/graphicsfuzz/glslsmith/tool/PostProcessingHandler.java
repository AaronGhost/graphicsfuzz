package com.graphicsfuzz.glslsmith.tool;

import com.graphicsfuzz.common.ast.TranslationUnit;
import com.graphicsfuzz.common.util.ParseHelper;
import com.graphicsfuzz.glslsmith.Buffer;
import com.graphicsfuzz.glslsmith.ProgramState;
import com.graphicsfuzz.glslsmith.config.ConfigInterface;
import com.graphicsfuzz.glslsmith.config.ParameterConfiguration;
import com.graphicsfuzz.glslsmith.postprocessing.ArithmeticWrapperBuilder;
import com.graphicsfuzz.glslsmith.postprocessing.ArrayIndexBuilder;
import com.graphicsfuzz.glslsmith.postprocessing.BufferFormatEnforcer;
import com.graphicsfuzz.glslsmith.postprocessing.CallingOrderCleaner;
import com.graphicsfuzz.glslsmith.postprocessing.InitializerEnforcer;
import com.graphicsfuzz.glslsmith.postprocessing.LoopLimiter;
import com.graphicsfuzz.glslsmith.postprocessing.PostProcessorInterface;
import com.graphicsfuzz.glslsmith.postprocessing.StdWrapperBuilder;
import com.graphicsfuzz.glslsmith.stateprinters.AmberStatePrinter;
import com.graphicsfuzz.glslsmith.stateprinters.ShaderTrapStatePrinter;
import com.graphicsfuzz.glslsmith.stateprinters.StatePrinter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class PostProcessingHandler {

  // post-processors need to be registered here
  private static final List<PostProcessorInterface> allProcessors = Arrays.asList(
      new BufferFormatEnforcer(),
      new InitializerEnforcer(),
      new CallingOrderCleaner(),
      new LoopLimiter(false, 10),
      new ArithmeticWrapperBuilder(),
      new StdWrapperBuilder(),
      new ArrayIndexBuilder(false)
  );

  private static final List<PostProcessorInterface> coreProcessors = Arrays.asList(
      new LoopLimiter(false, 10),
      new ArrayIndexBuilder(true)
  );

  private static final List<PostProcessorInterface> extraProcessors = Arrays.asList(
      new BufferFormatEnforcer(),
      new InitializerEnforcer(),
      new CallingOrderCleaner(),
      new ArithmeticWrapperBuilder(),
      new StdWrapperBuilder()
  );

  public static void updateFile(String src, String dest, boolean addIds, String reduceWrappers,
                                String extent) {
    try {
      // Recognize the format of the code based on the extension
      String[] shaderFileExtensions = src.split("\\.");
      StatePrinter shaderPrinter = new ShaderTrapStatePrinter();
      if (shaderFileExtensions[shaderFileExtensions.length - 1 ].equals("amber")) {
        shaderPrinter = new AmberStatePrinter();
      } else if (!shaderFileExtensions[shaderFileExtensions.length - 1].equals("shadertrap")) {
        throw new RuntimeException("Provided file is not a shadertrap or an amber file");
      }
      String harnessText = Files.readString(Path.of(src));
      List<Buffer> buffers = shaderPrinter.getBuffersFromHarness(harnessText);
      //TODO determine the kind of shader from the harness
      //Setup the Program state and the TU using the parsed code
      //TODO see if the config interface is necessary to the program state
      ParameterConfiguration.Builder builder = new ParameterConfiguration.Builder();
      builder.withTypeDecoratorsOnBuffers(false);
      if (addIds) {
        builder.withRunType(ConfigInterface.RunType.ADDED_ID);
      } else if (!reduceWrappers.equals("")) {
        builder.withRunType(ConfigInterface.RunType.REDUCED_WRAPPERS);
      }
      ConfigInterface configInterface = builder.getConfig();
      ProgramState programState = new ProgramState(configInterface);
      for (Buffer buffer: buffers) {
        programState.addBuffer(buffer);
      }
      String glslCode = shaderPrinter.getShaderCodeFromHarness(harnessText);
      TranslationUnit unit = ParseHelper.parse(glslCode, configInterface.getShaderKind());
      if (!reduceWrappers.equals("")) {
        programState.setIds(shaderPrinter.parseIdsBuffer(reduceWrappers));
      }
      programState.programInitialization(unit);

      //Pipeline post-processing using presets of postprocessors (for experiments)
      // TODO add a way to call a single processing step
      List<PostProcessorInterface> postProcessors = allProcessors;
      if (extent.equals("core")) {
        postProcessors = coreProcessors;
      } else if (extent.equals("extra")) {
        postProcessors = extraProcessors;
      }

      for (PostProcessorInterface postProcessorInterface : postProcessors) {
        programState = postProcessorInterface.process(programState);
      }

      //Replace the glsl code from the shadertrap
      String newGlslCode = programState.getShaderCode();
      String newHarnessText = shaderPrinter.changeShaderFromHarness(harnessText, newGlslCode);
      if (addIds && programState.hasIdsBuffer()) {
        newHarnessText = shaderPrinter.addBufferToHarness(newHarnessText,
            programState.getIdsBuffer());
      }
      Files.write(Path.of(dest), newHarnessText.getBytes());
      //TODO use the printer with a buffer instruction

      System.out.println("SUCCESS!");
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
    parser.addArgument("--id_wrappers")
        .dest("id_wrappers")
        .action(Arguments.storeTrue())
        .help("Add an id field to the wrapper calls");
    parser.addArgument("--reduce_wrappers")
        .dest("reduce")
        .type(String.class)
        .setConst("ids.txt")
        .setDefault("")
        .help("Name of the buffer containing the function ids");
    parser.addArgument("--extent")
        .dest("extent")
        .type(String.class)
        .setDefault("full");
    Namespace ns = parser.parseArgs(args);
    updateFile(ns.getString("src"), ns.getString("dest"),
        ns.getBoolean("id_wrappers"), ns.getString("reduce"), ns.getString("extent"));
  }
}
