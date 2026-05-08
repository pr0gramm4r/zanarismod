@file:Suppress("UnstableApiUsage")

import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

abstract class IntegrationTestExecutionService : BuildService<BuildServiceParameters.None>

val catalogs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val junitVersion by lazy { catalogs.findVersion("junit").get().requiredVersion }
val integrationTestExecutionService =
    gradle.sharedServices.registerIfAbsent(
        "integrationTestExecutionService",
        IntegrationTestExecutionService::class,
    ) {
        maxParallelUsages.set(1)
    }

plugins {
    `jvm-test-suite`
}

testing.suites {
    val integration by registering(JvmTestSuite::class) {
        useJUnitJupiter(junitVersion)
        targets.all {
            dependencies {
                implementation(project())
                implementation(project(":api:testing"))
            }
            testTask.configure {
                workingDir = rootDir
                usesService(integrationTestExecutionService)
                maxParallelForks = 1
                systemProperty("junit.jupiter.extensions.autodetection.enabled", true)
                systemProperty("junit.jupiter.execution.parallel.enabled", true)
                systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
                systemProperty("junit.jupiter.execution.parallel.mode.classes.default", "same_thread")
            }
        }
    }
}
