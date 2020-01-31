###
# Precondition: Java is properly installed on the system and the path to the 'bin' folder of the java installation is part of the 'PATH' environment variable.
##
/*
 * Compile and run the java classes
 */ 
## Microsoft windows
dir /s /b *.java > sources
## Linux
find . -type f -name "*.java" > sources

mkdir build
javac -d build -g @sources
java -cp build com.unitrier.teaching.debugging.home.Main 100

/*
 * Run the java command line debugger
 */ 
jdb -sourcepath src -classpath build com.unitrier.teaching.debugging.home.Main 100