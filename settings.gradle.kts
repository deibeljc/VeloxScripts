rootProject.name = "VeloxScripts"

include("libraries:frameworks")

include("libraries:utils")

include("libraries:gui")

include("scripts:veloxcombat")

include("scripts")

pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }
}
