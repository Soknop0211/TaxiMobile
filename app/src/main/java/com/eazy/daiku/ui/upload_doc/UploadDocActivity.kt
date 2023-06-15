package com.eazy.daiku.ui.upload_doc

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.custom.MessageUtils
import com.eazy.daiku.utility.permission_media.PermissionHelper
import com.eazy.daiku.utility.redirect.RedirectClass
import com.eazy.daiku.utility.view_model.uplaod_doc_vm.UploadDocVM
import com.eazy.daiku.R
import com.eazy.daiku.data.model.server_model.User
import com.eazy.daiku.databinding.ActivityUploadDocBinding
import com.eazy.daiku.utility.*
import com.eazy.daiku.utility.adapter.VehicleSelectAdapter
import com.eazy.daiku.utility.base.SimpleBaseActivity
import com.eazy.daiku.utility.pagin.community_taxi.CommunityTaxiPaginAdapter
import com.eazy.daiku.utility.view_model.community.CommunityVm
import com.fxn.pix.Pix
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import java.util.*

class UploadDocActivity : SimpleBaseActivity() {
    private lateinit var binding: ActivityUploadDocBinding
    private lateinit var vehicleAdapter: VehicleSelectAdapter
    private lateinit var communityTaxiAdapter: CommunityTaxiPaginAdapter

    private var docKycTemporary: HashMap<String, Any> = HashMap<String, Any>()
    private var selectVehicleTypeId: Int = -1
    private var hasBiometric: String? = null

    //private var selectCommunityTaxiId: Int = -1
    private val uploadDocVm: UploadDocVM by viewModels {
        factory
    }
    private val communityTaxi: CommunityVm by viewModels {
        factory
    }
    private val communityTaxiTemporaryRequestOneTimeVm: CommunityVm by viewModels {
        factory
    }

