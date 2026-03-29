rootProject.name = "MineCore"

include("common")
project(":common").projectDir = file("common")

sequenceOf("paper").forEach {
    val name = "platform-$it"
    if (file("platform/$it").exists()) {
        include(name)
        project(":$name").projectDir = file("platform/$it")
    }
}
