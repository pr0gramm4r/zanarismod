plugins {
    id("base-conventions")
    id("integration-test-suite")
}

dependencies {
    implementation(projects.api.pluginCommons)
    implementation(projects.api.scriptAdvanced)
    integrationImplementation(projects.api.music)
    integrationImplementation(projects.api.musicPlugin)
    integrationImplementation(projects.api.player)
    integrationImplementation(projects.engine.annotations)
}
