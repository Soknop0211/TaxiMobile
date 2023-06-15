package com.eazy.daiku.ui.customer.step_booking

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.eazy.daiku.R
import com.eazy.daiku.data.model.server_model.VehicleTypeRespond
import com.eazy.daiku.databinding.StepBookingFragmentLayoutBinding
import com.eazy.daiku.utility.base.BaseBottomSheetDialogFragment
import com.google.gson.Gson

class StepBookingFragmentBottomSheet : BaseBottomSheetDialogFragment() {
    private lateinit var binding: StepBookingFragmentLayoutBinding
    private lateinit var fContext: FragmentActivity
    private lateinit var navController: NavController
    private lateinit var stepBookingListener: (Boolean) -> Unit
    private var myListTaxi: ArrayList<VehicleTypeRespond>? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        fContext = context as FragmentActivity
    }


    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(
            listTaxi: (ArrayList<VehicleTypeRespond>)? = null,
            stepBookingListener: (Boolean) -> Unit,
        ) =
            StepBookingFragmentBottomSheet().apply {
                this.stepBookingListener = stepBookingListener
                this.myListTaxi = listTaxi
            }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = StepBookingFragmentLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initAction()
    }

    private fun initView() {
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment?
        navHostFragment?.let {
            navController = it.navController
            val bundle = Bundle()
            //val gson = Gson()
            // val myListTaxi = gson.toJson(myListTaxi)
            bundle.putSerializable(SelectCarBookingFragment.myListTaxiKey, myListTaxi)

            navController.setGraph(R.navigation.booking_payment_navigation_layout, bundle)
            val graph = navController.graph
            graph.startDestination = R.id.selectCarBookingFragment
        }

    }

    private fun initAction() {

    }

    override fun getTheme(): Int {
        return R.style.TopRoundCornerBottomSheetDialogTheme
    }
}