package com.eazy.daiku.utility

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.eazy.daiku.R

class FormattedNumberEditText : AppCompatEditText {

    var prefix = ""
        private set

    var groupSeparator = ' '

    var numberOfGroups = 4
        private set

    var groupLength = 4

    var inputLength = numberOfGroups * (groupLength + 1) - 1

    private val digitsKeyListener = DigitsKeyListener.getInstance("0123456789")

    private lateinit var separatorAndDigitsKeyListener: DigitsKeyListener

    private var initCompleted = false

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(attrs, R.styleable.FormattedNumberEditText, 0, 0)
            prefix = a.getString(R.styleable.FormattedNumberEditText_prefix) ?: prefix
            val separatorStr = a.getString(R.styleable.FormattedNumberEditText_groupSeparator)
            if (!separatorStr.isNullOrEmpty()) {
                groupSeparator = separatorStr[0]
            }
            numberOfGroups = a.getInteger(R.styleable.FormattedNumberEditText_numberOfGroups, numberOfGroups)
            groupLength = a.getInteger(R.styleable.FormattedNumberEditText_groupLength, groupLength)
        }

        inputLength = numberOfGroups * (groupLength + 1) - 1
        separatorAndDigitsKeyListener = DigitsKeyListener.getInstance("0123456789$groupSeparator")

        setText(prefix)
        setSelection(text!!.length)
        inputType = InputType.TYPE_CLASS_NUMBER
        keyListener = digitsKeyListener
        addTextChangedListener(TextChangeListener())

        initCompleted = true
    }

    override fun onSelectionChanged(start: Int, end: Int) {
        if (!initCompleted) {
            return
        }

        // make sure input always starts with the prefix
        if (!text!!.startsWith(prefix)) {
            setText(prefix)
            setSelection(text!!.length, text!!.length)
            return
        }

        // make sure cursor is always at the end of the string
        if (start != text!!.length || end != text!!.length) {
            setSelection(text!!.length)
        } else {
            super.onSelectionChanged(start, end)
        }
    }

    private inner class TextChangeListener : TextWatcher {

        var textBefore = ""
        var enteredText = ""
        var deletedChars = 0

        var listenerEnabled = true

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            if (!listenerEnabled) return

            textBefore = text.toString()
            enteredText = ""
            deletedChars = 0
        }

        override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
            if (!listenerEnabled) return

            if (text == null) {
                deletedChars = textBefore.length
                return
            }

            if (text.length < textBefore.length) {
                deletedChars = textBefore.length - text.length
                return
            }

            enteredText = text.toString().substring(textBefore.length, text.length)
        }

        override fun afterTextChanged(s: Editable?) {
            if (!listenerEnabled) return

            if (s == null) {
                return
            }

            listenerEnabled = false

            if (deletedChars > 0) {
                handleTextChange(s)
            } else {
                if (enteredText.length > 1) {
                    s.replace(s.length - enteredText.length, s.length, "")

                    // Append one char at a time
                    enteredText.forEach {
                        s.append("$it")
                        handleTextChange(s)
                    }
                } else {
                    handleTextChange(s)
                }
            }

            listenerEnabled = true
        }

        fun handleTextChange(s: Editable) {
            if (s.length > inputLength) {
                while (s.length > inputLength) {
                    s.delete(s.length - 1, s.length)
                }
            } else if (s.isNotEmpty() && s.length % (groupLength + 1) == 0) {
                if (s.last() == groupSeparator) {
                    s.delete(s.length - 1, s.length)
                } else if (s.last().isDigit() && s.length < inputLength) {
                    keyListener = separatorAndDigitsKeyListener
                    s.insert(s.length - 1, groupSeparator.toString())
                    keyListener = digitsKeyListener
                }
            }
        }

    }
}