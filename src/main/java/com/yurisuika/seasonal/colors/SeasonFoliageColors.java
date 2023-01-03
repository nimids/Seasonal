package com.yurisuika.seasonal.colors;

import com.yurisuika.seasonal.Seasonal;
import com.yurisuika.seasonal.utils.Season;

public class SeasonFoliageColors {

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
        return switch(season){
            case EARLY_SPRING -> earlySpringColorMap[j << 8 | i];
            case MID_SPRING -> midSpringColorMap[j << 8 | i];
            case LATE_SPRING -> lateSpringColorMap[j << 8 | i];
            case EARLY_SUMMER -> earlySummerColorMap[j << 8 | i];
            case MID_SUMMER -> midSummerColorMap[j << 8 | i];
            case LATE_SUMMER -> lateSummerColorMap[j << 8 | i];
            case EARLY_AUTUMN -> earlyAutumnColorMap[j << 8 | i];
            case MID_AUTUMN -> midAutumnColorMap[j << 8 | i];
            case LATE_AUTUMN -> lateAutumnColorMap[j << 8 | i];
            case EARLY_WINTER -> earlyWinterColorMap[j << 8 | i];
            case MID_WINTER -> midWinterColorMap[j << 8 | i];
            case LATE_WINTER -> lateWinterColorMap[j << 8 | i];
        };
    }

    public static int getSpruceColor(Season season) {
        return Seasonal.CONFIG.getMinecraftSpruceFoliage().getColor(season);
    }

    public static int getBirchColor(Season season) {
        return Seasonal.CONFIG.getMinecraftBirchFoliage().getColor(season);
    }

    public static int getDefaultColor(Season season) {
        return Seasonal.CONFIG.getMinecraftDefaultFoliage().getColor(season);
    }

}
