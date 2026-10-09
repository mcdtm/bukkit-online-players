dependencies {
  compileOnly(fileTree("../.local_libraries") {
    include("craftbukkit-1.7.2.jar")
  })
  api(project(":api"))
}