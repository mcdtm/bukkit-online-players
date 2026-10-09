plugins {
  idea
  `java-library`
  `maven-publish`

  alias(libraries.plugins.shadow)
}

allprojects {
  group = "me.kvdpxne"
  version = "0.1.1"
}

// The latest LTS version of Java.
val latestJavaVersion = 25

// The Java version in which the project will be compiled.
val targetJavaVersion = 25

idea {
  project {
    jdkName = latestJavaVersion.toString()
  }

  module {
    isDownloadJavadoc = true
    isDownloadSources = true
  }
}

subprojects {

  apply {
    plugin("java-library")
    plugin("maven-publish")
    plugin("com.gradleup.shadow")
  }

  java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)

    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion

    if (JavaVersion.current() < javaVersion) {
      toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
  }

  tasks {

    withType<JavaCompile> {
      options.apply {
        if (10 <= targetJavaVersion || JavaVersion.current().isJava10Compatible) {
          release = targetJavaVersion
        }

        encoding = Charsets.UTF_8.name()
        compilerArgs.add("-Xlint:-options")
      }
    }

    withType<Test> {
      useJUnitPlatform()
    }
  }

  publishing {
    publications {
      create<MavenPublication>("mavenJava") {

        pom {
          name = rootProject.name
          url = "https://github.com/kvdpxne/${rootProject.name}"

          licenses {
            license {
              name = "MIT License"
              url = "https://opensource.org/licenses/MIT"
            }
          }
        }

        from(components["java"])
      }
    }
  }
}

tasks {

}