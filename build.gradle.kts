plugins {
    java
    application
}

group = "org.example"
version = "1.0.2"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

val javafxVersion = "21"
val platform = "win"



dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.openjfx:javafx-controls:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-graphics:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-base:$javafxVersion:$platform")
}

application {
    mainClass.set("org.example.launcher.Launcher")
}

tasks.jar {
    archiveFileName.set("HEDLauncher-Final.jar")

    // Manifest dosyasını ayarla
    manifest {
        attributes["Main-Class"] = "org.example.launcher.Launcher"
    }

    // Çakışan dosyaları engelle
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // TÜM KÜTÜPHANELERİ JAR İÇİNE GÖMÜYORUZ (Shadow yerine manuel Fat Jar)
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })

    // İmzalı jar dosyalarının (META-INF) güvenlik hatalarını önle
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}