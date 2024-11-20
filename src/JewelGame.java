import java.util.ResourceBundle;
import java.util.Locale;
import java.util.Scanner;

public class JewelGame {
    private static ResourceBundle messages;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        chooseLanguage(scanner); // 选择语言
        System.out.println(messages.getString("welcome"));
        System.out.println(messages.getString("instructions"));
        System.out.println(messages.getString("input_hint"));
        GameHelper.showFormulaExamples(messages); // 显示逻辑公式示例，指导用户输入

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
                    GameHelper.showHistory(messages);
                    break;
                case "show states":
                case "显示状态":
                    GameHelper.showAllStates(messages);
                    break;
                case "show initial":
                case "显示初始分配":
                    GameHelper.showInitialDistribution(messages);
                    break;
                case "guess":
                case "猜测":
                    GameHelper.handleGuess(scanner, messages);
                    break;
                default:
                    GameHelper.handleQuery(userInput, messages);
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
}
