plugins {
    id("idea")
    id("java")
    id("java-library")
    id("net.neoforged.moddev.legacyforge") version("2.0.107")
}

version = project.properties["mod_version"]!!
group = project.properties["mod_group"]!!

repositories {
    mavenLocal()

    maven("https://maven.neoforged.net/#/releases/")
    maven("https://www.cursemaven.com")
    maven("https://api.modrinth.com/maven")
    maven("https://modmaven.dev")
    maven("https://maven.createmod.net") // Create, Ponder, Flywheel
    maven("https://maven.ithundxr.dev/snapshots") // Registrate
    maven("https://maven.blamejared.com") // JEI, Vazkii's Mods
    maven("https://dl.zznty.ru/maven") // Create Factory Abstractions, Create Factory Logistics
    maven("https://maven.theillusivec4.top/") // Curios
    maven("https://maven.squiddev.cc") // CC: Tweaked
}

dependencies {
    implementation("com.simibubi.create:create-${property("minecraft_version")}:${property("create_version")}:slim") { isTransitive = false }
    implementation("net.createmod.ponder:Ponder-Forge-${property("minecraft_version")}:${property("ponder_version")}")
    compileOnly("dev.engine-room.flywheel:flywheel-forge-api-${property("minecraft_version")}:${property("flywheel_version")}")
    runtimeOnly("dev.engine-room.flywheel:flywheel-forge-${property("minecraft_version")}:${property("flywheel_version")}")
    implementation("com.tterrag.registrate:Registrate:${property("registrate_version")}")
    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")!!)
    implementation("io.github.llamalad7:mixinextras-forge:0.4.1")
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    implementation(jarJar("ru.zznty:create_factory_abstractions-${property("minecraft_version")}:1.4.7")!!)

    compileOnly("ru.zznty:create_factory_logistics-${property("minecraft_version")}:1.4.7")
    runtimeOnly("ru.zznty:create_factory_logistics-${property("minecraft_version")}:1.4.7")

    compileOnly("curse.maven:create-more-pipe-bombs-in-packages-1304635:6755828")

    compileOnly("top.theillusivec4.curios:curios-forge:${property("curios_version")}:api")
    runtimeOnly("top.theillusivec4.curios:curios-forge:${property("curios_version")}")

    compileOnly("cc.tweaked:cc-tweaked-${property("minecraft_version")}-core-api:${property("cc_tweaked_version")}")
    compileOnly("cc.tweaked:cc-tweaked-${property("minecraft_version")}-forge-api:${property("cc_tweaked_version")}")
    runtimeOnly("cc.tweaked:cc-tweaked-${property("minecraft_version")}-forge:${property("cc_tweaked_version")}")

    implementation("mezz.jei:jei-${property("minecraft_version")}-forge:${property("jei_version")}")

    compileOnly("maven.modrinth:supplementaries:${property("supplementaries_version")}-forge")

    // Dev QOL
//    runtimeOnly("curse.maven:jei-238222:7270446")
}

legacyForge {
    version = property("forge_version").toString()

    accessTransformers.from("src/main/resources/META-INF/accesstransformer.cfg")

    parchment {
        mappingsVersion = property("parchment_mappings_version")!!.toString()
        minecraftVersion = property("parchment_minecraft_version")!!.toString()
    }

    runs {
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel.set(org.slf4j.event.Level.DEBUG)
        }

        create("client") {
            client()
            systemProperty("forge.enabledGameTestNamespaces", property("mod_id")!!.toString())
        }

        create("server") {
            server()
            programArgument("--nogui")
            systemProperty("forge.enabledGameTestNamespaces", property("mod_id")!!.toString())
        }
    }

    mods {
        create(property("mod_id")!!.toString()) {
            sourceSet(sourceSets["main"])
        }
    }
}

mixin {
    config("${property("mod_id")}.mixins.json")
    add(sourceSets.main.get(), "${property("mod_id")}.refmap.json")
}

tasks.processResources {
    val props = project.providers.gradlePropertiesPrefixedBy("").get()
    inputs.properties(props)
    filesMatching("META-INF/mods.toml") { expand(props) }
}

tasks {
    jar {
        archiveBaseName.set("${rootProject.property("mod_id")}-forge")
        manifest.attributes("MixinConfigs" to "${rootProject.property("mod_id")}.mixins.json")
    }
}

sourceSets {
    main {
        java {
            srcDir("src")
        }
        resources {
            srcDir("src/generated/resources")
        }
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}