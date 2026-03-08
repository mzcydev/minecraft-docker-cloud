dependencies {
    implementation("com.google.guava:guava:33.3.0-jre")
    implementation("org.yaml:snakeyaml:2.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")
}

configurations.compileOnly {
    exclude(group = "org.projectlombok")
}
configurations.annotationProcessor {
    exclude(group = "org.projectlombok")
}
