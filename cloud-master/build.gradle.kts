plugins { id("com.github.johnrengelman.shadow") }
dependencies {
    implementation(project(":cloud-api"))
    implementation(project(":cloud-networking"))
    implementation(project(":cloud-template"))
    implementation(project(":cloud-rest"))
    implementation("ch.qos.logback:logback-classic:1.5.8")
    implementation("org.jline:jline:3.26.3")
    implementation("org.yaml:snakeyaml:2.3")
    implementation("com.google.guava:guava:33.3.0-jre")
}
tasks.shadowJar {
    archiveClassifier.set("")
    manifest { attributes["Main-Class"] = "dev.cloud.master.MasterBootstrap" }
    mergeServiceFiles()
}
tasks.build { dependsOn(tasks.shadowJar) }
