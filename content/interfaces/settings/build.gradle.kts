plugins {
    id("base-conventions")
}

dependencies {
    implementation(projects.api.musicPlugin)
    implementation(projects.api.pluginCommons)
    implementation(projects.content.interfaces.gameframe)
}
