package com.eazy.daiku.ui.home

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.eazy.daiku.utility.base.BaseFragment
import com.eazy.daiku.R


/**
 * A simple [Fragment] subclass.
 * Use the [ContactFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContactFragment : BaseFragment() {
    private lateinit var fContext: FragmentActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context as FragmentActivity
    }

    override fun onResume() {
        super.onResume()
        hideShowActionBar(fContext,true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction, container, false)
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
        fun newInstance() = ContactFragment()

    }
}