    companion object {
        const val reUploadKycKey = "reUploadKycKey"
        const val RequestBodyKey = "request_body_key"
        const val hasKycDraftKey = "hasKycDraftKey"
        const val RequestIdCardCode = 167
        const val RequestSelfieProfileCode = 168
        const val RequestVehiclePictureCode = 169
        const val RequestPlateNumberCode = 170
        const val TAG = "UploadDocActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadDocBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intiView()
        initObserved()
        doAction()
        initDataSever()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RequestSelfieProfileCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    val returnValue: ArrayList<String> =
                        data!!.getStringArrayListExtra(Pix.IMAGE_RESULTS) as ArrayList<String>
                    if (returnValue.size > 0) {
                        convertAsBitmap(binding.selfiePictureImg, returnValue[0])
                        docKycTemporary[Config.KycDocKey.selfie] = returnValue[0]
                        binding.addSelfiePictureImg.visibility = View.GONE
                        binding.selfiePictureImg.alpha = 1f
                    }
                }
            }
            RequestIdCardCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    val returnValue: ArrayList<String> =
                        data!!.getStringArrayListExtra(Pix.IMAGE_RESULTS) as ArrayList<String>
                    if (returnValue.size > 0) {
                        docKycTemporary[Config.KycDocKey.idCard] = returnValue[0]
                        convertAsBitmap(binding.idCardImg, returnValue[0])
                        binding.addIdCardImg.visibility = View.GONE
                        binding.idCardImg.alpha = 1f
                    }
                }
            }
            RequestVehiclePictureCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    val returnValue: ArrayList<String> =
                        data!!.getStringArrayListExtra(Pix.IMAGE_RESULTS) as ArrayList<String>
                    if (returnValue.size > 0) {
                        docKycTemporary[Config.KycDocKey.vehiclePicture] = returnValue[0]
                        convertAsBitmap(binding.vehiclePictureImg, returnValue[0])
                        binding.addSelectVehicleImg.visibility = View.GONE
                        binding.vehiclePictureImg.alpha = 1f
                    }
                }
            }
            RequestPlateNumberCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    val returnValue: ArrayList<String> =
                        data!!.getStringArrayListExtra(Pix.IMAGE_RESULTS) as ArrayList<String>
                    if (returnValue.size > 0) {
                        docKycTemporary[Config.KycDocKey.plateNumberImg] = returnValue[0]
                        convertAsBitmap(binding.plateNumberImg, returnValue[0])
                        binding.addPlateNumberImg.visibility = View.GONE
                        binding.plateNumberImg.alpha = 1f
                    }
                }
            }
        }
    }

    private fun intiView() {

        val dpHeightSelfieProfile = ImageHelper.getWidthScreen(self()) * 0.50
        val dpHeightIDCard = ImageHelper.getWidthScreen(self()) * 0.48
        val dpHeightPlateNumber = ImageHelper.getWidthScreen(self()) * 0.66
        val dpHeightVehiclePicture = ImageHelper.getWidthScreen(self()) * 0.52

        binding.idCardImg.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            dpHeightIDCard.toInt()
        )

        binding.plateNumberImg.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            dpHeightPlateNumber.toInt()
        )

        binding.vehiclePictureImg.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            dpHeightVehiclePicture.toInt()
        )

        binding.selfiePictureImg.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            dpHeightSelfieProfile.toInt()
        )
        vehicleAdapter = VehicleSelectAdapter()
        binding.vehicleSelectRecyclerView.apply {
            layoutManager = GridLayoutManager(
                self(),
                2
            )
            adapter = vehicleAdapter
        }

        //select temporary image
        if (intent.hasExtra(hasKycDraftKey)) {
            docKycTemporary =
                intent.getSerializableExtra(hasKycDraftKey) as HashMap<String, Any>
            if (docKycTemporary.size > 0) {
                docKycTemporary.forEach { (kycKey, data) ->
                    when (kycKey) {
                        Config.KycDocKey.selfie -> {
                            convertAsBitmap(binding.selfiePictureImg, data.toString())
                            binding.addSelfiePictureImg.visibility = View.GONE
                            binding.selfiePictureImg.alpha = 1f
                        }
                        Config.KycDocKey.idCard -> {
                            convertAsBitmap(binding.idCardImg, data.toString())
                            binding.addIdCardImg.visibility = View.GONE
                            binding.idCardImg.alpha = 1f
                        }
                        Config.KycDocKey.vehiclePicture -> {
                            convertAsBitmap(binding.vehiclePictureImg, data.toString())
                            binding.addSelectVehicleImg.visibility = View.GONE
                            binding.vehiclePictureImg.alpha = 1f
                        }
                        Config.KycDocKey.plateNumberImg -> {
                            convertAsBitmap(binding.plateNumberImg, data.toString())
                            binding.addPlateNumberImg.visibility = View.GONE
                            binding.plateNumberImg.alpha = 1f
                        }
                        Config.KycDocKey.plateNumberText -> {
                            binding.plateNumberTextInputEditText.setText(data.toString())
                        }

                    }
                }
            }

        }

        communityTaxiAdapter = CommunityTaxiPaginAdapter()
        binding.communityTextRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                self(), RecyclerView.HORIZONTAL, false
            )
            adapter = communityTaxiAdapter
        }

        if (hasReUploadKyc()) {
            var user = getUserUserToSharePreference()
            /* selectCommunityTaxiId = user?.kycDocument?.communityTaxi?.id ?: -1*/
            selectVehicleTypeId = user?.kycDocument?.termId ?: -1
            binding.appNameTv.text = getString(R.string.re_submit_kyc)

            binding.plateNumberTextInputEditText.setText(
                user?.kycDocument?.plateNumber ?: ""
            )

            binding.organizationCodeTextInputEditText.setText(
                user?.kycDocument?.organizationCode ?: ""
            )
            binding.organizationCodeContainer.visibility = View.VISIBLE
        } else {
            binding.organizationCodeContainer.visibility = View.GONE
        }

        if (BuildConfig.IS_WEGO_TAXI) {
            binding.actionDoneMtb.setBackgroundColor(ContextCompat.getColor(self(), R.color.white))
        }
    }

    private fun initObserved() {
        uploadDocVm.loadingUploadDocMutableLiveData.observe(this) { hasLoading ->
            binding.loading.root.visibility = if (hasLoading) View.VISIBLE else View.GONE
        }
        uploadDocVm.vehicleTypeUploadDocMutableLiveData.observe(this) { respondState ->
            if (respondState.success) {
                respondState.data?.let {
                    if (respondState.data.isNotEmpty()) {
                        vehicleAdapter.addVehicle(respondState.data, selectVehicleTypeId)
                    }
                }
            }
        }
        uploadDocVm.globalErrorMutableLiveData.observe(this) { respondState ->
            if (!respondState.success) {
                globalShowError(respondState.message)
            }
        }
        uploadDocVm.registerMutableLiveData.observe(this) { respondState ->
            if (respondState.success) {
                respondState.data?.let { data ->
                    data.user?.let {
                        val userDetailGsonStr = Gson().toJson(data.user)
                        EazyTaxiHelper.setSharePreference(
                            this,
                            Constants.userDetailKey,
                            userDetailGsonStr
                        )
                    }

                    EazyTaxiHelper.setSharePreference(
                        this,
                        Constants.Token.API_TOKEN,
                        data.access_token ?: ""
                    )

                    hasBiometric?.let {
                        saveBiometric(it)
                    }

                    RedirectClass.gotoMainActivity(
                        self()
                    )
                }
            } else {
                globalShowError(respondState.message)
            }
        }
        uploadDocVm.reUploadKycDocMutableLiveData.observe(this) { respondState ->
            if (respondState.success) {
                respondState.data?.let { data ->
                    val userDetailGsonStr = Gson().toJson(data)
                    EazyTaxiHelper.setSharePreference(
                        this,
                        Constants.userDetailKey,
                        userDetailGsonStr
                    )
                    RedirectClass.gotoMainActivity(self())
                }
            } else {
                globalShowError(respondState.message)
            }
        }
        communityTaxi.communityTaxiLoadingMutableLiveData.observe(this) { hasLoading ->
            binding.loading.root.visibility = if (hasLoading) View.VISIBLE else View.GONE
        }
        communityTaxi.communityTaxiErrorPaginationMutableLiveData.observe(this) {
            if (!it.success) {
                globalShowError(it.message)
            }
        }
        communityTaxiTemporaryRequestOneTimeVm.communityTaxiLoadingMutableLiveData.observe(this) { hasLoading ->
            binding.loading.root.visibility = if (hasLoading) View.VISIBLE else View.GONE
        }
    }

    private fun doAction() {
        vehicleAdapter.selectedRow = { vehicle ->
            this.selectVehicleTypeId = vehicle.id ?: -1
        }
        communityTaxiAdapter.selectRow = { communityTaxi ->
            /* selectCommunityTaxiId = communityTaxi.id ?: -1*/
        }
        binding.actionBackImg.setOnClickListener {
            val hasEditKycDoc = docKycTemporary.size > 0
            if (hasEditKycDoc) {
                if (binding.plateNumberTextInputEditText.text.toString().isNotEmpty()) {
                    docKycTemporary[Config.KycDocKey.plateNumberText] =
                        binding.plateNumberTextInputEditText.text.toString().trim()
                }
                val intent = Intent()
                intent.putExtra(
                    "tempKyc",
                    GsonConverterHelper.convertGenericClassToJson(docKycTemporary)
                )
                setResult(RESULT_OK, intent)
                finish()

            } else {
                finish()
            }
        }
        binding.actionDoneMtb.setOnClickListener {
            when {
                binding.addSelfiePictureImg.visibility == View.VISIBLE -> {
                    globalShowError(getString(R.string.selfie_picture_is_required))
                }
                binding.addIdCardImg.visibility == View.VISIBLE -> {
                    globalShowError(getString(R.string.id_card_is_required))
                }
                /*selectCommunityTaxiId == -1 -> {
                    globalShowError(getString(R.string.please_choose_community_taxi))
                }*/
                selectVehicleTypeId == -1 -> {
                    globalShowError(getString(R.string.please_select_vehicle_type))
                }
                binding.addSelectVehicleImg.visibility == View.VISIBLE -> {
                    globalShowError(getString(R.string.vehicle_picture_is_required))
                }
                binding.addPlateNumberImg.visibility == View.VISIBLE -> {
                    globalShowError(getString(R.string.plate_number_picture_is_required))
                }
                binding.plateNumberTextInputEditText.text.toString()
                    .isEmpty() -> {
                    globalShowError(getString(R.string.please_enter_your_plate_number))
                    binding.plateNumberTextInputLayout.error =
                        getString(R.string.field_is_required)
                }
                else -> {
                    if (hasReUploadKyc() && binding.organizationCodeTextInputEditText.text.toString()
                            .isEmpty()
                    ) {
                        globalShowError(getString(R.string.organization_code))
                        binding.organizationCodeTextInputLayout.error =
                            getString(R.string.organization_code)

                    } else {
                        binding.plateNumberTextInputLayout.error = null
                        binding.loading.root.visibility = View.VISIBLE
                        val selfiePictureBitmap: Bitmap =
                            (binding.selfiePictureImg.drawable as BitmapDrawable).bitmap
                        val idCardBitmap: Bitmap =
                            (binding.idCardImg.drawable as BitmapDrawable).bitmap
                        val vehiclePictureBitmap: Bitmap =
                            (binding.vehiclePictureImg.drawable as BitmapDrawable).bitmap
                        val plateNumberBitmap: Bitmap =
                            (binding.plateNumberImg.drawable as BitmapDrawable).bitmap

                        val kycDocHm = HashMap<String, Any>()
                        kycDocHm["profile_img"] = String.format(
                            "data:image/png;base64, %s",
                            ImageHelper.convertBase64(
                                selfiePictureBitmap,Bitmap.CompressFormat.JPEG,90
                            )
                        )
                        kycDocHm["id_card_img"] = String.format(
                            "data:image/png;base64, %s",
                            ImageHelper.convertBase64(idCardBitmap,Bitmap.CompressFormat.JPEG,90)
                        )
                        kycDocHm["vehicle_img"] = String.format(
                            "data:image/png;base64, %s",
                            ImageHelper.convertBase64(vehiclePictureBitmap,Bitmap.CompressFormat.JPEG,90)
                        )
                        kycDocHm["plate_number_img"] = String.format(
                            "data:image/png;base64, %s",
                            ImageHelper.convertBase64(plateNumberBitmap,Bitmap.CompressFormat.JPEG,90)
                        )

                        if (intent.hasExtra(RequestBodyKey)) {
                            val bodyRequest: HashMap<String, Any> =
                                intent.getSerializableExtra(RequestBodyKey) as HashMap<String, Any>
                            bodyRequest["kyc_documents"] = kycDocHm
                            /* bodyRequest["community_user_id"] = selectCommunityTaxiId*/
                            bodyRequest["plate_number"] =
                                binding.plateNumberTextInputEditText.text.toString().trim()
                            bodyRequest["term_id"] = selectVehicleTypeId
                            hasBiometric = bodyRequest["hasBiometric"].toString()
                            bodyRequest.remove("hasBiometric")
                            uploadDocVm.submitCreateUser(bodyRequest)
                        } else {
                            //re upload kyc
                            val bodyRequest = HashMap<String, Any>()
                            bodyRequest["kyc_documents"] = kycDocHm
                            /* bodyRequest["community_user_id"] = selectCommunityTaxiId*/
                            bodyRequest["plate_number"] =
                                binding.plateNumberTextInputEditText.text.toString().trim()
                            bodyRequest["term_id"] = selectVehicleTypeId

                            if (hasReUploadKyc()) {
                                bodyRequest["organization_code"] =
                                    binding.organizationCodeTextInputEditText.text.toString().trim()
                            }

                            uploadDocVm.reUploadKycDoc(bodyRequest)
                        }
                    }
                }
            }
        }
        binding.actionSelfiePictureFrameLayout.setOnClickListener {
            if (PermissionHelper.hasCameraAndExternalStoragePermission(self())) {
                openSelfiePicture()
            } else {
                PermissionHelper.requestMultiPermission(
                    self(),
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    )
                ) { hasPermission ->
                    if (hasPermission) {
                        openSelfiePicture()
                    } else {
                        MessageUtils.showError(
                            self(),
                            null,
                            "Permission Camera And Storage is deny"
                        )
                    }
                }
            }
        }
        binding.actionIdCardFrameLayout.setOnClickListener {
            if (PermissionHelper.hasCameraAndExternalStoragePermission(self())) {
                openIdCard()
            } else {
                PermissionHelper.requestMultiPermission(
                    self(),
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    )
                ) { hasPermission ->
                    if (hasPermission) {
                        openIdCard()
                    } else {
                        MessageUtils.showError(
                            self(),
                            null,
                            "Permission Camera And Storage is deny"
                        )
                    }
                }
            }
        }
        binding.actionSelectVehicleFrameLayout.setOnClickListener {
            if (PermissionHelper.hasCameraAndExternalStoragePermission(self())) {
                openSelectVehicle()
            } else {
                PermissionHelper.requestMultiPermission(
                    self(),
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    )
                ) { hasPermission ->
                    if (hasPermission) {
                        openSelectVehicle()
                    } else {
                        MessageUtils.showError(
                            self(),
                            null,
                            "Permission Camera And Storage is deny"
                        )
                    }
                }
            }
        }
        binding.actionPlateNumberFrameLayout.setOnClickListener {
            if (PermissionHelper.hasCameraAndExternalStoragePermission(self())) {
                openPlateNumber()
            } else {
                PermissionHelper.requestMultiPermission(
                    self(),
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    )
                ) { hasPermission ->
                    if (hasPermission) {
                        openPlateNumber()
                    } else {
                        MessageUtils.showError(
                            self(),
                            null,
                            "Permission Camera And Storage is deny"
                        )
                    }
                }
            }
        }
        binding.searchCommunityImg.setOnClickListener {
            openCommunityTaxi()
        }
        binding.plateNumberTextInputEditText.doOnTextChanged { text, start, before, count ->
            text?.let {
                EazyTaxiHelper.validateField(
                    binding.plateNumberTextInputLayout,
                    it,
                    getString(R.string.field_is_required)
                )
            }
        }
        binding.organizationCodeTextInputEditText.doOnTextChanged { text, start, before, count ->
            text?.let {
                EazyTaxiHelper.validateField(
                    binding.organizationCodeTextInputLayout,
                    it,
                    getString(R.string.field_is_required)
                )
            }
        }

    }

    private fun initDataSever() {
        //to disable this version
        /*val bodyQuery = HashMap<String, Any>()
        bodyQuery["page"] = 1
        bodyQuery["limit"] = 50
        val communityTaxis = communityTaxi.fetchCommunityTaxi(bodyQuery)
        communityTaxis.observe(this) {
            communityTaxiAdapter.submitData(it, selectCommunityTaxiId)
        }*/
        uploadDocVm.fetchVehicleType(Config.KeyServer.Slug)
    }

    private fun hasReUploadKyc(): Boolean {
        return intent.hasExtra(reUploadKycKey) && intent.getBooleanExtra(reUploadKycKey, false)
    }

    private fun openIdCard() {
        Pix.start(
            this,
            "ID Card",
            ContextCompat.getColor(self(), R.color.colorPrimary),
            RequestIdCardCode,
            1,
            60,
            false
        )
    }

    private fun openSelfiePicture() {
        Pix.start(
            this,
            "Selfie",
            ContextCompat.getColor(self(),R.color.colorPrimary),
            RequestSelfieProfileCode,
            1,
            10,
            true
        )
    }

    private fun openSelectVehicle() {
        Pix.start(
            this, "Vehicle",
            ContextCompat.getColor(self(),R.color.colorPrimary),
            RequestVehiclePictureCode,
            1,
            60,
            false
        )
    }

    private fun openPlateNumber() {
        Pix.start(
            this,
            "Plate Number",
            ContextCompat.getColor(self(),R.color.colorPrimary),
            RequestPlateNumberCode,
            1,
            60,
            false
        )
    }

    private fun openCommunityTaxi() {
//        val bodyQuery = HashMap<String, Any>()
//        bodyQuery["page"] = 1
//        bodyQuery["limit"] = 50
//        val communityTaxis =
//            communityTaxiTemporaryRequestOneTimeVm.fetchCommunityTaxi(bodyQuery)
//
//        ChooseCommunityBottomSheet
//            .newInstance(
//                communityTaxi,
//                communityTaxiTemporaryRequestOneTimeVm,
//                communityTaxis,
//                selectCommunityTaxiId
//            ) {
//                this.selectCommunityTaxiId = it.id ?: -1
//                communityTaxiAdapter.updateItem(selectCommunityTaxiId)
//                val index =
//                    communityTaxiAdapter.getCommunityTaxiLists()?.indexOf(it) ?: 0
//                binding.communityTextRecyclerView.smoothScrollToPosition(index)
//            }
//            .show(supportFragmentManager, "ChooseCommunityBottomSheet")
    }

    private fun getUserFromGson(jsonObject: JsonObject): User? {
        try {
            val userObject = jsonObject["user"].asJsonObject
            return GsonConverterHelper.getJsonObjectToGenericClass<User>(userObject.toString())
        } catch (jsonSyntax: JsonSyntaxException) {

        }
        return null
    }

}

