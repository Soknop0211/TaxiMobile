package com.eazy.daiku.ui.signup

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.eazy.daiku.BuildConfig
import com.eazy.daiku.utility.base.BaseActivity
import com.eazy.daiku.utility.bottom_sheet.DatePickerBottomSheetDialog
import com.eazy.daiku.utility.call_back.SelectDateListener
import com.eazy.daiku.utility.custom.MessageUtils
import com.eazy.daiku.utility.redirect.RedirectClass
import com.eazy.daiku.utility.view_model.user_case.CreateUserViewModel
import com.eazy.daiku.R
import com.eazy.daiku.databinding.ActivityCreateUserBinding
import com.eazy.daiku.utility.*
import com.eazy.daiku.utility.base.SimpleBaseActivity
import com.eazy.daiku.utility.other.AppLOGG
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates
import com.eazy.daiku.utility.EazyTaxiHelper.validateField as validateField1

class SignUpActivity : SimpleBaseActivity() {

    private val createUserViewModel: CreateUserViewModel by viewModels {
        factory
    }
    private lateinit var binding: ActivityCreateUserBinding
    private var dobDate: Date? = null
    private lateinit var genderScreenValue: Array<String>
    private lateinit var genderKey: Array<String>
    private var selectGenderIndex by Delegates.notNull<Int>()
    private var docKycTemporary: HashMap<String, Any>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        doAction()
        initObserved()
        initViewTaxiWithCustomer()

    }

    private fun initView() {

        if (BuildConfig.IS_WEGO_TAXI) {
            binding.actionNextMtb.setBackgroundColor(ContextCompat.getColor(self(), R.color.white))
        }

        genderScreenValue = resources.getStringArray(R.array.gender_screen)
        genderKey = resources.getStringArray(R.array.gender_value)

        val adapterGenderDropDown =
            ArrayAdapter(this, R.layout.gender_dropdown_item, genderScreenValue)
        binding.genderAutoCompleteTextView.setAdapter(adapterGenderDropDown)

    }

    private fun validateTextChangedEditText() {
        //skip organize code is a customer
        if (!BuildConfig.IS_CUSTOMER) {
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

        binding.firstnameTextInputEditText.doOnTextChanged { text, start, before, count ->
            text?.let {
                validateField1(
                    binding.firstnameTextInputLayout,
                    it,
                    getString(R.string.field_is_required)
                )
            }
        }
        binding.lastnameTextInputEditText.doOnTextChanged { text, start, before, count ->
            text?.let {
                validateField1(
                    binding.lastnameTextInputLayout,
                    it,
                    getString(R.string.field_is_required)
                )
            }
        }
        binding.genderAutoCompleteTextView.doOnTextChanged { text, start, before, count ->
            text?.let {
                validateField1(
                    binding.genderTextInputLayout,
                    it,
                    getString(R.string.field_is_required)
                )
            }
        }
        binding.birthdayTextInputEditText.doOnTextChanged { text, start, before, count ->
            text?.let {
                validateField1(
                    binding.birthdayTextInputLayout,
                    it,
                    getString(R.string.field_is_required)
                )
            }
        }
       /* binding.emailTextInputEditText.doOnTextChanged { text, start, before, count ->
            text?.let {
                validateField1(
                    binding.emailTextInputLayout,
                    it,
                    getString(R.string.field_is_required)
                )
            }
        }*/
        binding.phoneNumberTextInputEditText.doOnTextChanged { text, start, before, count ->
            text?.let {
                validateField1(
                    binding.phoneNumberTextInputLayout,
                    it,
                    getString(R.string.field_is_required)
                )
            }
        }
        binding.passwordTextInputEditText.doOnTextChanged { text, start, before, count ->
            text?.let {
                validateField1(
                    binding.passwordTextInputLayout,
                    it,
                    getString(R.string.field_is_required)
                )
            }
        }
        binding.confirmPasswordTextInputEditText.doOnTextChanged { text, start, before, count ->
            text?.let {
                validateField1(
                    binding.confirmPasswordTextInputLayout,
                    it,
                    getString(R.string.field_is_required)
                )
            }
        }
    }

    private fun initObserved() {
        createUserViewModel.signupUserValidate.observe(this) { cUserState ->
            if (cUserState.hasDoneValidate == true) {
                val bodyHm = HashMap<String, Any>()

                val organizationCode = "organization_code"
                val firstName = "first_name"
                val lastName = "last_name"
                val gender = "gender"
                val phoneNumber = "phone"
                val password = "password"
                val email = "email"
                val birthday = "birthday"
                val hasBiometric = "hasBiometric"

                val birthDayString: String? = EazyTaxiHelper.formatDate(dobDate, "yyyy-MM-dd")
                bodyHm[organizationCode] =
                    binding.organizationCodeTextInputEditText.text.toString()
                bodyHm[firstName] = binding.firstnameTextInputEditText.text.toString()
                bodyHm[lastName] = binding.lastnameTextInputEditText.text.toString()
                bodyHm[gender] = genderKey[selectGenderIndex]
                bodyHm[phoneNumber] =
                    binding.phoneNumberTextInputEditText.text.toString()
                bodyHm[password] = binding.passwordTextInputEditText.text.toString()
                bodyHm[email] = binding.emailTextInputEditText.text.toString()
                bodyHm[birthday] = birthDayString!!
                if (!BuildConfig.IS_CUSTOMER) {
                    bodyHm[hasBiometric] =
                        if (binding.enableFingerFaceIdSwitchMaterial.isChecked) "1" else "0"
                }

                if (docKycTemporary != null) {
                    RedirectClass.gotoUploadDocsActivity(
                        self(),
                        bodyHm,
                        docKycTemporary,
                        object : BetterActivityResult.OnActivityResult<ActivityResult> {
                            override fun onActivityResult(result: ActivityResult) {
                                if (result.resultCode == RESULT_OK) {
                                    val intent = result.data
                                    if (intent != null) {
                                        val docKycTemp = intent.getStringExtra("tempKyc")
                                        docKycTemporary =
                                            GsonConverterHelper.getJsonObjectToGenericClass(
                                                docKycTemp
                                            )
                                    }
                                }
                            }
                        }
                    )
                } else {
                    if (BuildConfig.IS_CUSTOMER) {
                        bodyHm["term"] = "1"
                        createUserViewModel.submitCreateUserCustomer(bodyHm)
                    } else {
                        RedirectClass.gotoUploadDocsActivity(
                            self(),
                            bodyHm,
                            object : BetterActivityResult.OnActivityResult<ActivityResult> {
                                override fun onActivityResult(result: ActivityResult) {
                                    if (result.resultCode == RESULT_OK) {
                                        val intent = result.data
                                        if (intent != null) {
                                            val docKycTemp = intent.getStringExtra("tempKyc")
                                            docKycTemporary =
                                                GsonConverterHelper.getJsonObjectToGenericClass(
                                                    docKycTemp
                                                )
                                        }
                                    }
                                }
                            }
                        )
                    }

                }


            } else {
                if (TextUtils.isEmpty(binding.organizationCodeTextInputEditText.text.toString())) {
                    validateField1(
                        binding.organizationCodeTextInputLayout,
                        binding.organizationCodeTextInputEditText.text.toString(),
                        getString(R.string.field_is_required)
                    )
                } else
                    if (TextUtils.isEmpty(binding.firstnameTextInputEditText.text.toString())) {
                        validateField1(
                            binding.firstnameTextInputLayout,
                            binding.firstnameTextInputEditText.text.toString(),
                            getString(R.string.field_is_required)
                        )
                    } else if (TextUtils.isEmpty(binding.lastnameTextInputEditText.text.toString())) {
                        validateField1(
                            binding.lastnameTextInputLayout,
                            binding.lastnameTextInputEditText.text.toString(),
                            getString(R.string.field_is_required)
                        )

                    } else if (TextUtils.isEmpty(binding.genderAutoCompleteTextView.text.toString())) {
                        validateField1(
                            binding.genderTextInputLayout,
                            binding.genderAutoCompleteTextView.text.toString(),
                            getString(R.string.field_is_required)
                        )
                    } else if (TextUtils.isEmpty(binding.birthdayTextInputEditText.text.toString())) {
                        validateField1(
                            binding.birthdayTextInputLayout,
                            binding.birthdayTextInputEditText.text.toString(),
                            getString(R.string.field_is_required)
                        )
                    } /*else if (TextUtils.isEmpty(binding.emailTextInputEditText.text.toString())) {
                        validateField1(
                            binding.emailTextInputLayout,
                            binding.emailTextInputEditText.text.toString(),
                            getString(R.string.field_is_required)
                        )
                    }*/ else if (TextUtils.isEmpty(binding.phoneNumberTextInputEditText.text.toString())) {
                        validateField1(
                            binding.phoneNumberTextInputLayout,
                            binding.phoneNumberTextInputEditText.text.toString(),
                            getString(R.string.field_is_required)
                        )
                    } else if (TextUtils.isEmpty(binding.passwordTextInputEditText.text.toString())) {
                        validateField1(
                            binding.passwordTextInputLayout,
                            binding.passwordTextInputEditText.text.toString(),
                            getString(R.string.field_is_required)
                        )
                    } else if (TextUtils.isEmpty(binding.confirmPasswordTextInputEditText.text.toString())) {
                        validateField1(
                            binding.confirmPasswordTextInputLayout,
                            binding.confirmPasswordTextInputEditText.text.toString(),
                            getString(R.string.field_is_required)
                        )
                    }
                MessageUtils.showError(this, "", cUserState.organizationCode?.let { getString(it) })
            }
        }

        // TODO: ================ Start Create User Customer===================
        createUserViewModel.loadingCreateUserCustomerLiveData.observe(self()) { haseLoading ->
            binding.loadingView.root.visibility = if (haseLoading) View.VISIBLE else View.GONE
        }
        createUserViewModel.dataCreateUserCustomerLiveData.observe(self()) { respondState ->
            if (respondState != null && respondState.success) {
                respondState.data?.let { data ->
                    val userDetailGsonStr = Gson().toJson(data)
                    EazyTaxiHelper.setSharePreference(
                        this,
                        Constants.userDetailKey,
                        userDetailGsonStr
                    )
                    EazyTaxiHelper.setSharePreference(
                        this,
                        Constants.Token.API_TOKEN,
                        data.access_token ?: ""
                    )

                    RedirectClass.gotoMainActivity(
                        self()
                    )
                }
            } else {
                globalShowError(respondState.message)
            }
        }

        // TODO: ================ End Create User Customer===================
    }

    private fun initViewTaxiWithCustomer() {
        binding.actionNextMtb.text =
            if (BuildConfig.IS_CUSTOMER) getString(R.string.done) else getString(R.string.action_next)
        binding.containerOrganizationCode.visibility =
            if (BuildConfig.IS_CUSTOMER) View.GONE else View.VISIBLE
    }

    private fun onValidateInformationUser() {
        val organizationCode = binding.organizationCodeTextInputEditText.text.toString()
        val firstName = binding.firstnameTextInputEditText.text.toString()
        val lastName = binding.lastnameTextInputEditText.text.toString()
        val gender = binding.genderAutoCompleteTextView.text.toString()
        val birthday = binding.birthdayTextInputEditText.text.toString()
        val email = binding.emailTextInputEditText.text.toString()
        val phoneNumber = binding.phoneNumberTextInputEditText.text.toString()
        val password = binding.passwordTextInputEditText.text.toString()
        val confirmPassword = binding.confirmPasswordTextInputEditText.text.toString()
        createUserViewModel.validateSignUpUserField(
            organizationCode,
            firstName,
            lastName,
            gender,
            birthday,
            email,
            phoneNumber,
            password,
            confirmPassword
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

    private fun doAction() {
        validateTextChangedEditText()
        binding.birthdayTextInputEditText.apply {
            isLongClickable = false
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
        binding.actionNextMtb.setOnClickListener {
            onValidateInformationUser()
        }
        binding.actionBackImg.setOnClickListener {
            this.finish()
        }
        binding.genderAutoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { p0, p1, p2, p3 ->
                selectGenderIndex = p2
            }

        binding.enableFingerFaceIdSwitchMaterial.setOnClickListener {
            val hasBiometric = BiometricSecurity.checkBiometric(self())
            if (!hasBiometric) {
                binding.enableFingerFaceIdSwitchMaterial.isChecked = false
            }
        }


    }


}





