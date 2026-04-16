rootProject.name = "MineCore"

include("common")
project(":common").projectDir = file("common")
include("internal")
project(":internal").projectDir = file("internal")

sequenceOf("paper").forEach {
    val name = "platform-$it"
    if (file("platform/$it").exists()) {
        include(name)
        project(":$name").projectDir = file("platform/$it")
    }
}
