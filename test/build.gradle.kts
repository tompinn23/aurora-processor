plugins {
    id("java")
}

group = "uk.org.yonside"
version = "1.0-SNAPSHOT"

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

    annotationProcessor(project(":annotato-processor"))
    compileOnly(project(":annotato-annotations"))
}

tasks.test {
    useJUnitPlatform()
}