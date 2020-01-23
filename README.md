# Two Dimensional RLE
A two dimensional RLE (run length encoding) implementation for one dimensional input data, e.g. Strings.


### Usage
- Clone Project
- build Project and all dependencies with maven `mvn clean install`
- if dependencies still missing ([jcenter bug](https://jfrog.com/blog/secure-jcenter-with-https)), run the dep.sh skript and try again
- alternatively you can just run the build.sh script
- to build jar run `mvn package -DskipTests`
- further usage visible when run with `java -jar target/twoDimensionalRLE-1.0-SNAPSHOT-jar-with-dependencies.jar --help`

- run Test with maven surefire plugin `mvn surefire-report:report` to execute all Test cases and get the results shown as a report in html.

- it performs better with the [GraalVM](https://www.graalvm.org/) instead of some Oracle or OpenJDK


### Thesis

The latest version of the thesis is found [here](https://github.com/fierg/twoDimensionalRLE/blob/master/doc/thesis/thesis.pdf) 
