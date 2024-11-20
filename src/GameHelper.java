import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GameHelper {
    // 状态编号与宝石名称的映射
    private static final Map<Integer, String> STATE_TO_JEWEL = Map.of(
            1, "Red", 2, "Orange", 3, "Yellow",
            4, "Green", 5, "Black", 6, "Blue", 7, "Purple"
    );

    // 初始宝石分配
    private static final List<String> INITIAL_DISTRIBUTION = List.of(
            "Alice: Red, Green", "Bob: Yellow, Black", "Carol: Blue, Orange"
    );

    // 所有宝石集合
    private static final Set<String> ALL_JEWELS = new HashSet<>(STATE_TO_JEWEL.values());

    // 查询历史和结果历史
    private static final List<String> queryHistory = new ArrayList<>();
    private static final List<String> resultHistory = new ArrayList<>();

    // 可能丢失的宝石
    private static Set<String> possibleMissingJewels = new HashSet<>(ALL_JEWELS);

    // 处理逻辑查询
    public static void handleQuery(String formula, ResourceBundle messages) {
        if (!isValidFormula(formula)) { // 检查公式格式是否合法
            System.out.println(messages.getString("invalid_format")); // 格式错误提示
            System.out.println(messages.getString("input_examples")); // 示例提示
            return;
        }

        SMCDELHelper.writeSMCDELFile(formula, messages); // 写入SMCDEL文件
        String output = SMCDELHelper.runSMCDEL(messages); // 调用SMCDEL工具
        queryHistory.add(formula); // 保存查询历史
        resultHistory.add(output); // 保存结果历史

        System.out.println(messages.getString("smcdel_output"));
        System.out.println(output);

        // 解析匹配的状态
        List<Integer> matchedStates = parseMatchingStates(output);
        if (matchedStates.isEmpty() || matchedStates.size() == STATE_TO_JEWEL.size()) {
            System.out.println(messages.getString("no_matching_states"));
        } else {
            interpretAndDisplayOutput(matchedStates, messages); // 解释并显示匹配结果
        }
    }

    // 显示逻辑公式示例
    public static void showFormulaExamples(ResourceBundle messages) {
        System.out.println(messages.getString("input_examples"));
        System.out.println("1. alice knows whether 1");
        System.out.println("2. bob knows that 3 & ~6");
        System.out.println("3. carol knows whether (1 | 4)");
    }

    // 显示查询历史
    public static void showHistory(ResourceBundle messages) {
        System.out.println("\n" + messages.getString("query_history"));
        for (int i = 0; i < queryHistory.size(); i++) {
            System.out.println((i + 1) + ". " + queryHistory.get(i));
            System.out.println("   " + messages.getString("result") + resultHistory.get(i));
        }
    }

    // 显示所有可能的状态
    public static void showAllStates(ResourceBundle messages) {
        System.out.println("\n" + messages.getString("all_states"));
        STATE_TO_JEWEL.forEach((state, jewel) -> System.out.println("- State " + state + ": " + jewel));
    }

    // 显示初始宝石分配
    public static void showInitialDistribution(ResourceBundle messages) {
        System.out.println("\n" + messages.getString("initial_distribution"));
        INITIAL_DISTRIBUTION.forEach(System.out::println);
    }

    // 处理用户猜测
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
            System.out.println(messages.getString("invalid_jewel")); // 无效宝石名称提示
        }
    }

    // 检查逻辑公式格式
    private static boolean isValidFormula(String formula) {
        return formula != null && formula.matches("[a-zA-Z0-9_\\s&|~()]+");
    }

    // 解析匹配的状态
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

    // 显示解析后的匹配结果
    private static void interpretAndDisplayOutput(List<Integer> matchedStates, ResourceBundle messages) {
        System.out.println(messages.getString("matching_states"));
        matchedStates.forEach(state -> System.out.println("- State " + state + ": " + STATE_TO_JEWEL.get(state)));

        // 更新可能丢失的宝石集合
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
