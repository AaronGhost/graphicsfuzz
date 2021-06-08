package com.graphicsfuzz;

import com.graphicsfuzz.common.util.RandomWrapper;
import com.graphicsfuzz.config.ConfigInterface;
import com.graphicsfuzz.config.DefaultConfig;
import com.graphicsfuzz.random.MultipleRangeRandomWrapper;
import com.graphicsfuzz.stateprinters.ShaderTrapStatePrinter;
import com.graphicsfuzz.stateprinters.StatePrinter;
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
    parser.addArgument("--config")
        .dest("configFile")
        .type(String.class)
        .setDefault("default")
        .help("Specify a config type for the generator");

    Namespace ns = parser.parseArgs(args);
    System.out.println("Seed:" + ns.getLong("seed"));

    //Instantiate main class with the selected random Generator
    StatePrinter shadertrapWrapper = new ShaderTrapStatePrinter();
    try {
      //Generates the number of program given in argument of the program
      for (int i = 0; i < ns.getInt("count"); i++) {
        System.out.println("Generating shader " + i);
        RandomWrapper randomWrapper;
        ConfigInterface configuration;
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
        if (ns.getString("configFile").equals("default")) {
          configuration = new DefaultConfig();
        } else {
          throw new UnsupportedOperationException("Only default configuration are supported at "
              + "the moment");
        }
        ProgramGenerator generator = new ProgramGenerator(randomWrapper, configuration);
        String program = generator.generateProgram(shadertrapWrapper);
        FileWriter outputfile = new FileWriter("test_" + i + ".shadertrap");
        outputfile.write(program);
        outputfile.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
