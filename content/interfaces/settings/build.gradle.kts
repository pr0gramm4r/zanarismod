plugins {
    id("base-conventions")
    id("integration-test-suite")
}

dependencies {
    implementation(projects.api.musicPlugin)
    implementation(projects.api.pluginCommons)
    implementation(projects.content.interfaces.gameframe)
}
