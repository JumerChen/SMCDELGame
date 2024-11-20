import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ResourceBundle;

public class SMCDELHelper {
    private static final String SMCDEL_PATH = "./smcdel";
    private static final String INPUT_FILE = "Input.smcdel.txt";

    public static void writeSMCDELFile(String formula, ResourceBundle messages) {
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

    public static String runSMCDEL(ResourceBundle messages) {
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
}
