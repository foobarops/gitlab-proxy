tasks.withType<Jar> {
    this.archiveFileName.set("app.jar")
    
    // In order to add the external folder to classpath we use PropertiesLauncher
    this.manifest.attributes["Main-Class"] = "org.springframework.boot.loader.PropertiesLauncher"
    // This specifies the path where additional resources (like configuration files) can be found when the application is running.
    this.manifest.attributes["Loader-Path"] = "/app/resources"
}