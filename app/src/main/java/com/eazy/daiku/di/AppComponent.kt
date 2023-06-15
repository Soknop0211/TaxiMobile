package com.eazy.daiku.di

import android.app.Application

import com.eazy.daiku.di.module.*
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        AndroidSupportInjectionModule::class,
        ViewModelModule::class,
        NetworkModule::class,
        FragmentBuilderModule::class,
        ActivityBuilderModule::class
    ]
)

interface AppComponent : AndroidInjector<com.eazy.daiku.EazyTaxiApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        fun build(): AppComponent
    }
    override fun inject(app: com.eazy.daiku.EazyTaxiApplication)

}


