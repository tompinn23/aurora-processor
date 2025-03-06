plugins {
    id("java-library")
    id("signing")
}

repositories {
    mavenCentral()
}

group = "org.yonside"
version = "0.10.0"

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

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

artifacts {
    add("archives", tasks["javadocJar"])
    add("archives", tasks["sourcesJar"])
}

signing {
    sign(configurations.archives.get())
}