plugins {
    `java-library`
}

dependencies {
    api(project(":cloud-api"))
    api("io.javalin:javalin:6.3.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")
    implementation("ch.qos.logback:logback-classic:1.5.8")
}
