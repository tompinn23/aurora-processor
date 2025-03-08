import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("java-library")
    id("com.vanniktech.maven.publish") version "0.31.0-rc2"
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

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    pom {
        name.set("Aurora Annotation Processor")
        description.set("Small annotation processor doing useful things")
        inceptionYear.set("2020")
        url.set("https://github.com/tompinn23/aurora-processor/")
        licenses {
            license {
                name.set("MIT")
                url.set("https://github.com/tompinn23/aurora-processor/LICENSE")
            }
        }
        developers {
            developer {
                id.set("tompinn23")
                name.set("Tom Pinnock")
                url.set("https://github.com/tompinn23")
            }
        }
        scm {
            url.set("https://github.com/tompinn23/aurora-processor")
            connection.set("scm:git:git://github.com/tompinn23/aurora-processor.git")
            developerConnection.set("scm:git:ssh://git@github.com/tompinn23/aurora-processor.git")
        }
    }

    signAllPublications()
}