package com.eazy.daiku.utility.edit_text;

import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

import java.util.StringTokenizer;

public class KESSMoneyEditText extends AppCompatEditText implements TextWatcher {
    private MoneyValueFilter moneyValueFilter;
    private InputFilter lengthFilter;
    private TextWatcher textWatcher;
    private Context context;

    public KESSMoneyEditText(Context context) {
        super(context);
        this.context = context;
        setupListeners();
    }

    public KESSMoneyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupListeners();
        this.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    public KESSMoneyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupListeners();
    }

    private void setupListeners() {
        moneyValueFilter = MoneyValueFilter.newInstance();
        lengthFilter = new KESSLengthFilter(12);

        setFilters(new InputFilter[]{moneyValueFilter, lengthFilter});
        addTextChangedListener(this);
    }

    public void setDecimalDigits(int digits) {
        if (moneyValueFilter != null) {
            moneyValueFilter.setDigits(digits);
        }
    }

    public TextWatcher getTextWatcher() {
        return textWatcher;
    }

    public void setTextWatcher(TextWatcher textWatcher) {
        this.textWatcher = textWatcher;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            this.addTextChangedListener(this);
        } else {
            this.removeTextChangedListener(this);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (textWatcher != null) textWatcher.beforeTextChanged(s, start, count, after);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (textWatcher != null) textWatcher.onTextChanged(s, start, before, count);
    }

    @Override
    public void afterTextChanged(Editable s) {
        new Thread(() -> {
            /*removeTextChangedListener(this);
            try {

                Editable editable = getText();
                if (editable != null && !editable.toString().equals("")) {

                    String value = editable.toString();
                    if (value.startsWith(".")) {
                        setText("0.");
                    }

                    if (value.startsWith("0") && value.length() == 2) {
                        value = value.substring(1, 2);
                        setText(value);
                    }

                    try {
                        String str = getText().toString().contains(",") ? getText().toString().replaceAll(",", "") : getText().toString();
                        if (!value.equals("")) setText(getDecimalFormattedString(str));
                        setSelection(getText().toString().length());
                    } catch (ArrayIndexOutOfBoundsException ignored) {
                    }

                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            addTextChangedListener(this);*/
            if (textWatcher != null) textWatcher.afterTextChanged(s);

        }).start();
    }

    private String getDecimalFormattedString(String value) {
        StringTokenizer lst = new StringTokenizer(value, ".");
        String str1 = value;
        String str2 = "";
        if (lst.countTokens() > 1) {
            str1 = lst.nextToken();
            str2 = lst.nextToken();
        }
        StringBuilder str3 = new StringBuilder();
        int i = 0;
        int j = -1 + str1.length();
        if (str1.charAt(-1 + str1.length()) == '.') {
            j--;
            str3 = new StringBuilder(".");
        }
        for (int k = j; ; k--) {
            if (k < 0) {
                if (str2.length() > 0)
                    str3.append(".").append(str2);
                return str3.toString();
            }
            if (i == 3) {
                str3.insert(0, ",");
                i = 0;
            }
            str3.insert(0, str1.charAt(k));
            i++;
        }

    }

}
