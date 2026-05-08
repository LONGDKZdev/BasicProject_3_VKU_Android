import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("com.google.gms.google-services")
}


android {
    namespace = "com.vohuy.mixueapp"
    compileSdk = 36

    // Read local.properties (not committed) for Supabase keys
    val localProperties = Properties().apply {
        val localPropsFile = rootProject.file("local.properties")
        if (localPropsFile.exists()) {
            localPropsFile.inputStream().use { load(it) }
        }
    }

    defaultConfig {
        applicationId = "com.vohuy.mixueapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Supabase (Storage) keys
        // Priority:
        // 1) local.properties (recommended)
        // 2) hardcoded fallback (LOCAL-ONLY) to keep this workspace runnable even if local.properties is missing.
        val supabaseUrl = localProperties.getProperty("SUPABASE_URL")
            ?.takeIf { it.isNotBlank() }
            ?: "https://vqupigbrkuucghnauwrb.supabase.co"

        val supabaseAnonKey = localProperties.getProperty("SUPABASE_ANON_KEY")
            ?.takeIf { it.isNotBlank() }
            ?: "sb_publishable_hJjVJNHKhXvwIT17Goi0Cw_4w3AQE9y"

        val supabaseBucket = localProperties.getProperty("SUPABASE_BUCKET")
            ?.takeIf { it.isNotBlank() }
            ?: "StorageImage_MixueAndroid"

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
        buildConfigField("String", "SUPABASE_BUCKET", "\"$supabaseBucket\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
        )
    }

    buildFeatures {
        compose = true
        // Compose-first UI; keep ViewBinding off unless you still need XML screens.
        viewBinding = false
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    
    // Jetpack Compose Dependencies
    // Use a more recent Compose BOM to avoid Indication/clickable incompatibilities
    // (e.g. crash: 'clickable only supports IndicationNodeFactory... PlatformRipple').
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.13.0")
    
    // Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // Compose ViewModel & LiveData integration
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.compose.runtime:runtime-livedata")
    
    // Coil for image loading in Compose (replaces Glide)
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Compose debugging
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    
    // RecyclerView for listing items - UPDATED to 1.4.0
    implementation(libs.androidx.recyclerview)
    
    // Fragment - UPDATED to latest
    implementation(libs.androidx.fragment.ktx)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //***THƯ VIỆN FIREBASE CHO ĐỒ ÁN MIXUE***

    // Firebase Bill of Materials (BoM) - Stable version
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // Các dịch vụ Firebase cho đồ án Mixue
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")

    // --- Supabase (Storage) ---
    // We only use Supabase for image storage (public bucket).
    implementation("io.github.jan-tennert.supabase:supabase-kt:2.6.1")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.6.1")
    implementation("io.ktor:ktor-client-okhttp:2.3.12")


    // ViewModel và LiveData cho mô hình MVVM - UPDATED to 2.8.1
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    
    // Coroutines - UPDATED for async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}