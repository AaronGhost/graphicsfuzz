package org.graphicsfuzz;

import com.graphicsfuzz.common.util.RandomWrapper;
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

    Namespace ns = parser.parseArgs(args);
    System.out.println("Seed:" + ns.getLong("seed"));

    //Instantiate main class
    ProgramGenerator generator = new ProgramGenerator(new RandomWrapper(ns.getLong("seed")));

    try {
      //Generates the number of program given in argument of the program
      for (int i = 0; i  < ns.getInt("count"); i++) {
        System.out.println("Generating shader " + i);
        String program = generator.generateProgram(new ShaderTrapStatePrinter());
        FileWriter outputfile = new FileWriter("test_" + i + ".shadertrap");
        outputfile.write(program);
        outputfile.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
