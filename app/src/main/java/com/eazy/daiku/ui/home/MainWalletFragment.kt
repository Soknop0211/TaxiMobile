package com.eazy.daiku.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.eazy.daiku.utility.base.BaseFragment
import com.eazy.daiku.utility.redirect.RedirectClass
import com.eazy.daiku.R
import de.hdodenhof.circleimageview.CircleImageView

/**
 * A simple [Fragment] subclass.
 * Use the [MainWalletFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainWalletFragment : BaseFragment() {

    private lateinit var actionProfileImg: CircleImageView
    private lateinit var fContext: FragmentActivity


    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context as FragmentActivity
    }

    override fun onResume() {
        super.onResume()
        hideShowActionBar(fContext,false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        initAction()
    }


    private fun initView(view: View) {
        actionProfileImg = view.findViewById(R.id.action_profile_img)

    }

    private fun initAction() {
        actionProfileImg.setOnClickListener {
            RedirectClass.gotoProfileActivity(fContext)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainWalletFragment()
    }
}