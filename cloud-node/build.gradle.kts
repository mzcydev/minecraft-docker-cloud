plugins { id("com.github.johnrengelman.shadow") }
dependencies {
    implementation(project(":cloud-api"))
    implementation(project(":cloud-networking"))
    implementation(project(":cloud-template"))
    implementation(project(":cloud-docker"))
    implementation("ch.qos.logback:logback-classic:1.5.8")
    implementation("org.yaml:snakeyaml:2.3")
}
tasks.shadowJar {
    archiveClassifier.set("")
    manifest { attributes["Main-Class"] = "dev.cloud.node.NodeBootstrap" }
    mergeServiceFiles()
}
tasks.build { dependsOn(tasks.shadowJar) }
