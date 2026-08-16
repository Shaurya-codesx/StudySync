
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlinx.rpc)
}


application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}
dependencies {
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.auth.jwt)
    implementation(ktorLibs.server.callLogging)
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.resources)
    implementation(ktorLibs.server.statusPages)
    implementation(ktorLibs.server.websockets)
    implementation(libs.kotlinx.rpc.server)
    implementation(libs.logback.classic)
    implementation("org.jetbrains.exposed:exposed-java-time:0.41.1")
    implementation(project(":core"))

    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.postgresql:postgresql:42.6.0")

    implementation("org.mindrot:jbcrypt:0.4")

    implementation("io.ktor:ktor-server-auth-jwt")

    implementation("io.ktor:ktor-client-core-jvm")
    implementation("io.ktor:ktor-client-cio-jvm")
    implementation("io.ktor:ktor-client-content-negotiation-jvm")

    implementation("com.sun.mail:javax.mail:1.6.2")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation(ktorLibs.server.testHost)
    testImplementation(libs.kotlinx.rpc.client)
}

tasks.withType<Test> {
    environment("DATABASE_URL", "jdbc:postgresql://localhost:5432/studysync")
    environment("ENV", "test")
    environment("JWT_SECRET", "dummy_secret_for_tests")
    environment("AI_API_KEY", "dummy_api_key")
}
