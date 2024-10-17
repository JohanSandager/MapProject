package controller;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import model.DebugConsole;
import model.GlobalConfig;
import model.GlobalConfig.Options;
import view.MapView;
import view.UIView;

public class KeyboardInputController {

    private static Boolean ctrlDown; 
    
    public KeyboardInputController(Canvas canvas, UIView uiView, MapView mapView) {
        ctrlDown = false; 

        canvas.setOnKeyPressed(e -> {
            if(e.getCode().equals(KeyCode.CONTROL)) {
                //if(!ctrlDown) {System.out.println("ctrlDown true");}
                ctrlDown = true; 
            }
        });


        canvas.setOnKeyReleased(e -> {
            if(ctrlDown) {
                if(e.getCode().equals(KeyCode.X)) {
                    Boolean curOption = GlobalConfig.getInstance().getOption(Options.DEBUG_CONSOLE);
                    GlobalConfig.getInstance().setOption(Options.DEBUG_CONSOLE, !curOption);
                    uiView.setConsoleVisibility(!curOption);
                    DebugConsole.getInstance().log("Set Console Visibility to " + !curOption); 
                } else if (e.getCode().equals(KeyCode.V)) {
                    setKeyboardOption(KeyCode.V, Options.DEBUG_OUTLINE, "Set Debug Outline to ");
                } else if (e.getCode().equals(KeyCode.Z)) {
                    setKeyboardOption(KeyCode.Z, Options.DEBUG_ZOOM, "Set Debug Zoom to ");
                } else if (e.getCode().equals(KeyCode.C)) {
                    setKeyboardOption(KeyCode.C, Options.PRINT_DRAW_INFO, "Set Print Draw Info to ");
                } else if (e.getCode().equals(KeyCode.A)) {
                    setKeyboardOption(KeyCode.A, Options.ADRESS_DEBUG_INFO, "Set Print Address Debug Info to ");
                } else if (e.getCode().equals(KeyCode.G)) {
                    setKeyboardOption(KeyCode.G, Options.DRAW_GRAPH, "Set Draw Graph to ");
                } else if (e.getCode().equals(KeyCode.M)) {
                    setKeyboardOption(KeyCode.M, Options.DRAW_MAP, "Set Draw Map to ");
                }
                mapView.draw(); // do a redraw so we dont have to move smth to see change
            }
            if(e.getCode().equals(KeyCode.CONTROL)) {
                //if(ctrlDown) {System.out.println("ctrlDown false");}
                ctrlDown = false; 
            }
        });
    }

    private void setKeyboardOption(KeyCode keyCode, Options optionToSet, String logMsg) {
        boolean curOption = GlobalConfig.getInstance().getOption(optionToSet); 
        GlobalConfig.getInstance().setOption(optionToSet, !curOption);
        DebugConsole.getInstance().log(logMsg + !curOption);
    }
}
