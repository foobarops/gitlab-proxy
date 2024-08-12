tasks.withType<Jar> {
    this.archiveFileName.set("app.jar")
    this.manifest.attributes["Main-Class"] = "org.springframework.boot.loader.PropertiesLauncher"
    this.manifest.attributes["Loader-Path"] = "/app/resources"
}