plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("app.cash.paparazzi")
    id("com.google.devtools.ksp")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "proton.android.pass.screenshottests"
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    flavorDimensions += "version"
    productFlavors {
        maybeCreate("dev")
        maybeCreate("alpha")
        maybeCreate("play")
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }
    sourceSets {
        getByName("test").java.srcDirs("build/generated/ksp/devDebugUnitTest/kotlin")
    }
}

androidComponents {
    beforeVariants(selector().withBuildType("release")) { builder ->
        builder.enable = false
    }
    beforeVariants { variant ->
        variant.enableAndroidTest = false
    }
}

dependencies {
    implementation(projects.pass.autofill.impl)
    implementation(projects.pass.commonUi.api)
    implementation(projects.pass.composeComponents.impl)
    implementation(projects.pass.featureAccount.impl)
    implementation(projects.pass.featureAuth.impl)
    implementation(projects.pass.featureHome.impl)
    implementation(projects.pass.featureItemCreate.impl)
    implementation(projects.pass.featureItemDetail.impl)
    implementation(projects.pass.featureMigrate.impl)
    implementation(projects.pass.featureOnboarding.impl)
    implementation(projects.pass.featurePasskeys)
    implementation(projects.pass.featurePassword.impl)
    implementation(projects.pass.featureProfile.impl)
    implementation(projects.pass.featureSearchOptions.impl)
    implementation(projects.pass.featureSelectItem)
    implementation(projects.pass.featureSettings.impl)
    implementation(projects.pass.featureSharing.impl)
    implementation(projects.pass.featureSync.impl)
    implementation(projects.pass.featureTrash.impl)
    implementation(projects.pass.featureTrial.impl)
    implementation(projects.pass.featureVault.impl)
    implementation(projects.pass.features.itemHistory)
    implementation(projects.pass.features.securityCenter)
    implementation(projects.pass.features.upsell)

    testImplementation(libs.androidx.compose.ui)
    testImplementation(libs.androidx.compose.uiTooling)
    testImplementation(libs.core.presentation.compose)
    testImplementation(libs.kotlin.reflect)

    testImplementation(libs.showkase)
    kspTest(libs.showkaseProcessor)

    testImplementation(libs.testParameterInjector)
}
