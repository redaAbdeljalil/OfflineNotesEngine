extra.apply {
    set("compose_ui_version", "1.6.0")
    set("room_version", "2.6.1")
    set("hilt_version", "2.50")
    set("work_version", "2.9.0")
}
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
}