pluginManagement {

  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {

  versionCatalogs {
    val fileName = "libraries"
    create(fileName) {
      from(files("gradle/$fileName.versions.toml"))
    }
  }

  repositories {
    mavenCentral()
    maven("https://jitpack.io") {
      content {
        includeGroupAndSubgroups("com.github.kvdpxne")
      }
    }
  }

  repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
}

arrayOf(
  "entry",
  "api",
  "from_1_7_10",
  "pre_1_7_10",
).forEach {
  val name = it.replace(':', '-')
  val directory = file("./${it.replace(':', '/')}")

  include(name)
  project(":$name").projectDir = directory
}

rootProject.name = "bukkit-online-players"

