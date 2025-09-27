plugins {
    id("java-library")
    kotlin("jvm") version "2.0.21"
    kotlin("kapt") version "2.0.21"
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly("com.github.Moriafly:spw-workshop-api:0.1.0-dev14")
    kapt("com.github.Moriafly:spw-workshop-api:0.1.0-dev14")
}

val pluginClass = "com.zmxl.timeplugin.TimePlugin"
val pluginId = "com.zmxl.timeplugin"
val pluginVersion = "1.0.0"
val pluginProvider = "zmxl"

tasks.named<Jar>("jar") {
    manifest {
        attributes["Plugin-Class"] = pluginClass
        attributes["Plugin-Id"] = pluginId
        attributes["Plugin-Version"] = pluginVersion
        attributes["Plugin-Provider"] = pluginProvider
        attributes["Plugin-Name"] = "SaltTimePlugin"
        attributes["Plugin-Description"] = "保存播放器上次播放进度"
        attributes["Plugin-Open-Source-Url"] = "https://github.com/zmxlsss666/SaltTimePlugin"
    }
}

tasks.register<Jar>("plugin") {
    archiveBaseName.set("SaltTimePlugin-$pluginVersion")
    into("classes") {
        with(tasks.named<Jar>("jar").get())
    }
    dependsOn(configurations.runtimeClasspath)
    into("lib") {
        from({
            configurations.runtimeClasspath.get()
                .filter { it.name.endsWith("jar") }
        })
    }
    archiveExtension.set("zip")

}
