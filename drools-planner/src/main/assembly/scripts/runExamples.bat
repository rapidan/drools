set mainJar=lib\drools-planner-examples-${project.version}.jar
set mainClass=org.drools.planner.examples.app.ExamplesApp

echo "Usage: runExamples.bat"
echo "For example: runExamples.bat"
echo "Some notes:"
echo "- Working dir should be the directory of this script."
echo "- Java is recommended to be JDK and java 6 for optimal performance"
echo "- The environment variable JAVA_HOME should be set to the JDK installation directory"
echo "  For example: set JAVA_HOME="C:\Program Files\Java\jdk1.6.0"
echo
echo "Starting examples app..."

# -Xmx128M or less works too, but it might be slower
"%JAVA_HOME%\bin\java" -Xms256m -Xmx512m -server -cp %mainJar% %mainClass%
