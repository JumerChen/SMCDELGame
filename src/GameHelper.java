import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GameHelper {
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

    public static void handleQuery(String formula, ResourceBundle messages) {
        if (!isValidFormula(formula)) {
            System.out.println(messages.getString("invalid_format"));
            System.out.println(messages.getString("input_examples"));
            return;
        }

        SMCDELHelper.writeSMCDELFile(formula, messages);
        String output = SMCDELHelper.runSMCDEL(messages);
        queryHistory.add(formula);
        resultHistory.add(output);

        System.out.println(messages.getString("smcdel_output"));
        System.out.println(output);

        List<Integer> matchedStates = parseMatchingStates(output);
        if (matchedStates.isEmpty() || matchedStates.size() == STATE_TO_JEWEL.size()) {
            System.out.println(messages.getString("no_matching_states"));
        } else {
            interpretAndDisplayOutput(matchedStates, messages);
        }
    }

    public static void showFormulaExamples(ResourceBundle messages) {
        System.out.println(messages.getString("input_examples"));
        System.out.println("1. alice knows whether 1");
        System.out.println("2. bob knows that 3 & ~6");
        System.out.println("3. carol knows whether (1 | 4)");
    }

    public static void showHistory(ResourceBundle messages) {
        System.out.println("\n" + messages.getString("query_history"));
        for (int i = 0; i < queryHistory.size(); i++) {
            System.out.println((i + 1) + ". " + queryHistory.get(i));
            System.out.println("   " + messages.getString("result") + resultHistory.get(i));
        }
    }

    public static void showAllStates(ResourceBundle messages) {
        System.out.println("\n" + messages.getString("all_states"));
        STATE_TO_JEWEL.forEach((state, jewel) -> System.out.println("- State " + state + ": " + jewel));
    }

    public static void showInitialDistribution(ResourceBundle messages) {
        System.out.println("\n" + messages.getString("initial_distribution"));
        INITIAL_DISTRIBUTION.forEach(System.out::println);
    }

    public static void handleGuess(Scanner scanner, ResourceBundle messages) {
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

    private static boolean isValidFormula(String formula) {
        return formula != null && formula.matches("[a-zA-Z0-9_\\s&|~()]+");
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

    private static void interpretAndDisplayOutput(List<Integer> matchedStates, ResourceBundle messages) {
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
}
