dependencies {
  compileOnly(fileTree("../.local_libraries") {
    include("craftbukkit-1.7.10.jar")
  })

  api(project(":api"))
  api(project(":pre_1_7_10"))
  api(project(":from_1_7_10"))

  api(libs.notchity)
}