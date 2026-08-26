extra.apply {
    set("compose_ui_version", "1.6.0")
    set("room_version", "2.6.1")
    set("hilt_version", "2.50")
    set("work_version", "2.9.0")
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android.plugin) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.devtools.ksp") version "2.0.20-1.0.24" apply false
}