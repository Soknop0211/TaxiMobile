package com.eazy.daiku.di.module

import com.eazy.daiku.ui.customer.step_booking.SelectCarBookingFragment
import com.eazy.daiku.ui.customer.step_booking.TripDetailBookingFragment
import com.eazy.daiku.ui.home.ContactFragment
import com.eazy.daiku.ui.home.MainWalletFragment
import com.eazy.daiku.ui.payment_method.AddABAAccountNumberFragment
import com.eazy.daiku.ui.payment_method.ChangeCardAndBankAccountFragment
import com.eazy.daiku.ui.payment_method.SelectBankFragment
import com.eazy.daiku.utility.base.BaseFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class FragmentBuilderModule {

    @ContributesAndroidInjector
    abstract fun baseFragment(): BaseFragment

    @ContributesAndroidInjector
    abstract fun mainWalletFragment(): MainWalletFragment

    @ContributesAndroidInjector
    abstract fun contactFragment(): ContactFragment

    @ContributesAndroidInjector
    abstract fun addABAAccountNumberFragment(): AddABAAccountNumberFragment

    @ContributesAndroidInjector
    abstract fun changeCardAndBankAccountFragment(): ChangeCardAndBankAccountFragment

    @ContributesAndroidInjector
    abstract fun selectBankFragment(): SelectBankFragment

    @ContributesAndroidInjector
    abstract fun selectCarBookingFragment(): SelectCarBookingFragment


    @ContributesAndroidInjector
    abstract fun tripDetailBookingFragment(): TripDetailBookingFragment


}