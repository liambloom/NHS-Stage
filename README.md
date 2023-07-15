# Stage Builder for NHS

Stage builder for NHS is a program I created as president of my high school's National Honor
Society (co '23). Provided a list of members, plus some basic information about them, it produces a chart
with the stage layout and lineup order for members going onto the stage for the induction ceremony.

### Releases

For installers and compiled app images go to the [releases page](https://github.com/liambloom/NHS-Stage/releases)

### Using source code

If you want to work with the source code yourself, you can clone this repository. Since it
is built using maven, you can compile the code with `mvn compile` and run it with `mvn javafx:run`,
or you can use the built in functions of your IDE. To package it into an installer, you can run 
`package.ps1`. For more detailed help on using `package`, run `help ./package` in Powershell.

Note that this is written using Java 17, so you will need JDK 17 in order to compile, run, or package
this program from source code. Packaging might have additional platform-specific requirements, see
[jpackage pre-reqs](https://docs.oracle.com/en/java/javase/20/jpackage/packaging-overview.html#GUID-786E15C0-2CE7-4BDF-9B2F-AC1C57249134)
for details
