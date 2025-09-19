architectury {
    val platforms = (rootProject.findProperty("enabled_platforms") as String)
        .split(',')
        .map(String::trim)
        .toTypedArray()
    common(*platforms) // vararg
}

dependencies {
    add("modImplementation", "net.fabricmc:fabric-loader:${rootProject.findProperty("fabric_loader_version")}")
    add("modImplementation", "dev.architectury:architectury:${rootProject.findProperty("architectury_api_version")}")
    add("modImplementation", "dev.isxander:yet-another-config-lib:${rootProject.findProperty("yacl_version")}-fabric")
}
