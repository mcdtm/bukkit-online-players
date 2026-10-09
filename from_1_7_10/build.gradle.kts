dependencies {
  compileOnly(fileTree("../.local_libraries") {
    include("craftbukkit-1.8.jar")
  })
  api(project(":api"))
}