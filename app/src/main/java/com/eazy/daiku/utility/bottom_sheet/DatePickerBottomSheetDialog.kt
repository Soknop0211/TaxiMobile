package com.eazy.daiku.utility.bottom_sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.eazy.daiku.R
import com.eazy.daiku.utility.base.BaseBottomSheetDialogFragment
import com.eazy.daiku.utility.call_back.SelectDateListener
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker
import com.google.android.material.button.MaterialButton
import java.util.*


class DatePickerBottomSheetDialog : BaseBottomSheetDialogFragment() {

    private var currentSelectDate: Date? = null
    private var title: String? = null
    private var selectDateListener: SelectDateListener? = null

    private lateinit var selectDatePicker: SingleDateAndTimePicker
    private lateinit var titleTv: TextView
    private lateinit var actionOkMtb: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.date_picker_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initAction()
    }

    private fun initAction() {
        actionOkMtb.setOnClickListener {
            selectDateListener?.dateCallBack(selectDatePicker.date)
            dismiss()
        }
    }

    private fun initView(view: View) {
        titleTv = view.findViewById(R.id.titlePickerDate)
        selectDatePicker = view.findViewById(R.id.select_date_time_picker)
        actionOkMtb = view.findViewById(R.id.action_ok)

        selectDatePicker.setDisplayMinutes(false)
        selectDatePicker.setDisplayHours(false)
        selectDatePicker.setDisplayDays(false)
        selectDatePicker.setDisplayDays(false)
        selectDatePicker.setDisplayMonths(true)
        selectDatePicker.setDisplayYears(true)
        selectDatePicker.setDisplayDaysOfMonth(true)
        selectDatePicker.setDefaultDate(currentSelectDate)
        titleTv.text = "$title"

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TransactionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(
            title: String = "Select Date",
            currentSelectDate: Date? = Date(),
            selectDateListener: SelectDateListener
        ) =
            DatePickerBottomSheetDialog().apply {
                this.title = title
                this.currentSelectDate = currentSelectDate
                this.selectDateListener = selectDateListener
            }

    }


}