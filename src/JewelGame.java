import java.io.*;
import java.util.*;

public class JewelGame {
    private static final String SMCDEL_PATH = "./smcdel"; // 假设 smcdel 可执行文件在当前目录
    private static final String INPUT_FILE = "Input.smcdel.txt";

    private static final Map<Integer, String> JEWEL_MAP = Map.of(
            1, "Red",
            2, "Orange",
            3, "Yellow",
            4, "Green",
            5, "Black",
            6, "Blue",
            7, "Purple"
    );

    private static final List<String> QUERY_HISTORY = new ArrayList<>();
    private static final List<String> RESULT_HISTORY = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Welcome to the Jewel Distribution Game!");
        System.out.println("Try to deduce which jewel is missing.\n");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("Enter your logic formula (or type 'history' to view past queries, 'exit' to quit):");
                String userInput = scanner.nextLine().trim();

                if ("exit".equalsIgnoreCase(userInput)) {
                    System.out.println("Thank you for playing the Jewel Distribution Game!");
                    break;
                }

                if ("history".equalsIgnoreCase(userInput)) {
                    showHistory();
                    continue;
                }

                userInput = userInput.toLowerCase();

                if (!isValidFormula(userInput)) {
                    System.out.println("Invalid formula format. Please try again.");
                    continue;
                }

                writeSMCDELFile(userInput);

                String smcdelOutput = runSMCDEL();

                interpretAndDisplayOutput(userInput, smcdelOutput);
            }
        }
    }

    private static boolean isValidFormula(String formula) {
        return formula != null && formula.matches("[a-z0-9_\\s&|~()]+");
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
                    // 移除 ANSI 转义序列
                    line = line.replaceAll("\u001B\\[[;\\d]*m", "");
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

    private static void interpretAndDisplayOutput(String formula, String smcdelOutput) {
        QUERY_HISTORY.add(formula);
        RESULT_HISTORY.add(smcdelOutput);

        System.out.println("SMCDEL Output:");
        System.out.println(smcdelOutput);

        if (smcdelOutput.contains("At which states")) {
            String[] states = extractStates(smcdelOutput);
            if (states.length == 0) {
                System.out.println("No states match the given query.");
            } else {
                System.out.println("Matching states:");
                for (String state : states) {
                    try {
                        int stateId = Integer.parseInt(state.trim());
                        System.out.println("- State " + stateId + ": " + describeState(stateId));
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Invalid state ID detected in SMCDEL output: " + state);
                    }
                }
            }
        }
    }

    private static String[] extractStates(String smcdelOutput) {
        try {
            String[] parts = smcdelOutput.split("\\[|\\]");
            return parts.length > 1 ? parts[1].split(",") : new String[0];
        } catch (Exception e) {
            return new String[0];
        }
    }

    private static String describeState(int stateId) {
        return JEWEL_MAP.getOrDefault(stateId, "Unknown Jewel");
    }

    private static void showHistory() {
        System.out.println("\nQuery History:");
        for (int i = 0; i < QUERY_HISTORY.size(); i++) {
            System.out.println((i + 1) + ". Query: " + QUERY_HISTORY.get(i));
            System.out.println("   Result: " + RESULT_HISTORY.get(i).replace("\n", "\n      "));
        }
    }
}
