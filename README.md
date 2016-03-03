SwissFxKnife
============

Compile classes as a module:

```
javac -d mods/SwissFxKnife \
 src/SwissFxKnife/module-info.java \
 src/net/reini/swissfxknife/Main.java \
 src/net/reini/swissfxknife/Controller.java \
 src/net/reini/swissfxknife/Greeting.java
```

 Startable now with

```
java -modulepath mods -m SwissFxKnife/net.reini.swissfxknife.Main
```
