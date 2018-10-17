package com.grail.util;

import java.text.DecimalFormat;

/**
 *
 */
public class FileSizeUtils {
    /**
     *
     * @param value(in Bytes)
     * @return
     */
    public static String format(final Long value) {
        if((value / (1024 * 1024 * 1024 * 1.0)) >= 1) {
            return formatGigaBytes(value);
        } else if((value / (1024 *  1024 * 1.0)) >= 1) {
            return formatMegaBytes(value);
        } else if((value / (1024 * 1.0)) >= 1) {
           return formatKiloBytes(value);
        } else {
            return formatBytes(value);
        }
    }

    public static String formatBytes(final Long value) {
        StringBuilder str = new StringBuilder();
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        str.append(Double.valueOf(twoDForm.format(value)).toString()).append(" B");
        return str.toString();
    }

    /**
     * Format to Kilobytes
     * @param value
     * @return
     */
    public static String formatKiloBytes(final Long value) {
        StringBuilder str = new StringBuilder();
        str.append(toKiloBytes(value)).append(" KB");
        return str.toString();
    }

    /**
     * Format to Megabytes
     * @param value (in Bytes)
     * Convert to Gigabytes
     * @return
     */
    public static String formatMegaBytes(final Long value) {
        StringBuilder str = new StringBuilder();
        str.append(toMegaBytes(value)).append(" MB");
        return str.toString();
    }

    /**
     * Format to Gigabytes
     * @param value (in Bytes)
     * @return
     */
    public static String formatGigaBytes(final Long value) {
        StringBuilder str = new StringBuilder();
        str.append(toGigaBytes(value)).append(" GB");
        return str.toString();
    }

    /**
     * Convert to Kilobytes
     * @param value
     * @return
     */
    public static Double toKiloBytes(final Long value) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(value/(1024 * 1.0)));
    }

    /**
     * Convert to Megabytes
     * @param value
     * @return
     */
    public static Double toMegaBytes(final Long value) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(value/(1024 * 1024 * 1.0)));
    }

    /**
     * Convert to Gigabytes
     * @param value
     * @return
     */
    public static Double toGigaBytes(final Long value) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(value/(1024 * 1024 * 1024 * 1.0)));
    }
}
