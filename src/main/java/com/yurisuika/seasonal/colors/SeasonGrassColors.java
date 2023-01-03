package com.yurisuika.seasonal.colors;

import com.yurisuika.seasonal.utils.Season;

public class SeasonGrassColors {

    private static int[] earlySpringColorMap = new int[65536];
    private static int[] midSpringColorMap = new int[65536];
    private static int[] lateSpringColorMap = new int[65536];
    private static int[] earlySummerColorMap = new int[65536];
    private static int[] midSummerColorMap = new int[65536];
    private static int[] lateSummerColorMap = new int[65536];
    private static int[] earlyAutumnColorMap = new int[65536];
    private static int[] midAutumnColorMap = new int[65536];
    private static int[] lateAutumnColorMap = new int[65536];
    private static int[] earlyWinterColorMap = new int[65536];
    private static int[] midWinterColorMap = new int[65536];
    private static int[] lateWinterColorMap = new int[65536];

    public static void setColorMap(Season season, int[] pixels) {
        switch(season){
            case EARLY_SPRING -> earlySpringColorMap = pixels;
            case MID_SPRING -> midSpringColorMap = pixels;
            case LATE_SPRING -> lateSpringColorMap = pixels;
            case EARLY_SUMMER -> earlySummerColorMap = pixels;
            case MID_SUMMER -> midSummerColorMap = pixels;
            case LATE_SUMMER -> lateSummerColorMap = pixels;
            case EARLY_AUTUMN -> earlyAutumnColorMap = pixels;
            case MID_AUTUMN -> midAutumnColorMap = pixels;
            case LATE_AUTUMN -> lateAutumnColorMap = pixels;
            case EARLY_WINTER -> earlyWinterColorMap = pixels;
            case MID_WINTER -> midWinterColorMap = pixels;
            case LATE_WINTER -> lateWinterColorMap = pixels;
        }
    }

    public static int getColor(Season season, double temperature, double humidity) {
        humidity *= temperature;
        int i = (int)((1.0D - temperature) * 255.0D);
        int j = (int)((1.0D - humidity) * 255.0D);
        int k = j << 8 | i;
        return switch(season){
            case EARLY_SPRING -> k > earlySpringColorMap.length ? -65281 : earlySpringColorMap[k];
            case MID_SPRING -> k > midSpringColorMap.length ? -65281 : midSpringColorMap[k];
            case LATE_SPRING -> k > lateSpringColorMap.length ? -65281 : lateSpringColorMap[k];
            case EARLY_SUMMER -> k > earlySummerColorMap.length ? -65281 : earlySummerColorMap[k];
            case MID_SUMMER -> k > midSummerColorMap.length ? -65281 : midSummerColorMap[k];
            case LATE_SUMMER -> k > lateSummerColorMap.length ? -65281 : lateSummerColorMap[k];
            case EARLY_AUTUMN -> k > earlyAutumnColorMap.length ? -65281 : earlyAutumnColorMap[k];
            case MID_AUTUMN -> k > midAutumnColorMap.length ? -65281 : midAutumnColorMap[k];
            case LATE_AUTUMN -> k > lateAutumnColorMap.length ? -65281 : lateAutumnColorMap[k];
            case EARLY_WINTER -> k > earlyWinterColorMap.length ? -65281 : earlyWinterColorMap[k];
            case MID_WINTER -> k > midWinterColorMap.length ? -65281 : midWinterColorMap[k];
            case LATE_WINTER -> k > lateWinterColorMap.length ? -65281 : lateWinterColorMap[k];
        };
    }
}
