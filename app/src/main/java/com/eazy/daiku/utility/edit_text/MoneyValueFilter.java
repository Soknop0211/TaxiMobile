package com.eazy.daiku.utility.edit_text;

import android.annotation.SuppressLint;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.DigitsKeyListener;

import androidx.annotation.RequiresApi;

import java.util.Locale;


public class MoneyValueFilter extends DigitsKeyListener {

    @SuppressLint("ConstantLocale")
    private static final Locale locale = Locale.getDefault();

    public static MoneyValueFilter newInstance() {
        return newInstance(2);
    }

    public static MoneyValueFilter newInstance(int digits) {
        MoneyValueFilter fragment;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fragment = new MoneyValueFilter(digits, locale);
        } else {
            fragment = new MoneyValueFilter(digits);
        }
        return fragment;
    }

    private int digits;
    private final int maxLength = 6;
    private final double maxValue = 1000;
    private final double minValue = 0;

    public void setDigits(int d) {
        digits = d;
    }

    public MoneyValueFilter(int digits) {
        super(false, true);
        this.digits = digits;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private MoneyValueFilter(int digits, Locale locale) {
        super(locale, false, true);
        this.digits = digits;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

        /*CharSequence out = super.filter(source, start, end, dest, dstart, dend);
        KESSLOG.d("filter", String.format("source: %s; start: %s; end: %s ;dest: %s ;dstart: %s ;dend: %s", source, start, end, dest, dstart, dend));

        // if changed, replace the source
        if (out != null) {
            source = out;
            start = 0;
            end = out.length();
        }*/

        int len = end - start;

        // if deleting, source is empty
        // and deleting can't break anything
        if (len == 0) {
            return source;
        }

        if (source.charAt(0) == '.' && dest.length() == 0) {
            return "0.";
        }

        if (source.charAt(0) == '0' && dest.length() == 1 && dest.charAt(0) == '0') {
            return "";
        }

        if (source.charAt(0) != '0' && dest.length() == 1 && dest.charAt(0) == '0') {
//            return "";
        }

        int dlen = dest.length();

        // Find the position of the decimal .
        for (int i = 0; i < dstart; i++) {
            if (dest.charAt(i) == '.') {
                // being here means, that a number has
                // been inserted after the dot
                // check if the amount of digits is right
                return (dlen - (i + 1) + len > digits) ? "" : new SpannableStringBuilder(source, start, end);
            }
        }

        for (int i = start; i < end; ++i) {
            if (source.charAt(i) == '.') {
                // being here means, dot has been inserted
                // check if the amount of digits is right
                if ((dlen - dend) + (end - (i + 1)) > digits)
                    return "";
                else
                    break;  // return new SpannableStringBuilder(source, start, end);
            }
        }

        // if the dot is after the inserted part,
        // nothing can break
        return new SpannableStringBuilder(source, start, end);
    }

}
