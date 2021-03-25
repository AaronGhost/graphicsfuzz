package org.graphicsfuzz;

import java.io.FileWriter;
import java.io.IOException;

public class GeneratorHandler {

  public static void main(String[] args) {
    //Instantiate main class
    ProgramGenerator generator = new ProgramGenerator();
    String program = generator.generateProgram();
    //Write shadertrap program
    try {
      System.out.println(System.getProperty("user.dir"));
      FileWriter outputfile = new FileWriter("test.shadertrap");
      outputfile.write(program);
      outputfile.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
