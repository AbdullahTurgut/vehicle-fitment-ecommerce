package com.carmats.catalog.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SlugUtils {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern MULTI_HYPHEN = Pattern.compile("-+");

    private SlugUtils() {
    }

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        String normalized = input.trim()
                .replace("ı", "i")
                .replace("İ", "i")
                .replace("ş", "s")
                .replace("Ş", "s")
                .replace("ğ", "g")
                .replace("Ğ", "g")
                .replace("ü", "u")
                .replace("Ü", "u")
                .replace("ö", "o")
                .replace("Ö", "o")
                .replace("ç", "c")
                .replace("Ç", "c");

        String noWhitespace = WHITESPACE.matcher(normalized).replaceAll("-");
        String decomposed = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD);
        String slug = NON_LATIN.matcher(decomposed).replaceAll("");
        slug = MULTI_HYPHEN.matcher(slug).replaceAll("-");

        return slug.toLowerCase(Locale.ENGLISH)
                .replaceAll("^-", "")
                .replaceAll("-$", "");
    }
}
