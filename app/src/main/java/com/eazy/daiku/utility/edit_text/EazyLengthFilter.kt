package com.eazy.daiku.utility.edit_text

import android.text.InputFilter
import android.text.Spanned

class EazyLengthFilter(int: Int) : InputFilter {
    private var mMax = int


    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int,
    ): CharSequence? {
        var keep = mMax - (dest.length - (dend - dstart))
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

*/return if (keep <= 0) {
            ""
        } else if (keep >= end - start) {
            null // keep original
        } else {
            keep += start
            if (Character.isHighSurrogate(source[keep - 1])) {
                --keep
                if (keep == start) {
                    return ""
                }
            }
            source.subSequence(start, keep)
        }
    }

    /**
     * @return the maximum length enforced by this input filter
     */
    fun getMax(): Int {
        return mMax
    }

    fun setMax(max: Int) {
        mMax = max
    }
}