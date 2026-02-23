plugins {
    java
    id("org.openapi.generator") version "7.20.0"
    id("io.qameta.allure") version "3.0.2"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("tools.jackson.core:jackson-databind:3.0.4")
    implementation("io.rest-assured:rest-assured:6.0.0")
    implementation("io.qameta.allure:allure-rest-assured:2.32.0")

    testImplementation("org.testng:testng:7.12.0")
    testImplementation("io.qameta.allure:allure-testng:2.32.0")
    testImplementation("ch.qos.logback:logback-classic:1.5.32")
}

tasks.test {
    useTestNG()

    systemProperty("testng.dtd.http", "true")
}

tasks.register<Test>("testFull") {
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useTestNG {
        suites("src/test/resources/suites/full.xml")
    }
}

tasks.register<Test>("testSanity") {
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useTestNG {
        suites("src/test/resources/suites/sanity.xml")
    }
}

allure {
    version.set("3.0.2")

    adapter {
        frameworks {
            testng {
                enabled = true
            }
        }
    }
}
