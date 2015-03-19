#! /bin/sh

classpath=..:/usr/share/java/junit.jar
javac -cp $classpath  BoardModelTest.java || exit 1
java -cp .:$classpath junit.textui.TestRunner BoardModelTest

