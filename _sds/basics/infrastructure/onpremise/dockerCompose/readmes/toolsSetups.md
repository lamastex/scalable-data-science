# Summary
In order to work on the programming exercises locally, you need to have the following tools installed on your local machine:

1. Oracle JDK, the Java Development Kit, version 1.8. 
  - Check you have the right version by typing in the terminal: `java -version`
* Sbt, a build tool for Scala, version 0.13.x or newer.
* (optionally) [The Scala IDE for Eclipse](http://scala-ide.org/), [Intellij IDEA](https://www.jetbrains.com/idea/) or another IDE of your choice.

Follow the instructions below carefully.

# 1. Installing the JDK

## Linux

- **Ubuntu, Debian**: To install the JDK using `apt-get`, execute the following command in a terminal: `sudo apt-get install openjdk-8-jdk`
- **Fedora, Oracle, Red Had**: To install the JDK using `yum`, execute the following command in a terminal: `su -c "yum install java-1.8.0-openjdk-devel"`
- **Installing manually**: To install the JDK manually on a Linux system, follow these steps:
  1. Download the `.tar.gz` archive from the [Oracle website](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
  * Unpack the downloaded archive to a directory of your choice
  * Add the `bin/` directory of the extracted JDK to the `PATH` environment variable. Open the file `~/.bashrc` in a text editor (create it if it doesn't exist) and add the following line:
    - `export PATH="/PATH/TO/YOUR/jdk1.8.0-VERSION/bin:$PATH"` 
    - If you are nout using `BASH`, add the above line in the corresponding configuration file (e.g. `~/.zshrc` for `zsh`).

**Verify your setup**: Open a new terminal (or `source ~/.bashrc` if you installed manually) and type `java -version`. If you have problems installing the JDK, google search or in stack overflow.

## Mac OS X

Mac OS X either comes with a pre-installed JDK, or installs it automatically.

To verify your JDK installation, open the Terminal application in `/Applications/Utilities/` and type `java -version`. If the JDK is not yet installed, the system will ask you if you would like to download and install it. Make sure you install Java 1.8.

## Windows

1. Download the JDK installer for Windows from [the Oracle website](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* Run the installer
* Add the `bin\` directory of the installed JDK to the `PATH` environment variable, as described [here](http://www.java.com/en/download/help/path.xml)

To **verify the JDK installation**, open the Command Prompt and type java -version. If you have problems installing the JDK, ask for help on the forums.

## Installing sbt

sbt is already installed in `lamastex/hsbase` docker image.

You can also install it on your local machine, especially if you want to integrate it with IDEs you prefer.

Follow [these instructions](http://www.scala-sbt.org/release/docs/Setup.html) for your platform here to get it running.

We require sbt version **1.2.8** or higher (this is what is installed in `hsbase` so you can be sure that packaged jars from you local platform are also fine). 
If you have previously installed sbt version that is older, then you need to uninstall it and install a newer version. 
If in doubt, you can check your currently installed sbt in any directory that is **not** a programming assignment directory or otherwise an sbt project itself, as follows:

```
$ cd /tmp
$ sbt about
```

You should see a line similar to the following:

```
[info] This is sbt 1.2.8
```

If the `sbt` command is not found, you need to install `sbt` by following the [official instructions for your platform](https://www.scala-sbt.org/release/docs/Setup.html).

