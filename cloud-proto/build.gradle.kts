plugins {
    `java-library`
    id("com.google.protobuf")
}

dependencies {
    api("io.grpc:grpc-protobuf:1.67.1")
    api("io.grpc:grpc-stub:1.67.1")
    api("com.google.protobuf:protobuf-java:3.25.5")
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
}

protobuf {
    protoc { artifact = "com.google.protobuf:protoc:3.25.5" }
    plugins {
        create("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:1.67.1" }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("grpc")
            }
        }
    }
}

tasks.named<ProcessResources>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

configurations.compileOnly {
    exclude(group = "org.projectlombok")
}
configurations.annotationProcessor {
    exclude(group = "org.projectlombok")
}