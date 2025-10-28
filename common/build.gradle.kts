architectury {
    val platforms = (rootProject.findProperty("enabled_platforms") as String)
        .split(',')
        .map(String::trim)
        .toTypedArray()
    common(*platforms) // vararg
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${rootProject.findProperty("fabric_loader_version")}")
    modImplementation("dev.architectury:architectury:${rootProject.findProperty("architectury_api_version")}")
    modCompileOnly("maven.modrinth:midnightlib:${rootProject.findProperty("midnightlib_version")}-fabric")
}
