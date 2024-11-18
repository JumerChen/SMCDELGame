// Import statements
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

public class JewelGame {
    private static final String SMCDEL_PATH = "./smcdel";
    private static final String INPUT_FILE = "Input.smcdel.txt";
    private static final Map<Integer, String> STATE_TO_JEWEL = Map.of(
            1, "Red", 2, "Orange", 3, "Yellow",
            4, "Green", 5, "Black", 6, "Blue", 7, "Purple"
    );
    private static final List<String> INITIAL_DISTRIBUTION = List.of(
            "Alice: Red, Green", "Bob: Yellow, Black", "Carol: Blue, Orange"
    );
    private static final Set<String> ALL_JEWELS = new HashSet<>(STATE_TO_JEWEL.values());

    private static final List<String> queryHistory = new ArrayList<>();
    private static final List<String> resultHistory = new ArrayList<>();
    private static Set<String> possibleMissingJewels = new HashSet<>(ALL_JEWELS);

    public static void main(String[] args) {
        System.out.println("Welcome to the Jewel Distribution Game!");
        System.out.println("Try to deduce which jewel is missing.\n");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("Enter your logic formula (or type 'history', 'show states', 'show initial', 'guess', or 'exit'):");
                String userInput = scanner.nextLine().trim();

                if ("exit".equalsIgnoreCase(userInput)) {
                    System.out.println("Thank you for playing the Jewel Distribution Game!");
                    break;
                }

                switch (userInput.toLowerCase()) {
                    case "history":
                        showHistory();
                        break;
                    case "show states":
                        showAllStates();
                        break;
                    case "show initial":
                        showInitialDistribution();
                        break;
                    case "guess":
                        handleGuess(scanner);
                        break;
                    default:
                        handleQuery(userInput);
                        break;
                }
            }
        }
    }

    private static void handleQuery(String formula) {
        if (!isValidFormula(formula)) {
            System.out.println("Invalid formula format. Please try again.");
            return;
        }

        writeSMCDELFile(formula);
        String output = runSMCDEL();
        queryHistory.add(formula);
        resultHistory.add(output);

        System.out.println("SMCDEL Output:");
        System.out.println(output);

        List<Integer> matchedStates = parseMatchingStates(output);
        if (matchedStates.isEmpty() || matchedStates.size() == STATE_TO_JEWEL.size()) {
            System.out.println("No matching states found. Unable to narrow down possible missing jewels.");
        } else {
            interpretAndDisplayOutput(matchedStates);
        }
    }

    private static boolean isValidFormula(String formula) {
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

    private static List<Integer> parseMatchingStates(String output) {
        Pattern pattern = Pattern.compile("\\[(\\d+(,\\s*\\d+)*)\\]");
        Matcher matcher = pattern.matcher(output);

        if (matcher.find()) {
            return Arrays.stream(matcher.group(1).split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private static void interpretAndDisplayOutput(List<Integer> matchedStates) {
        System.out.println("Matching states:");
        matchedStates.forEach(state -> System.out.println("- State " + state + ": " + STATE_TO_JEWEL.get(state)));

        Set<String> observedJewels = matchedStates.stream()
                .map(STATE_TO_JEWEL::get)
                .collect(Collectors.toSet());
        possibleMissingJewels.removeAll(observedJewels);

        if (possibleMissingJewels.isEmpty()) {
            System.out.println("No possible missing jewels could be inferred.");
        } else {
            System.out.println("Possible missing jewels: " + String.join(", ", possibleMissingJewels));
        }
    }

    private static void showHistory() {
        System.out.println("\nQuery History:");
        for (int i = 0; i < queryHistory.size(); i++) {
            System.out.println((i + 1) + ". " + queryHistory.get(i));
            System.out.println("   Result: " + resultHistory.get(i));
        }
    }

    private static void showAllStates() {
        System.out.println("\nAll Possible States:");
        STATE_TO_JEWEL.forEach((state, jewel) -> System.out.println("- State " + state + ": " + jewel));
    }

    private static void showInitialDistribution() {
        System.out.println("\nInitial Jewel Distribution:");
        INITIAL_DISTRIBUTION.forEach(System.out::println);
    }

    private static void handleGuess(Scanner scanner) {
        System.out.println("Enter your guess for the missing jewel:");
        String guess = scanner.nextLine().trim();
        if (ALL_JEWELS.contains(guess)) {
            System.out.println("Your guess: " + guess);
            System.out.println(possibleMissingJewels.contains(guess)
                    ? "Correct! " + guess + " was missing!"
                    : "Incorrect guess. The missing jewel is not " + guess + ".");
        } else {
            System.out.println("Invalid jewel name. Please try again.");
        }
    }
}
