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

    private static ResourceBundle messages;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        chooseLanguage(scanner);
        System.out.println(messages.getString("welcome"));
        System.out.println(messages.getString("instructions"));
        System.out.println(messages.getString("input_hint"));
        showFormulaExamples(); // Show formula examples to guide the user

        while (true) {
            System.out.println(messages.getString("prompt"));
            if (!scanner.hasNextLine()) {
                break;
            }
            String userInput = scanner.nextLine().trim();

            if ("exit".equalsIgnoreCase(userInput) || "退出".equalsIgnoreCase(userInput)) {
                System.out.println(messages.getString("goodbye"));
                break;
            }

            switch (userInput.toLowerCase()) {
                case "history":
                case "历史记录":
                    showHistory();
                    break;
                case "show states":
                case "显示状态":
                    showAllStates();
                    break;
                case "show initial":
                case "显示初始分配":
                    showInitialDistribution();
                    break;
                case "guess":
                case "猜测":
                    handleGuess(scanner);
                    break;
                default:
                    handleQuery(userInput);
                    break;
            }
        }
    }

    private static void chooseLanguage(Scanner scanner) {
        System.out.println("Choose your language / 请选择你的语言:");
        System.out.println("1. English");
        System.out.println("2. 中文 (Chinese)");
        String choice = scanner.nextLine().trim();
        switch (choice.toLowerCase()) {
            case "2":
            case "中文":
            case "chinese":
                messages = ResourceBundle.getBundle("MessagesBundle", new Locale("zh", "CN"));
                break;
            case "1":
            case "english":
            default:
                messages = ResourceBundle.getBundle("MessagesBundle", new Locale("en", "US"));
                break;
        }
    }

    private static void handleQuery(String formula) {
        if (!isValidFormula(formula)) {
            System.out.println(messages.getString("invalid_format"));
            System.out.println(messages.getString("input_examples"));
            return;
        }

        writeSMCDELFile(formula);
        String output = runSMCDEL();
        queryHistory.add(formula);
        resultHistory.add(output);

        System.out.println(messages.getString("smcdel_output"));
        System.out.println(output);

        List<Integer> matchedStates = parseMatchingStates(output);
        if (matchedStates.isEmpty() || matchedStates.size() == STATE_TO_JEWEL.size()) {
            System.out.println(messages.getString("no_matching_states"));
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
            System.err.println(messages.getString("error_writing_file") + e.getMessage());
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
                output.append(messages.getString("smcdel_failed") + exitCode).append("\n");
            }
        } catch (IOException | InterruptedException e) {
            output.append(messages.getString("error_running_smcdel") + e.getMessage()).append("\n");
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
        System.out.println(messages.getString("matching_states"));
        matchedStates.forEach(state -> System.out.println("- State " + state + ": " + STATE_TO_JEWEL.get(state)));

        Set<String> observedJewels = matchedStates.stream()
                .map(STATE_TO_JEWEL::get)
                .collect(Collectors.toSet());
        possibleMissingJewels.removeAll(observedJewels);

        if (possibleMissingJewels.isEmpty()) {
            System.out.println(messages.getString("no_possible_missing"));
        } else {
            System.out.println(messages.getString("possible_missing") + String.join(", ", possibleMissingJewels));
        }
    }

    private static void showFormulaExamples() {
        System.out.println(messages.getString("input_examples"));
        System.out.println("1. alice knows whether 1");
        System.out.println("2. bob knows that 3 & ~6");
        System.out.println("3. carol knows whether (1 | 4)");
    }

    private static void showHistory() {
        System.out.println("\n" + messages.getString("query_history"));
        for (int i = 0; i < queryHistory.size(); i++) {
            System.out.println((i + 1) + ". " + queryHistory.get(i));
            System.out.println("   " + messages.getString("result") + resultHistory.get(i));
        }
    }

    private static void showAllStates() {
        System.out.println("\n" + messages.getString("all_states"));
        STATE_TO_JEWEL.forEach((state, jewel) -> System.out.println("- State " + state + ": " + jewel));
    }

    private static void showInitialDistribution() {
        System.out.println("\n" + messages.getString("initial_distribution"));
        INITIAL_DISTRIBUTION.forEach(System.out::println);
    }

    private static void handleGuess(Scanner scanner) {
        System.out.println(messages.getString("enter_guess"));
        if (!scanner.hasNextLine()) {
            return;
        }
        String guess = scanner.nextLine().trim();
        if (ALL_JEWELS.contains(guess)) {
            System.out.println(messages.getString("your_guess") + guess);
            System.out.println(possibleMissingJewels.contains(guess)
                    ? messages.getString("correct_guess") + guess + messages.getString("was_missing")
                    : messages.getString("incorrect_guess") + guess + messages.getString("not_missing"));
        } else {
            System.out.println(messages.getString("invalid_jewel"));
        }
    }
}
