SwissFxKnife
============

Compile specific classes as a module:
```
javac -d mods/SwissFxKnife \
 src/SwissFxKnife/module-info.java \
 src/net/reini/swissfxknife/Main.java \
 src/net/reini/swissfxknife/Controller.java \
 src/net/reini/swissfxknife/Greeting.java
```

or a simpler way:
```
javac -d mods/SwissFxKnife $(find src -name "*.java")
```

Copy additional resources found:
```
find src/net/reini/swissfxknife -type f \
 \( ! -iname "*.java" -and ! -iname ".*" \) \
 -exec cp {} mods/SwissFxKnife/net/reini/swissfxknife/ \;
```

Startable now with:
```
java -modulepath mods -m SwissFxKnife/net.reini.swissfxknife.Main
```

To package use the following command:
```
mkdir mlib

jar -c -f mlib/swissfxknife.jar \
 --main-class net.reini.swissfxknife.Main \
 -C mods/SwissFxKnife .
```

Startable now with the module class path:
```
java -mp mlib -m SwissFxKnife
```

or the greeting class using:
```
java -mp mlib -m SwissFxKnife/net.reini.swissfxknife.Greeting
```

Now let's link the application:
```
jlink --modulepath $JAVA_HOME/jmods:mlib --addmods SwissFxKnife \
 --output SwissFxKnifeApp
```

If you want it even smaller you can strip the debug infos *-G* and improve the resource compression *-c* as follows:
```
jlink --modulepath $JAVA_HOME/jmods:mlib --addmods SwissFxKnife \
 --output SwissFxKnifeApp -G -c
```
