package model;

import javafx.scene.control.TextArea;
import model.GlobalConfig.Options;

public class DebugConsole {

    private static DebugConsole console;

    private TextArea textArea;

    private DebugConsole() {
        textArea = new TextArea();
    }

    public static DebugConsole getInstance() {
        if (console == null) {
            console = new DebugConsole();
        }
        return console;
    }

    public void log(String msg) {
        System.out.println(msg);
        // textArea.appendText(msg + "\n");
    }

    public void logIfOption(Options option, String msg) {
        if (!GlobalConfig.getInstance().getOption(option)) {
            return;
        }
        log(msg);
    }

    public TextArea getTextArea() {
        return textArea;
    }

}
