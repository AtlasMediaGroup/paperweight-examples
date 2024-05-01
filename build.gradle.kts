plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("io.papermc.paperweight.patcher") version "1.6.3"
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"

repositories {
    mavenCentral()
    maven(paperMavenPublicUrl) {
        content { onlyForConfigurations(configurations.paperclip.name) }
    }
}

dependencies {
    remapper("net.fabricmc:tiny-remapper:0.8.11:fat")
    decompiler("net.minecraftforge:forgeflower:2.0.629.1")
    paperclip("io.papermc:paperclip:3.0.4-SNAPSHOT")
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
}

subprojects {
    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }
    tasks.withType<Test> {
        minHeapSize = "2g"
        maxHeapSize = "4g"
    }
    repositories {
        mavenCentral()
        maven(paperMavenPublicUrl)
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") // TODO - Adventure snapshot
    }
}

paperweight {
    serverProject.set(project(":Scissors-Server"))

    remapRepo.set(paperMavenPublicUrl)
    decompileRepo.set(paperMavenPublicUrl)

    usePaperUpstream(providers.gradleProperty("paperRef")) {
        withPaperPatcher {
            apiPatchDir.set(layout.projectDirectory.dir("patches/api"))
            apiOutputDir.set(layout.projectDirectory.dir("Scissors-API"))

            serverPatchDir.set(layout.projectDirectory.dir("patches/server"))
            serverOutputDir.set(layout.projectDirectory.dir("Scissors-Server"))

            patchTasks {
                register("generatedApi") {
                    isBareDirectory.set(true)
                    upstreamDirPath.set("paper-api-generator/generated")
                    patchDir.set(layout.projectDirectory.dir("patches/generatedApi"))
                    outputDir.set(layout.projectDirectory.dir("paper-api-generator/generated"))
                }
            }
        }
    }
}

tasks.generateDevelopmentBundle {
    apiCoordinates.set("me.totalfreedom.scissors:scissors-api")
    mojangApiCoordinates.set("io.papermc.paper:paper-mojangapi")
    libraryRepositories.set(
        listOf(
            "https://repo.maven.apache.org/maven2/",
            paperMavenPublicUrl,
            "https://repo.scissors.gg/repository/scissors-snapshot/",
        )
    )
}

allprojects {
    // Publishing API:
    // ./gradlew :Scissors-API:publish[ToMavenLocal]
    publishing {
        repositories {
            maven {
                name = "scissors-snapshots"
                url = uri("https://repo.scissors.gg/repository/scissors-snapshot/")
                credentials {
                    username = System.getenv("scissorsUser")
                    password = System.getenv("scissorsPassword")
                }
            }
        }
    }
}