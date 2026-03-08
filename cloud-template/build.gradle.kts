plugins {
    `java-library`
}

dependencies {
    implementation(project(":cloud-api"))
    implementation(project(":cloud-proto"))

    // Jackson direkt deklarieren
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")

    // FTP
    implementation("commons-net:commons-net:3.11.1")

    // AWS S3
    implementation("software.amazon.awssdk:s3:2.28.17")
}
