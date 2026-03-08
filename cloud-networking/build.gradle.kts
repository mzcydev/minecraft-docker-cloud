plugins {
    `java-library`
}

dependencies {
    api(project(":cloud-api"))
    api(project(":cloud-proto"))
    api("io.grpc:grpc-netty-shaded:1.67.1")
    implementation("io.grpc:grpc-protobuf:1.67.1")
    implementation("io.grpc:grpc-stub:1.67.1")
}
