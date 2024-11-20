import java.util.ResourceBundle;
import java.util.Locale;
import java.util.Scanner;

public class JewelGame {
    private static ResourceBundle messages; // 多语言资源文件

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        chooseLanguage(scanner); // 选择语言
        System.out.println(messages.getString("welcome")); // 欢迎信息
        System.out.println(messages.getString("instructions")); // 游戏说明

        while (true) {
            System.out.println(messages.getString("input_hint")); // 输入提示
            GameHelper.showFormulaExamples(messages); // 每次输入时都显示逻辑公式示例

            if (!scanner.hasNextLine()) {
                break;
            }
            String userInput = scanner.nextLine().trim();

            // 支持用户通过"exit"或"退出"结束游戏
            if ("exit".equalsIgnoreCase(userInput) || "退出".equalsIgnoreCase(userInput)) {
                System.out.println(messages.getString("goodbye")); // 退出信息
                break;
            }

            // 根据用户输入选择相应操作
            switch (userInput.toLowerCase()) {
                case "history":
                case "历史记录":
                    GameHelper.showHistory(messages); // 显示历史记录
                    break;
                case "show states":
                case "显示状态":
                    GameHelper.showAllStates(messages); // 显示所有可能状态
                    break;
                case "show initial":
                case "显示初始分配":
                    GameHelper.showInitialDistribution(messages); // 显示初始宝石分配
                    break;
                case "guess":
                case "猜测":
                    GameHelper.handleGuess(scanner, messages); // 用户猜测操作
                    break;
                default:
                    GameHelper.handleQuery(userInput, messages); // 处理逻辑查询
                    break;
            }
        }
    }

    // 选择语言（英文或中文）
    private static void chooseLanguage(Scanner scanner) {
        System.out.println("Choose your language / 请选择你的语言:");
        System.out.println("1. English");
        System.out.println("2. 中文 (Chinese)");
        String choice = scanner.nextLine().trim();
        switch (choice.toLowerCase()) {
            case "2":
            case "中文":
            case "chinese":
                messages = ResourceBundle.getBundle("MessagesBundle", new Locale("zh", "CN")); // 中文资源
                break;
            case "1":
            case "english":
            default:
                messages = ResourceBundle.getBundle("MessagesBundle", new Locale("en", "US")); // 英文资源
                break;
        }
    }
}
