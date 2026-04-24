package com.periplus.utils;

public final class Money {
    private Money() {
    }

    public static long rupiahValue(String text) {
        if (text == null) {
            return 0;
        }

        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return 0;
        }
        return Long.parseLong(digits);
    }
}
