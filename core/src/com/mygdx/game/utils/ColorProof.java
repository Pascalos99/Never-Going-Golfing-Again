package com.mygdx.game.utils;

import com.badlogic.gdx.graphics.Color;

public class ColorProof {
    public static ColorProof WHITE      = of(Color.WHITE, Color.WHITE);
    public static ColorProof CYAN       = of(Color.CYAN, Color.SKY);
    public static ColorProof BLUE       = of(Color.BLUE, Color.BLUE);
    public static ColorProof YELLOW     = of(Color.YELLOW, Color.YELLOW);
    public static ColorProof PINK       = of(Color.PINK, Color.LIGHT_GRAY);
    public static ColorProof RED        = of(Color.RED, Color.GRAY);
    public static ColorProof PURPLE     = of(Color.PURPLE, Color.NAVY);
    public static ColorProof BLACK      = of(Color.BLACK, Color.BLACK);
    public static ColorProof BROWN      = of(Color.BROWN, Color.BLACK);

    public static ColorProof ARROW      = of(Color.YELLOW, Color.SKY);
    public static ColorProof FLAG      = of(Color.RED, Color.WHITE);
    public static ColorProof RANGE      = of(new Color(1, 0.4f, 1, 1f), new Color(1, 1f, 0.4f, 1f));
    public static ColorProof SAND       = of(new Color(0.7f, 0.7f, 0f, 1f), Color.YELLOW);
    public static ColorProof WATER      = of(new Color(0.2f, 0.2f, 1f, 1f), new Color(0.2f, 0.2f, 1f, 1f));

    public static boolean COLOR_BLIND_MODE = false;

    public static Color WHITE() { return WHITE.get();}
    public static Color CYAN() { return CYAN.get(); }
    public static Color BLUE() { return BLUE.get(); }
    public static Color YELLOW() { return YELLOW.get(); }
    public static Color PINK() { return PINK.get(); }
    public static Color RED() { return RED.get(); }
    public static Color PURPLE() { return PURPLE.get(); }
    public static Color BLACK() { return BLACK.get(); }
    public static Color BROWN() { return BROWN.get(); }
    public static Color SHOT_ARROW() { return ARROW.get(); }
    public static Color POLE_RANGE() { return RANGE.get(); }
    public static Color SAND() { return SAND.get(); }
    public static Color WATER() { return WATER.get(); }
    public static Color FLAG() { return FLAG.get(); }

    public static ColorProof of(Color norm, Color adjust) {
        return new ColorProof(norm, adjust);
    }

    public final Color normal;
    public final Color deuteranopia;
    public ColorProof(Color normal, Color color_blind_proof) {
        this.normal = normal;
        this.deuteranopia = color_blind_proof;
    }
    public Color get(boolean color_blind) {
        if (color_blind) return deuteranopia;
        else return normal;
    }
    public Color get() {
        return get(COLOR_BLIND_MODE);
    }

}
