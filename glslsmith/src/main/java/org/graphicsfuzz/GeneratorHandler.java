package org.graphicsfuzz;

import com.graphicsfuzz.common.util.RandomWrapper;
import java.io.FileWriter;
import java.io.IOException;

public class GeneratorHandler {

  public static void main(String[] args) {
    //Instantiate main class

    ProgramGenerator generator = new ProgramGenerator(new RandomWrapper(0));
    String program = generator.generateProgram(new ShaderTrapStatePrinter());
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
