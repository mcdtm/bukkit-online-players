plugins {
  idea
  java
  `java-library`
  signing
  `maven-publish`

  // Optional: enable only when the Shadow plugin is applied; uncomment under the SHADOW tag.
  alias(libs.plugins.shadow)
}

subprojects {
  apply(plugin = "java")
  apply(plugin = "java-library")
  apply(plugin = "signing")
  apply(plugin = "maven-publish")
  apply(plugin = "com.github.johnrengelman.shadow")

  group = providers.gradleProperty("group").get()
  version = providers.gradleProperty("version").get()
  description = providers.gradleProperty("description").get()

  java {
    toolchain {
      languageVersion = JavaLanguageVersion.of(25)
    }
  }

  dependencies {
    "testImplementation"(rootProject.libs.junit.jupiter)
    "testRuntimeOnly"(rootProject.libs.junit.platform.launcher)
  }

  tasks {

    test {
      useJUnitPlatform()
    }

    compileJava {
      options.encoding = "UTF-8"
      options.release.set(25)
    }

    val sourcesJar = register<Jar>("sourcesJar") {
      archiveClassifier.set("sources")
      from(sourceSets.main.get().allSource)
    }

    val javadocJar = register<Jar>("javadocJar") {
      archiveClassifier.set("javadoc")
      from(javadoc)
    }

    // Optional Shadow configuration (SHADOW): enable only when the Shadow plugin is applied to avoid plugin resolution failures.
    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
      archiveFileName = "${rootProject.name}-${rootProject.version}.jar"
      mergeServiceFiles()
      minimize()
    }

    register("projectInfo") {
      group = "help"
      description = "Wypisuje podstawowe informacje o projekcie"

      // Wartości obliczone w czasie konfiguracji (bezpieczne dla configuration cache)
      val projectName = project.name
      val projectGroup = project.group.toString()
      val projectVersion = project.version.toString()
      val projectDescription = project.description ?: ""
      val javaVersion = JavaVersion.current().toString()
      val toolchainVersion = java.toolchain.languageVersion.get().toString()
      val gradleVersion = gradle.gradleVersion

      // Wejścia zadania — Gradle wie, kiedy unieważnić cache
      inputs.property("projectName", projectName)
      inputs.property("projectGroup", projectGroup)
      inputs.property("projectVersion", projectVersion)
      inputs.property("projectDescription", projectDescription)

      doLast {
        println(
          """
              |Projekt:      $projectName
              |Grupa:        $projectGroup
              |Wersja:       $projectVersion
              |Opis:         $projectDescription
              |Java:         $javaVersion
              |Toolchain:    $toolchainVersion
              |Gradle:       $gradleVersion
              """.trimMargin()
        )
      }
    }
  }

  publishing {
    publications {
      create<MavenPublication>("mavenJava") {
        from(components["java"])
        artifact(tasks.named("sourcesJar"))
        artifact(tasks.named("javadocJar"))

        pom {
          name.set(rootProject.name)
          description.set(project.description)
          url.set(providers.gradleProperty("url").get())

          licenses {
            license {
              name.set(providers.gradleProperty("license.name").get())
              url.set(providers.gradleProperty("license.url").get())
            }
          }
          developers {
            developer {
              id.set(providers.gradleProperty("developer.id").get())
              name.set(providers.gradleProperty("developer.name").get())
            }
          }
          scm {
            connection.set(providers.gradleProperty("scm.connection").get())
            developerConnection.set(providers.gradleProperty("scm.developerConnection").get())
            url.set(providers.gradleProperty("scm.url").get())
          }
        }
      }
    }
  }

  signing {
    // Sign only when credentials are available to avoid failing local builds.
    val signingKey = providers.gradleProperty("signing.secretKey").orNull
    val signingPassword = providers.gradleProperty("signing.password").orNull

    if (signingKey != null && signingPassword != null) {
      useInMemoryPgpKeys(signingKey, signingPassword)
      sign(publishing.publications["mavenJava"])
    }
  }
}