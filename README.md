# Two Dimensional RLE
A two dimensional RLE (run length encoding) implementation for one dimensional input data, e.g. Strings.


### Build
- Clone Project
- build Project and all dependencies with maven `mvn clean install`
- if dependencies still missing ([jcenter bug](https://jfrog.com/blog/secure-jcenter-with-https)), run the dep.sh skript and try again
- alternatively you can just run the build.sh script
- to build jar run `mvn package -DskipTests`
- run Test with maven surefire plugin `mvn surefire-report:report` to execute all Test cases and get the results shown as a report in html.


### Usage
- build or download latest [release](https://github.com/fierg/twoDimensionalRLE/releases)
- run with JVM of your preference, but it performs better with the [GraalVM](https://www.graalvm.org/) instead of some Oracle or Open JVM
- further usage visible when run with `-h` or `--help`

```
Usage:
 [action] [method] [preprocessing] -f [files...]

ACTION:
-c 		compress file (default)
-d 		decompress file

METHOD:
-v #,#,#,#,#,#,#,#	 vertical reading (default), optional run lengths used on bit of significance 1 to 8 (N in range [2,32])
-bin #N 		 binary reading (with N bits per RLE encoded number, default 3)
-byte #N 		 byte wise reading (with N bits per RLE encoded number, default 3)

PREPROCESSING:
-bwt                                                  apply Burrows-Wheeler-Transformation during encoding
-map                                                apply Byte-mapping during encoding
-huf 		 encode with Huffman encoding

DEBUG:
-D 		debug log level
```


### Thesis

The latest version of the thesis is found [here](https://github.com/fierg/twoDimensionalRLE/blob/master/doc/thesis/thesis.pdf) 
