package com.eazy.daiku.utility.edit_text;

import android.text.InputFilter;
import android.text.Spanned;

public class KESSLengthFilter implements InputFilter {

    private int mMax;

    public KESSLengthFilter(int max) {
        mMax = max;
    }

    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        int keep = mMax - (dest.length() - (dend - dstart));
/*

        int countComma = 0;
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == ',') {
                countComma += 1;
            }
        }

        if (countComma > 0) keep += countComma + 1;

        KESSLOG.d("filter_length_keep_c", ": " + countComma + " keep:" + keep);
*/

/*

        source = source.toString().replaceAll("[,]", "");

*/

        if (keep <= 0) {
            return "";
        } else if (keep >= end - start) {
            return null; // keep original
        } else {
            keep += start;
            if (Character.isHighSurrogate(source.charAt(keep - 1))) {
                --keep;
                if (keep == start) {
                    return "";
                }
            }
            return source.subSequence(start, keep);
        }
    }

    /**
     * @return the maximum length enforced by this input filter
     */
    public int getMax() {
        return mMax;
    }

    public void setMax(int max) {
        this.mMax = max;
    }
}
