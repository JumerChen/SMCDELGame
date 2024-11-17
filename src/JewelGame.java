import java.io.*;
import java.util.Scanner;

public class JewelGame {
    private static final String SMCDEL_PATH = "./smcdel"; // 假设 smcdel 可执行文件在当前目录
    private static final String INPUT_FILE = "Input.smcdel.txt";

    public static void main(String[] args) {
        System.out.println("Welcome to the Jewel Distribution Game!");
        System.out.println("Try to deduce which jewel is missing.\n");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("Enter your logic formula (or type 'exit' to quit):");
                String userInput = scanner.nextLine().trim();

                if ("exit".equalsIgnoreCase(userInput)) {
                    System.out.println("Thank you for playing the Jewel Distribution Game!");
                    break;
                }

                // Validate user input format (optional, basic check)
                if (!isValidFormula(userInput)) {
                    System.out.println("Invalid formula format. Please try again.");
                    continue;
                }

                // Write user input to .smcdel.txt file
                writeSMCDELFile(userInput);

                // Execute SMCDEL and capture output
                String output = runSMCDEL();

                // Display SMCDEL output to user
                System.out.println("SMCDEL Output:");
                System.out.println(output);
            }
        }
    }

    private static boolean isValidFormula(String formula) {
        // 简单验证公式格式，例如不能为空，且只包含合法字符
        return formula != null && formula.matches("[a-zA-Z0-9_\\s&|~()]+");
    }

    private static void writeSMCDELFile(String formula) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(INPUT_FILE))) {
            writer.write("-- Jewel Distribution Game User Query\n");
            writer.write("VARS 1, 2, 3, 4, 5, 6, 7\n");
            writer.write("LAW ONEOF(1, 2, 3, 4, 5, 6, 7)\n");
            writer.write("OBS alice: 1, 4\n");
            writer.write("     bob: 3, 5\n");
            writer.write("     carol: 6, 2\n");
            writer.write("WHERE?\n");
            writer.write("  " + formula + "\n");
        } catch (IOException e) {
            System.err.println("Error writing to SMCDEL input file: " + e.getMessage());
        }
    }

    private static String runSMCDEL() {
        StringBuilder output = new StringBuilder();
        ProcessBuilder processBuilder = new ProcessBuilder(SMCDEL_PATH, INPUT_FILE);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.append("SMCDEL execution failed with exit code: ").append(exitCode).append("\n");
            }
        } catch (IOException | InterruptedException e) {
            output.append("Error running SMCDEL: ").append(e.getMessage()).append("\n");
        }

        return output.toString();
    }
}
