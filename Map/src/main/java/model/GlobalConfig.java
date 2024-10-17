package model;

import javafx.scene.paint.Color;

public class GlobalConfig {
    public enum Options {
        DEBUG_OUTLINE, 
        DEBUG_ZOOM, 
        DEBUG_CONSOLE, 
        DISPLAY_FPS, 
        PRINT_DRAW_INFO, 
        ADRESS_DEBUG_INFO, 
        DRAW_GRAPH, 
        DRAW_MAP,
        TOGGLE_DARKMODE
    }
    public enum Constant {
        MAX_AMT_ADDRESSES
    }

    public enum BackgroundColor {
        CANVAS_BACKGROUND
    }
    private Boolean[] config;
    private int[] constants;
    private static GlobalConfig globalConfig;
    private Color[] colors;

    private GlobalConfig() {
        config = new Boolean[Options.values().length];
        config[Options.DEBUG_CONSOLE.ordinal()] = false;
        config[Options.DEBUG_ZOOM.ordinal()] = false;
        config[Options.DEBUG_OUTLINE.ordinal()] = false;
        config[Options.DISPLAY_FPS.ordinal()] = true;
        config[Options.PRINT_DRAW_INFO.ordinal()] = false;
        config[Options.ADRESS_DEBUG_INFO.ordinal()] = true;
        config[Options.DRAW_GRAPH.ordinal()] = false;
        config[Options.DRAW_MAP.ordinal()] = true;
        config[Options.TOGGLE_DARKMODE.ordinal()] = true;
        constants = new int[Constant.values().length];
        constants[Constant.MAX_AMT_ADDRESSES.ordinal()] = 8;
        colors = new Color[2];
        colors[BackgroundColor.CANVAS_BACKGROUND.ordinal()] = Color.rgb(50, 50, 50);
    }

    public static GlobalConfig getInstance() {
        if(globalConfig == null) {
            globalConfig = new GlobalConfig(); 
        }
        return globalConfig; 
    }

    public Boolean getOption(Options index) {
        return config[index.ordinal()];
    }

    public int getConstant(Constant constant) {
        return constants[constant.ordinal()];
    }

    public Color getBackgroundColor(BackgroundColor color) {
        return colors[color.ordinal()];
    }

    public void setOption(Options option, Boolean value) {
        config[option.ordinal()] = value; 
    }

    public void setBackgroundColor(BackgroundColor bgColor, Color color) {
        colors[bgColor.ordinal()] = color;
    }
}
