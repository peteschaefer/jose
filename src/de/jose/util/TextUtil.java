package de.jose.util;

import java.text.Normalizer;

public class TextUtil
{
    public static String stripDiacritics(String s) {
        StringBuilder sb = new StringBuilder(s);
        for(int i = 0; i < s.length(); i++)
            sb.setCharAt(i, stripDiacritics(s.charAt(i)));
        return sb.toString();
    }

    public static char stripDiacritics(char c) {
        if (c!=0 && normChars[c]==0)
            normChars[c] = computeNormChar(c);
        return normChars[c];
    }

    private static char computeNormChar(char c) {
        String s = Normalizer.normalize(Character.toString(c), Normalizer.Form.NFD);
        return s.charAt(0);
    }

    private static char[] normChars = new char[Character.MAX_VALUE+1];
}
