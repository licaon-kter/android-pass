/*
 * Copyright (c) 2024 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "proton.android.pass.featurepasskeys"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "proton.android.pass.test.HiltRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }

    testOptions {
        managedDevices {
            devices {
                maybeCreate<com.android.build.api.dsl.ManagedVirtualDevice>("pixel2api30").apply {
                    device = "Pixel 2"
                    apiLevel = 30
                    systemImageSource = "aosp-atd"
                }
            }
        }
    }
}

androidComponents.beforeVariants { variant ->
    variant.enableAndroidTest = false
}

dependencies {

    implementation(libs.accompanist.navigation.material)
    implementation(libs.accompanist.systemUiController)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.core.accountManager.domain)
    implementation(libs.core.presentation)
    implementation(libs.core.presentation.compose)
    implementation(libs.core.utilKotlin)
    implementation(libs.kotlinx.collections)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)

    debugImplementation(libs.androidx.compose.uiTooling)
    implementation(libs.androidx.compose.uiToolingPreview)

    debugImplementation(libs.showkase)
    kspDebug(libs.showkaseProcessor)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)

    implementation(projects.pass.account.api)
    implementation(projects.pass.biometry.api)
    implementation(projects.pass.common.api)
    implementation(projects.pass.commonPresentation.api)
    implementation(projects.pass.commonPresentation.impl)
    implementation(projects.pass.commonUi.api)
    implementation(projects.pass.commonUi.impl)
    implementation(projects.pass.commonUiModels.api)
    implementation(projects.pass.composeComponents.impl)
    implementation(projects.pass.crypto.api)
    implementation(projects.pass.data.api)
    implementation(projects.pass.domain)
    implementation(projects.pass.featureAuth.impl)
    implementation(projects.pass.featureItemCreate.impl)
    implementation(projects.pass.featurePassword.impl)
    implementation(projects.pass.featureSearchOptions.api)
    implementation(projects.pass.featureSearchOptions.impl)
    implementation(projects.pass.featureSelectItem)
    implementation(projects.pass.featureVault.impl)
    implementation(projects.pass.log.api)
    implementation(projects.pass.navigation.api)
    implementation(projects.pass.notifications.api)
    implementation(projects.pass.passkeys.api)
    implementation(projects.pass.preferences.api)
    implementation(projects.pass.telemetry.api)

    testImplementation(libs.truth)
    testImplementation(libs.kotlinTest)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.turbine)
}
