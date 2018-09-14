/*
MIT License

Copyright (c) 2018 Janis Olekss

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.cordagen

import java.io.File

fun main(args: Array<String>) {
    when (args.size) {
        1 -> printCommandHelp(args[0])
        3 -> doCommand(args[0], args[1], args[2])
        else -> printHelp()
    }
}

fun printHelp() = println("""
WARNING::: This routine actually renames files and tampers with their content. Use at your own discretion.
Usage:

Renaming project related files (packages, directories, files)
java com.cordagen.MainKt rename <old_name> <new_name_of_the_project>

Generating new corda role and it's related files and directories
java com.cordagen.MainKt role <name_of_role>
""")

fun printCommandHelp(s: String) {
    when (s) {
        "rename" -> println("Please provide project old and new name after the 'name' command")
        "role" -> println("Please provide role name and project name")
        else -> println("I don't know how to do $s")
    }
}

fun doCommand(c: String, p1: String, p2: String) {
    when (c) {
        "rename" -> doRenaming(p1, p2)
        "role" -> doRoleGen(p1, p2)
    }
}

fun doRenaming(from: String, to: String) {
    // first transform directories
    File(".").walk().filterNot { skip(it.path) }.forEach { transformDirs(it, from, to) }
    // now, knowing dirs, we can transform files
    File(".").walk().filterNot { skip(it.path) }.forEach { transformFiles(it, from, to) }
}

fun skip(path: String) = (
        path.startsWith(".${File.separatorChar}.idea") ||
                path.startsWith(".${File.separatorChar}templates") ||
                path.startsWith(".${File.separatorChar}.gradle") ||
                path.startsWith(".${File.separatorChar}build") ||
                path.startsWith(".${File.separatorChar}src")
        )


fun transformDirs(f: File, from: String, to: String) {
    if (f.name.contains(from)) {
        println("$f > " + File(f.path.replace(from, to)))
        f.renameTo(File(f.path.replace(from, to)))
    }
}

fun transformFiles(f: File, from: String, to: String) {
    if (!f.isDirectory) {
        val content = f.readText()
        if (content.contains(from)) {
            println("--File ${f}")
            println(content.replace(from, to))
            println("--EOF")
            f.writeText(content.replace(from, to))
        }
    }
}

fun doRoleGen(role: String, project: String) {
    val roleDirectory = makeModuleDirecotry(role)
    makeModuleSourceAndTestDiretory(role, project, roleDirectory)
    copyGradle(roleDirectory)
    appendRoleToSettings(role)
    makeNode(role)
    makeWebModule(role, project)
}

fun makeModuleDirecotry(role: String): File {
    val file = File("./$role")
    println("Making module direcotry $role")
    if (file.exists()) {
        System.out.println("Direcotry ./$role already exist")
        System.exit(-2)
    }
    if (!file.mkdir()) {
        System.out.println("Can't create direotry ./$role")
        System.exit(-3)
    }
    return file
}

fun makeModuleSourceAndTestDiretory(role: String, project: String, file: File) {
    println("Creating module source direcotry")
    val src = File(file, "src/main/kotlin/com/$project/$role/")
    val test = File(file, "src/test/kotlin/com/$project/$role/")
    if (!src.mkdirs()) {
        System.out.println("Can't create source direcotry $src")
        System.exit(-4)
    }
    println("Creating module test direcotry")
    if (!test.mkdirs()) {
        System.out.println("Can't create test directory $test")
    }
}

fun copyGradle(file: File) {
    println("Copying build.gradle")
    File("./templates/role_build.gradle_")
            .copyTo(File(file, "build.gradle"), true)
}

fun appendRoleToSettings(role: String) {
    println("Updating settings.gradle")
    File("./settings.gradle").appendText("include '$role'\n")
}

// writes the node into gradle, essentially replaces // {{ROLE_NODE}} string with predefined content
fun makeNode(role: String) {
    println("Writing $role node definition to gradle")
    val mainGradle = File("./build.gradle")
    val content = mainGradle.readText()
    val newContent = content.replace("// {{ROLE_NODE}}",
            """node {
        name "O=${role.capitalize()},L=London,C=GB"
        p2pPort ${getNextNum()}
        rpcSettings {
            address("localhost:${getNextNum()}")
            adminAddress("localhost:${getNextNum()}")
        }
        cordapps = [
                "${'$'}project.group:$role:${'$'}project.version",
                "${'$'}project.group:contracts-states:${'$'}project.version",
                "${'$'}project.group:commons:${'$'}project.version",
        ]
        rpcUsers = [[ user: "user1", "password": "test", "permissions": ["ALL"]]]
    }
    // {{ROLE_NODE}}"""
    ).replace("// {{ROLE_CORDAPP}}", "cordapp project(':${role}')\n    // {{ROLE_CORDAPP}}")
    mainGradle.writeText(newContent)
}


// file containing port numbering delete or drop a single number if need specifics
fun getNextNum(): Int {
    val numFile = File("./num.tmp")
    if (!numFile.exists()) {
        numFile.writeText("10000")
        return 10000
    }
    val num = getNum() + 1
    numFile.writeText(num.toString())
    return num
}

fun getNum() = File("./num.tmp").readText().toInt()

fun makeWebModule(role: String, project: String) {
    val webDirectory = makeWebDirecotry(role, project)
    makeWebProperties(role)
    makeRoleController(role, project, webDirectory)
    makeWebGradleContent(role)
    updateWebMainGradle(role)
    updateWebScripts(role)
    addWebModuleToSettings(role)
}

fun makeWebDirecotry(role: String, project: String): File {
    val webDirectory = File("./web-$role/src/main/kotlin/com/$project/web/controllers/")
    if (webDirectory.exists()) {
        println("Web directory $webDirectory already exists")
        System.exit(-8)
    }
    println("Making web direcotry $webDirectory")
    if (!webDirectory.mkdirs()) {
        println("Can't create web directory $webDirectory")
        System.exit(-6)
    }
    return webDirectory
}

fun makeWebProperties(role: String) {
    println("Writing web properties file")
    val resources = File("./web-$role/src/main/resources/")
    if (!resources.mkdirs()) {
        println("Can't create resources directory $resources")
        System.exit(-7)
    }
    val port = getNum() - 1
    File(resources, "application-$role.properties").writeText(
            """server.port:${port + 10000}
config.rpc.host=localhost
config.rpc.port=${port}
config.rpc.username=user1
config.rpc.password=test
        """)
}

fun makeRoleController(role: String, project: String, webDirectory: File) {
    println("Writing empty role controller")
    val roleControlellerText = File("./templates/role_controller.kt_").readText()
    val newContent = roleControlellerText.replace("cordagen", project)
    File(webDirectory, "${role.capitalize()}Controller.kt").writeText(newContent)
}

fun makeWebGradleContent(role: String) {
    println("Writing web module gradle file")
    val webGradle = File("./templates/role_web_build.gradle_").readText()
    val newWebGradleContent = webGradle
            .replace("{{role}}", role)
    File("./web-$role/build.gradle").writeText(newWebGradleContent)
}

fun updateWebMainGradle(role: String) {
    println("Updating main gradle with web module")
    val gradle = File("./build.gradle").readText()
    val newContent = gradle
            .replace("// {{web_app}}", "'web-${role}:deployWebapp',\n    // {{web_app}}")
            .replace("// {{web_copy", """
    from('web-$role/build/webapps') {
        include '**/*.jar'
    }
    // {{web_copy}""")
    File("./build.gradle").writeText(newContent)
}

fun updateWebScripts(role: String) {
    println("Updaring web scripts")
    File("./src/main/resources/scripts/runwebapps.bat")
            .appendText("cmd /C start java -Dspring.profiles.active=$role -jar web-$role-0.1.jar\n")
    File("./src/main/resources/scripts/runwebapps.sh")
            .appendText("run_webapp \"Corda\" \"cd \\\"#DIR#\\\" &&  java -Dspring.profiles.active=$role -jar web-role-0.1.jar\" &\n")
}

fun addWebModuleToSettings(role: String) {
    println("Adding web module to settings.gradle")
    File("./settings.gradle").appendText("include 'web-$role'\n")
}