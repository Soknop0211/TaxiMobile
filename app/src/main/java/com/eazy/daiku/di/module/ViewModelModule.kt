package com.eazy.daiku.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eazy.daiku.ui.customer.viewmodel.ProcessCustomerBookingViewModel
import  com.eazy.daiku.utility.base.BaseViewModel
import  com.eazy.daiku.utility.view_model.*
import com.eazy.daiku.utility.view_model.community.CommunityVm
import  com.eazy.daiku.utility.view_model.user_case.CreateUserViewModel
import  com.eazy.daiku.utility.view_model.user_case.LoginViewModel
import  com.eazy.daiku.utility.view_model.user_case.UseCaseVm
import  com.eazy.daiku.utility.view_model.uplaod_doc_vm.UploadDocVM
import  com.eazy.daiku.utility.view_model.withdraw.WithdrawViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(BaseViewModel::class)
    abstract fun bindBaseViewModel(baseViewModel: BaseViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateUserViewModel::class)
    abstract fun bindCreateUserViewModel(createUserViewModel: CreateUserViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun bindLoginViewModel(loginViewModel: LoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UploadDocVM::class)
    abstract fun bindUploadDocVM(uploadDocVM: UploadDocVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(QrCodeVm::class)
    abstract fun bindScanQrVm(scanQrVm: QrCodeVm): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UseCaseVm::class)
    abstract fun bindMUserViewModel(mUserInfoVM: UseCaseVm): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TransactionVm::class)
    abstract fun bindTransactionViewModel(transactionVm: TransactionVm): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HistoryVm::class)
    abstract fun bindHistoryVmViewModel(historyVm: HistoryVm): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(WithdrawViewModel::class)
    abstract fun bindWithdrawViewModel(withdrawViewModel: WithdrawViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CommunityVm::class)
    abstract fun bindCommunityVm(forgetPwdVm: CommunityVm): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProcessCustomerBookingViewModel::class)
    abstract fun bindProcessCustomerBooking(processCustomerBookingViewModel: ProcessCustomerBookingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CustomerHistoryVm::class)
    abstract fun bindCustomerHistoryViewModel(customerHistoryVm: CustomerHistoryVm): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}

@MustBeDocumented
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)
