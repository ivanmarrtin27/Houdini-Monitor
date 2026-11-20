plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.oshi:oshi-core:6.8.2")
    implementation("io.github.mkpaz:atlantafx-base:2.1.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")
    implementation("org.slf4j:slf4j-api:2.0.17")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.18")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

javafx {
    version = "21.0.6"
    modules = listOf("javafx.controls", "javafx.graphics")
}

application {
    mainClass.set("com.houdini.monitor.Launcher")
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

tasks.test {
    useJUnitPlatform()
}

val packageName = "HoudiniMonitor"

tasks.register<Copy>("stageJpackageInput") {
    dependsOn(tasks.jar)
    into(layout.buildDirectory.dir("jpackage-input"))
    from(tasks.jar) {
        rename { "${packageName}.jar" }
    }
    from(configurations.runtimeClasspath)
}

tasks.register<Exec>("jpackage") {
    group = "distribution"
    description = "Create a Windows app-image with jpackage"
    dependsOn("stageJpackageInput")

    val javaHomeProvider = providers.environmentVariable("JAVA_HOME")
        .orElse(providers.systemProperty("java.home"))
    val inputDir = layout.buildDirectory.dir("jpackage-input")
    val destDir = layout.buildDirectory.dir("jpackage")

    doFirst {
        val javaHome = javaHomeProvider.get()
        val jpackage = file("$javaHome/bin/jpackage.exe")
        require(jpackage.exists()) {
            "jpackage not found at ${jpackage.absolutePath}. Set JAVA_HOME to a full JDK."
        }
        destDir.get().asFile.mkdirs()
        val previous = destDir.get().asFile.resolve(packageName)
        if (previous.exists()) {
            previous.deleteRecursively()
        }
        commandLine(
            jpackage.absolutePath,
            "--type", "app-image",
            "--name", packageName,
            "--app-version", version.toString(),
            "--dest", destDir.get().asFile.absolutePath,
            "--input", inputDir.get().asFile.absolutePath,
            "--main-jar", "${packageName}.jar",
            "--main-class", "com.houdini.monitor.Launcher",
            "--java-options", "--enable-native-access=ALL-UNNAMED"
        )
    }
}
