package com.cardioo

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
// dagger.hilt.android.testing.HiltTestApplication
import kotlin.jvm.java

class HiltTestRunner : AndroidJUnitRunner() {
  /*  override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?,
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }*/
}

