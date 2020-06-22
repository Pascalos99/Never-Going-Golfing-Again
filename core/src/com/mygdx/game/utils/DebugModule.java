package com.mygdx.game.utils;

import java.util.*;

public class DebugModule {

    public static DebugModule get(String name, boolean enabled) {
        DebugModule d = new DebugModule(name);
        d.setEnabled(enabled);
        return d;
    }

    public DebugModule(String code_name) {
        this.code_name = "["+code_name.toUpperCase()+"]";
        chapters = new HashMap<>();
        chapter_order = new Stack<>();
        startChapter("unspecified", false);
    }

    private String code_name;
    private Map<String, Map<String, TimeTrack>> chapters;
    private boolean DEBUG;
    private Stack<Chapter> chapter_order;

    private void startChapter(String chapter, boolean abide_debug_setting) {
        if (!DEBUG && abide_debug_setting) return;
        debug("CHAPTER %s running:...",chapter);
        chapter_order.push(new Chapter(chapter));
        chapters.put(chapter, new HashMap<>());
    }
    public void startChapter(String chapter) {
        startChapter(chapter, true);
    }
    public void endChapter() {
        if (!DEBUG) return;
        Chapter ch = chapter_order.pop();
        long elapsed = ch.elapsed_time();
        String chapter = ch.name;
        if (!chapters.containsKey(chapter)) return;
        Map<String, TimeTrack> times = chapters.get(chapter);
        debug("CHAPTER %s took a total of %.3f ms:", chapter, elapsed/1000000d);
        for (String section : times.keySet()) debug("  Section %s took a total of %.3f ms", section, times.get(section).total_time/1000000d);
        chapters.remove(chapter);
        if (chapter_order.size() <= 0) startChapter("unspecified");
    }
    public void startSection(String name) {
        if (!DEBUG) return;
        Map<String, TimeTrack> times = chapters.get(chapter_order.peek().name);
        TimeTrack tt = times.get(name);
        if (tt ==  null) times.put(name, new TimeTrack());
        else tt.start();
    }
    public void endSection(String name) {
        if (!DEBUG) return;
        Map<String, TimeTrack> times = chapters.get(chapter_order.peek().name);
        TimeTrack tt = times.get(name);
        if (tt != null) tt.end();
    }
    public void debug(String str) {
        if (DEBUG) System.out.println(code_name+": "+str);
    }
    public void debug(String format, Object... parameters) {
        debug(String.format(format, parameters));
    }
    public void setEnabled(boolean enable) {
        DEBUG = enable;
    }
    public void enable() {
        setEnabled(true);
    }
    public void disable() {
        setEnabled(false);
    }
    public boolean isEnabled() {
        return DEBUG;
    }

    private static class TimeTrack {
        public long total_time;
        public long last_time;
        public void start() { last_time = System.nanoTime(); }
        public void end() { total_time += System.nanoTime() - last_time; }
        public TimeTrack() {total_time = 0; last_time = System.nanoTime();}
    }
    private static class Chapter {
        public final long start_time;
        public final String name;
        public Chapter(String name) { this.name = name; start_time = System.nanoTime(); }
        public long elapsed_time() { return System.nanoTime() - start_time; }
    }
}
