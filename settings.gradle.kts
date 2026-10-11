pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }

  plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("com.gradleup.nmcp.settings") version "2.6.1"
  }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

  repositories {
    mavenCentral()
    maven("https://jitpack.io") {
      content {
        includeGroupAndSubgroups("com.github.kvdpxne")
      }
    }
  }
}

include("entry", "api", "from_1_7_10", "pre_1_7_10")
rootProject.name = "bukkit-online-players"

