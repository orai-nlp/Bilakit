#!/bin/sh

command="package"
if [ $# -eq 1 ]
  then
  command=$1
fi

# build all packages
mvn clean $command

if [ ! -d "target" ]
then
    mkdir ./target
    echo "target dir created"
fi

if [ $command = "package" ]
then
    # BasqueLemmatizer
    cp BasqueLemmatizer/target/Bkit-BasqueLemmatizer-4.10.3.jar target/
    # SpanishLemmatizer
    cp SpanishLemmatizer/target/Bkit-SpanishLemmatizer-4.10.3.jar target/
    # TextProcessor
    cp TextProcessorElh/target/Bkit-TextProcessor-4.10.3.jar target/
    # TermLemmatizer
    cp TermLemmatizer/target/Bkit-TermLemmatizer-4.10.3.jar target/
fi

