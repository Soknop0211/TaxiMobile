package com.eazy.daiku.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.R
import com.eazy.daiku.data.model.server_model.User
import com.eazy.daiku.utility.ImageHelper
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.bottom_sheet.DatePickerBottomSheetDialog
import com.eazy.daiku.utility.call_back.SelectDateListener
import com.eazy.daiku.databinding.ActivityEditProfileBinding
import com.eazy.daiku.ui.upload_doc.UploadDocActivity
import com.eazy.daiku.utility.Config
import com.eazy.daiku.utility.DateTimeHelper
import com.eazy.daiku.utility.EazyTaxiHelper
import com.eazy.daiku.utility.bottom_sheet.ChooseCommunityBottomSheet
import com.eazy.daiku.utility.bottom_sheet.ChooseVehicleTypeBottomSheetDialog
import com.eazy.daiku.utility.custom.MessageUtils
import com.eazy.daiku.utility.permission_media.PermissionHelper
import com.eazy.daiku.utility.redirect.RedirectClass
import com.eazy.daiku.utility.view_model.community.CommunityVm
import com.eazy.daiku.utility.view_model.uplaod_doc_vm.UploadDocVM
import com.eazy.daiku.utility.view_model.user_case.UseCaseVm
import com.fxn.pix.Pix
import pl.aprilapps.easyphotopicker.EasyImage
import pl.aprilapps.easyphotopicker.MediaFile
import pl.aprilapps.easyphotopicker.MediaSource
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class EditProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var genderScreenValue: Array<String>
    private lateinit var genderKey: Array<String>
    private val communityTaxiTemporaryRequestOneTimeVm: CommunityVm by viewModels {
        factory
    }
    private val communityTaxiVm: CommunityVm by viewModels {
        factory
    }
    private val uploadDocVm: UploadDocVM by viewModels {
        factory
    }
    private val userCaseVm: UseCaseVm by viewModels {
        factory
    }
    private var dobDate: Date? = null
    private var selectGenderIndex by Delegates.notNull<Int>()
    private var user: User? = null
    private var communityTaxiId: Int = -1
    private var vehicleTermId: Int = -1

    companion object {
        const val RequestSelfieProfileCode = 344
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intiView()
        initObserved()
        doAction()
        initViewTaxiWithCustomer()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RequestSelfieProfileCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    val returnValue: ArrayList<String> =
                        data!!.getStringArrayListExtra(Pix.IMAGE_RESULTS) as ArrayList<String>
                    if (returnValue.size > 0) {
                        convertAsBitmap(binding.profileCImg, returnValue[0])
                    }
                }
            }
        }
    }

    private fun intiView() {
        user = getUserUserToSharePreference()
        communityTaxiId = user?.kycDocument?.communityTaxi?.id ?: -1
        vehicleTermId = user?.kycDocument?.termId ?: -1

        genderScreenValue = resources.getStringArray(R.array.gender_screen)
        genderKey = resources.getStringArray(R.array.gender_value)

        dobDate = DateTimeHelper.dateParse(
            user?.birthday ?: ""
        )
        selectGenderIndex = user?.gender ?: 0
        var dobTv = user?.birthday ?: ""
        dobDate?.let {
            dobTv = DateTimeHelper.dateFm(it, "dd/MMMM/yyyy")
        }

        binding.firstNameTextInputEditText.setText(user?.firstName ?: "")
        binding.lastNameTextInputEditText.setText(user?.lastName ?: "")
        binding.emailTextInputEditText.setText(user?.email ?: "")
        binding.genderTextInputEditText.setText(genderScreenValue[selectGenderIndex])
        binding.birthdayTextInputEditText.setText(dobTv)
        binding.communityTaxiTextInputEditText.setText(
            user?.kycDocument?.communityTaxi?.businessName ?: ""
        )
        binding.plateNumberTextInputEditText.setText(user?.kycDocument?.plateNumber ?: "")
        binding.organizationCodeTextInputEditText.setText(
            user?.kycDocument?.organizationCode ?: ""
        )
        binding.vehicleTextInputEditText.setText(user?.kycDocument?.termName ?: "")
        ImageHelper.loadImage(
            self(),
            user?.avatarUrl ?: R.drawable.default_profile,
            R.drawable.default_profile,
            binding.profileCImg
        )

        val isKycPending = user?.kycDocument?.status.equals(Config.KycDocStatus.Pending)
        if (hasVerifiedKyc()) {
            uiEnable(false)
            binding.profileCImg.isEnabled = true
            binding.editImg.visibility = View.VISIBLE
            binding.actionUpdateMtb.visibility = View.VISIBLE
        } else if (isKycPending) {
            uiEnable(false)
            binding.profileCImg.isEnabled = false
            binding.editImg.visibility = View.GONE
            binding.actionUpdateMtb.visibility = View.GONE
        } else {
            uiEnable(true)
            binding.profileCImg.isEnabled = true
            binding.editImg.visibility = View.VISIBLE
            binding.actionUpdateMtb.visibility = View.VISIBLE
        }

        if (BuildConfig.IS_WEGO_TAXI) {
            binding.actionUpdateMtb.setBackgroundColor(
                ContextCompat.getColor(
                    self(),
                    R.color.white
                )
            )
        }
    }

    private fun hasVerifiedKyc(): Boolean {
        return user?.kycDocument?.status.equals(Config.KycDocStatus.Verified)
    }

    private fun initObserved() {
        communityTaxiTemporaryRequestOneTimeVm.communityTaxiLoadingMutableLiveData.observe(this) { hasLoading ->
            binding.loading.root.visibility = if (hasLoading) View.VISIBLE else View.GONE
        }
        uploadDocVm.loadingUploadDocMutableLiveData.observe(this) { hasLoading ->
            binding.loading.root.visibility = if (hasLoading) View.VISIBLE else View.GONE

        }
        uploadDocVm.vehicleTypeUploadDocMutableLiveData.observe(this) { respondState ->
            if (respondState.success) {
                respondState.data?.let { vehicles ->
                    ChooseVehicleTypeBottomSheetDialog
                        .newInstance(
                            vehicleTermId,
                            vehicles
                        ) {
                            this.vehicleTermId = it.id
                            binding.vehicleTextInputEditText.setText(
                                it.name ?: "N/A"
                            )
                        }
                        .show(supportFragmentManager, "ChooseVehicleTypeBottomSheetDialog")
                }
            }
        }
        uploadDocVm.globalErrorMutableLiveData.observe(this) { respondState ->
            if (!respondState.success) {
                globalShowError(respondState.message)
            }
        }

        userCaseVm.loadingUserLiveData.observe(this) {
            binding.loading.root.visibility = if (it) View.VISIBLE else View.GONE

        }
        userCaseVm.dataUserLiveData.observe(this) { it ->
            if (it.success) {
                MessageUtils.showSuccess(
                    self(), "", getString(R.string.update_successfully)
                ) {
                    it.dismiss()
                    setResult(RESULT_OK)
                    finish()
                }
            } else {
                globalShowError(it.message)
            }
        }

    }

    private fun doAction() {

        binding.firstNameTextInputEditText.doOnTextChanged { text, start, before, count ->
            text?.let {
                EazyTaxiHelper.validateField(
                    binding.firstNameTextInputLayout,
                    it,
                    getString(R.string.field_is_required)
                )
            }
        }
        binding.lastNameTextInputEditText.doOnTextChanged { text, start, before, count ->
            text?.let {
                EazyTaxiHelper.validateField(
                    binding.lastNameTextInputLayout,
                    it,
                    getString(R.string.field_is_required)
                )
            }
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

        binding.birthdayTextInputEditText.setOnClickListener {
            val selectDateListener = object : SelectDateListener {
                override fun dateCallBack(date: Date) {
                    updateUIDob(date)
                }
            }

            val datePicker =
                DatePickerBottomSheetDialog.newInstance(
                    getString(R.string.select_birthday),
                    dobDate,
                    selectDateListener
                )
            datePicker.show(supportFragmentManager, "DatePickerBottomSheetDialog")

        }
        binding.actionBackImg.setOnClickListener {
            finish()
        }
        binding.genderTextInputEditText.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.please_select_gender))
            //builder.setMessage("We have a message")
            builder.setSingleChoiceItems(
                genderScreenValue,
                selectGenderIndex
            ) { dialog, position ->
                dialog.dismiss()
                this.selectGenderIndex = position
                binding.genderTextInputEditText.setText(genderScreenValue[position])
            }
            builder.show()
        }
        binding.profileCImg.setOnClickListener {
            if (PermissionHelper.hasCameraAndExternalStoragePermission(self())) {
                openSelfiePicture()
            } else {
                PermissionHelper.requestMultiPermission(
                    self(),
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                    )
                ) { hasPermission ->
                    if (hasPermission) {
                        openSelfiePicture()
                    } else {
                        MessageUtils.showError(
                            self(),
                            null,
                            "Permission Write External Storage Deny"
                        )
                    }
                }
            }


        }
        binding.editImg.setOnClickListener {
            binding.profileCImg.performClick()
        }
        binding.communityTaxiTextInputEditText.setOnClickListener {
            val bodyQuery = HashMap<String, Any>()
            bodyQuery["page"] = 1
            bodyQuery["limit"] = 50
            val communityTaxis =
                communityTaxiTemporaryRequestOneTimeVm.fetchCommunityTaxi(bodyQuery)

            ChooseCommunityBottomSheet
                .newInstance(
                    communityTaxiVm,
                    communityTaxiTemporaryRequestOneTimeVm,
                    communityTaxis,
                    communityTaxiId
                ) {
                    this.communityTaxiId = it.id ?: -1
                    binding.communityTaxiTextInputEditText.setText(
                        it.businessName ?: "N/A"
                    )
                }
                .show(supportFragmentManager, "ChooseCommunityBottomSheet")
        }
        binding.vehicleTextInputEditText.setOnClickListener {
            uploadDocVm.fetchVehicleType(Config.KeyServer.Slug)
        }
        binding.actionUpdateMtb.setOnClickListener {
            if (hasVerifiedKyc()) {
                val profileImg: Bitmap =
                    (binding.profileCImg.drawable as BitmapDrawable).bitmap
                val bodyRequest = HashMap<String, Any>()
                bodyRequest["profile_img"] = String.format(
                    "data:image/png;base64, %s",
                    ImageHelper.convertBase64(profileImg,Bitmap.CompressFormat.PNG,40)
                )
                userCaseVm.updateUser(bodyRequest)
            } else {
                if (binding.firstNameTextInputEditText.text.toString().isEmpty()) {
                    binding.firstNameTextInputLayout.error = getString(R.string.field_is_required)
                } else if (binding.lastNameTextInputEditText.text.toString().isEmpty()) {
                    binding.lastNameTextInputLayout.error = getString(R.string.field_is_required)
                } else if (binding.emailTextInputEditText.text.toString().isNotEmpty() &&
                    !EazyTaxiHelper.isValidEmail(binding.emailTextInputEditText.text.toString())
                ) {
                    binding.emailTextInputLayout.error = getString(R.string.invalid_email_address)
                } else if (binding.plateNumberTextInputEditText.text.toString().isEmpty()) {
                    binding.plateNumberTextInputLayout.error = getString(R.string.field_is_required)
                } else if (binding.organizationCodeTextInputEditText.text.toString().isEmpty()) {
                    binding.organizationCodeTextInputLayout.error =
                        getString(R.string.field_is_required)
                } else {
                    binding.emailTextInputLayout.error = null
                    updateDataToServer()
                }
            }
        }
    }


    private fun initViewTaxiWithCustomer() {
        binding.vehicleTextInputLayout.visibility =
            if (BuildConfig.IS_CUSTOMER) View.GONE else View.VISIBLE
        binding.plateNumberTextInputLayout.visibility =
            if (BuildConfig.IS_CUSTOMER) View.GONE else View.VISIBLE
        binding.organizationCodeTextInputLayout.visibility =
            if (BuildConfig.IS_CUSTOMER) View.GONE else View.VISIBLE
        if (BuildConfig.IS_CUSTOMER) {
            binding.firstNameTextInputEditText.isEnabled = false
            binding.lastNameTextInputEditText.isEnabled = false
            binding.emailTextInputEditText.isEnabled = false
            binding.genderTextInputEditText.isEnabled = false
            binding.birthdayTextInputEditText.isEnabled = false
            binding.actionUpdateMtb.visibility = View.GONE
            binding.profileCImg.isEnabled = false
            binding.editImg.isEnabled = false

        }


    }

    private fun openSelfiePicture() {
        Pix.start(
            this,
            "Selfie",
            ContextCompat.getColor(self(), R.color.colorPrimary),
            RequestSelfieProfileCode,
            1,
            10,
            true
        )
    }

    private fun updateUIDob(date: Date) {
        this.dobDate = date
        val dateFormat = SimpleDateFormat("dd/MMMM/yyyy", Locale.getDefault())
        binding.birthdayTextInputEditText.setText(
            String.format(
                Locale.US,
                "%s",
                dateFormat.format(date)
            )
        )
    }

    private fun updateDataToServer() {
        val firstName = binding.firstNameTextInputEditText.text.toString()
        val lastName = binding.lastNameTextInputEditText.text.toString()
        val birthday = binding.birthdayTextInputEditText.text.toString()
        val email = binding.emailTextInputEditText.text.toString()
        val plateNumber = binding.plateNumberTextInputEditText.text.toString()

        val profileImg: Bitmap =
            (binding.profileCImg.drawable as BitmapDrawable).bitmap

        val bodyRequest = HashMap<String, Any>()
        val birthDayString: String? = EazyTaxiHelper.formatDate(dobDate, "yyyy-MM-dd")
        bodyRequest["first_name"] = binding.firstNameTextInputEditText.text.toString()
        bodyRequest["last_name"] = binding.lastNameTextInputEditText.text.toString()
        bodyRequest["gender"] = genderKey[selectGenderIndex]
        bodyRequest["email"] = binding.emailTextInputEditText.text.toString()
        bodyRequest["plate_number"] = binding.plateNumberTextInputEditText.text.toString()
        /*bodyRequest["community_user_id"] = communityTaxiId*/
        bodyRequest["organization_code"] = binding.organizationCodeTextInputEditText.text.toString()
        bodyRequest["term_id"] = vehicleTermId
        birthDayString?.let {
            bodyRequest["birthday"] = it
        }
        bodyRequest["profile_img"] = String.format(
            "data:image/png;base64, %s",
            ImageHelper.convertBase64(profileImg,Bitmap.CompressFormat.PNG,40)
        )
        userCaseVm.updateUser(bodyRequest)
    }

    private fun uiEnable(verifiedKyc: Boolean) {
        binding.firstNameTextInputEditText.isEnabled = verifiedKyc
        binding.lastNameTextInputEditText.isEnabled = verifiedKyc
        binding.emailTextInputEditText.isEnabled = verifiedKyc
        binding.genderTextInputEditText.isEnabled = verifiedKyc
        binding.birthdayTextInputEditText.isEnabled = verifiedKyc
        binding.communityTaxiTextInputEditText.isEnabled = verifiedKyc
        binding.vehicleTextInputEditText.isEnabled = verifiedKyc
        binding.plateNumberTextInputEditText.isEnabled = verifiedKyc
        binding.organizationCodeTextInputEditText.isEnabled = verifiedKyc

    }

}