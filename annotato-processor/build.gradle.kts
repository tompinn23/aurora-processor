plugins {
    id("java-library")
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(project(":annotato-annotations"))

    compileOnly("io.avaje:avaje-prisms:1.39")
    annotationProcessor("io.avaje:avaje-prisms:1.39")
}

tasks.test {
    useJUnitPlatform()
}
