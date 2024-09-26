rootProject.name = "VeloxScripts"

include("libraries:my-library")

include("scripts:veloxcombat")
include("scripts")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}