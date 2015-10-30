#!/bin/sh

command="package"
if [ $# -eq 1 ]
  then
  command=$1
fi

if [! -d "target" ]
then
    mkdir target
fi

# BasqueLemmatizer
cd BasqueLemmatizer
mvn clean $command
if [ $command == "package" ]
then 
    cp target/BasqueLemmatizer.jar ../target/
fi

# SpanishLemmatizer
cd SpanishLemmatizer
mvn clean $command
if [ $command == "package" ]
then
    cp target/SpanishLemmatizer.jar ../target/
fi

# TextProcessor
cd TextProcessor
mvn clean $command
if [ $command == "package" ]
then
    cp target/TextProcessor.jar ../target/
fi
