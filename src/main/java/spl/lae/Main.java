package spl.lae;
import java.io.IOException;
import java.text.ParseException;

import memory.SharedMatrix;
import parser.*;

public class Main {
    public static void main(String[] args) throws IOException {
      if (args.length != 3) {
            System.out.println("Error: Invalid arguments.");
            System.out.println("Usage: java Assignment2 <num_threads> <input_json> <output_file>");
            return;
      }

      int numThreads = Integer.parseInt(args[0]);
      String inputJson = args[1];
      String outputFile = args[2];
      LinearAlgebraEngine engine = null;

      try{
        InputParser parser = new InputParser();
        ComputationNode rootNode = parser.parse(inputJson);
        
        engine = new LinearAlgebraEngine(numThreads);
        ComputationNode resultNode = engine.run(rootNode);

        double[][] resultMatrix = resultNode.getMatrix();
        OutputWriter.write(resultMatrix, outputFile);
        System.out.println("Computation completed successfully. Result written to " + outputFile);
      }
      catch (/*ParseException |*/ IOException | IllegalArgumentException e) {
        System.out.println("Error: " + e.getMessage());
        try {
            OutputWriter.write(e.getMessage(), outputFile);
        }
        catch (IOException ioException) {
            System.err.println("Failed to write error to output file.");
            ioException.printStackTrace();
        }
      }
      catch (Exception e) {
            e.printStackTrace();
      } 
      //finally {
        // if (engine != null) {
        //     try {
        //         engine.shutdown();
        //     } catch (InterruptedException e) {
        //         System.err.println("Failed to shut down the executor.");
        //         Thread.currentThread().interrupt();
        //     }
        //}
      //}
    }
}