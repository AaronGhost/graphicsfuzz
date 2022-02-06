package com.graphicsfuzz.glslsmith.tool;

import com.graphicsfuzz.common.util.RandomWrapper;
import com.graphicsfuzz.glslsmith.ProgramGenerator;
import com.graphicsfuzz.glslsmith.config.ParameterConfiguration;
import com.graphicsfuzz.glslsmith.random.MultipleRangeRandomWrapper;
import com.graphicsfuzz.glslsmith.stateprinters.AmberStatePrinter;
import com.graphicsfuzz.glslsmith.stateprinters.ShaderTrapStatePrinter;
import com.graphicsfuzz.glslsmith.stateprinters.StatePrinter;
import java.io.FileWriter;
import java.io.IOException;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;


public class GeneratorHandler {

  public static void main(String[] args) throws ArgumentParserException {
    ArgumentParser parser = ArgumentParsers.newArgumentParser("GLSLsmith")
        .defaultHelp(true).description("Generates random shaders");

    parser.addArgument("--seed")
        .dest("seed")
        .type(Long.class)
        .setDefault(System.currentTimeMillis())
        .help("Seeds the random generator");
    parser.addArgument("--shader-count")
        .dest("count")
        .type(Integer.class)
        .setDefault(5)
        .help("Specify the number of created shaders");
    parser.addArgument("--random-generator")
        .dest("randomGenerator")
        .type(String.class)
        .setDefault("multiplerange")
        .help("Specify the type of random generator");
    parser.addArgument("--output-directory")
        .dest("directory")
        .type(String.class)
        .setDefault("./")
        .help("Specify an output directory for the generated files");
    parser.addArgument("--printer-format")
        .dest("printer")
        .type(String.class)
        .setDefault("shadertrap")
        .help("Specify the output format for the generated files (shadertrap / amber)");


    Namespace ns = parser.parseArgs(args);
    System.out.println("Seed:" + ns.getLong("seed"));

    //Instantiate main class with the selected random Generator
    try {
      // generate a configuration builder

      ParameterConfiguration.Builder builder = new ParameterConfiguration.Builder();
      //TODO fix the problem with coherent
      builder = builder.withTypeDecoratorsOnBuffers(false);

      // Select the state printer
      StatePrinter wrapper = new ShaderTrapStatePrinter();
      String fileExtension = ".shadertrap";
      if (ns.getString("printer").equals("amber")) {
        wrapper = new AmberStatePrinter();
        fileExtension = ".amber";
        // forbids mixing types within interface blocks
        builder = builder.withSingleTypePerBuffer(true);
      } else if (!ns.getString("printer").equals("shadertrap")) {
        System.out.println("Unrecognized host language, defaulting to ShaderTrap");
      }

      //Generates the number of program given in argument of the program
      for (int i = 0; i < ns.getInt("count"); i++) {
        System.out.println("Generating shader " + i);
        RandomWrapper randomWrapper;
        if (ns.getString("randomGenerator").equals("multiplerange")) {
          randomWrapper = new MultipleRangeRandomWrapper(ns.getLong("seed") + i);
        } else if (ns.getString("randomGenerator").equals("nospecial")) {
          randomWrapper = new MultipleRangeRandomWrapper(ns.getLong("seed") + i,
              MultipleRangeRandomWrapper.GeneratorType.SMALL,
              MultipleRangeRandomWrapper.GeneratorType.HIGH,
              MultipleRangeRandomWrapper.GeneratorType.FULL);
        } else if (ns.getString("randomGenerator").equals("uniform")) {
          randomWrapper = new RandomWrapper(ns.getLong("seed") + i);
        } else {
          throw new UnsupportedOperationException("Random generator not recognized");
        }
        ProgramGenerator generator = new ProgramGenerator(randomWrapper,
            builder.getConfig());
        String program = generator.generateProgram(wrapper);
        FileWriter outputFile = new FileWriter(ns.getString("directory") + "test_" + i
            + fileExtension);
        outputFile.write(program);
        outputFile.close();
      }
      System.out.println("SUCCESS");
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
