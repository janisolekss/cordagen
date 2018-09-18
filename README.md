
# Cordagen

Small utility "project/tool" to quickly wrap up role based Corda projects. It by itself is template project with self-modifying capabilities. Import it into IntelliJ as gradle project and then you can run it using arguments. Or it is possible to run from command line.

```
gradlew.bat build
```

To rename default cordagen package into your newname packages and files use this. Remember that root directory have to be renamed manually.
```
java -cp build/libs/cordagen-0.1.jar com.cordagen.MainKt rename cordagen newname
```

To add new corda role run this one. First argument is rolename and second argument is the name of your project (most likely one you renamed to by previous command)
```
java -cp build/libs/cordagen-0.1.jar com.cordagen.MainKt role seller newname
```


Main class is MainKt which can do two things

1. Rename all strings and directories in root directory (one you run java bla bla bla from), so use it carefully and recommend lowercase, as this will rename packages and imports
```
rename old_name new_name
```
2. Generate corda role module and corresponding web REST api module. Role name will be used to generate packages and directories, project name will be used to reference project related stuff. Using this architecture, you can have different role modules (bank, seller, buyer) which can have their own corDapps and still sharing and being able to "respond" on common initiators defined in commons module.
```
role role_name project_name
```

See the MainKt.kt file in src/main/kotlin/com/cordagen to see what and how it does. Use it with care, as there is no rollback and it is your file system the program tampers with.

Tested on Windows 10, not sure about linux/apple, most likely file permissions need to be updated manually to run scripts

After you are finished you are free to use the default command line commands
```
gradlew.bat deployNodes
gradlew.bat deployWebapps
call build/nodes/runnodes.bat
call build/webapps/runwebapps.bat
```
This will run your nodes and separate web server for each node connecting to corresponding node.


# WARNING
This is first time, one time use tool to quickly setup Corda project.
Don't use all this stuff on existing working project or when you are already mid-way, program is st00pid and overwrite and modify existing files without much thinking.