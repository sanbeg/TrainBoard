#! /bin/sh

classpath=..:/usr/share/java/junit4.jar
javac -cp $classpath  BoardModelTest.java
java -cp .:$classpath org.junit.runner.JUnitCore BoardModelTest

