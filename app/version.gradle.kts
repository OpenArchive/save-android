import java.util.Properties

val versionPropsFile = file("version.properties")
val versionProps = Properties()

if (versionPropsFile.canRead()) {
    versionProps.load(versionPropsFile.inputStream())
}

val versionCode = versionProps.getProperty("versionCode", "1").toInt()
val versionName = versionProps.getProperty("versionName", "1.0.0.0")

project.extra.apply {
    set("versionCode", versionCode)
    set("versionName", versionName)
}

project.version = versionName

tasks.register("increaseVersionCode") {
    doLast {
        val code = versionProps.getProperty("versionCode", "0").toInt() + 1
        versionProps.setProperty("versionCode", code.toString())
        versionProps.store(versionPropsFile.outputStream(), null)
    }
}

tasks.register("increaseVersionName") {
    doLast {
        val (major, minor, patch) = versionProps.getProperty("versionName", "1.0.0.0").split('.').map { it.toInt() }
        val newVersionName = "$major.$minor.$patch.${versionCode}"
        versionProps.setProperty("versionName", newVersionName)
        versionProps.store(versionPropsFile.outputStream(), null)
    }
}