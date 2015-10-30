Bilakit
=======

Bilakit Hizkuntza arteko bilaketa eleaniztunak egitea ahal bidetzen duen softwarea da. Apache Solr 4.10.3ren gainean lan egiten du, euskara, gaztelania eta ingelesaren arteko bilaketa ahalbidetuz.

Contents
========

Garatutako Solr-erako plugin-en zerrenda:

+ BasqueLemmatizer          #   Bilakit eta Tokikom zerbitzarian eustagger pluginaren kodea - Solr 4.10.3
+ TextProcessorElh          #   Bilakit demoan indexazioan lematizatzeko pluginaren kodea (eustagger, ixa-pipes) 
+ SpanishLemmatizer         #   Bilakit demorako freeling pluginaren kodea - Solr 4.10.3
+ TermLemmatizer            #   Itzulpen hiztegiak lematizatzeko exekutagarriaren kodea (eustagger, freeling, ixa-pipes)


INSTALLATION
============

Installing Bilakt requires the following steps:

If you already have installed in your machine JDK7 and MAVEN 3, please go to step 3
directly. Otherwise, follow these steps:

1. Install JDK 1.7
-------------------

If you do not install JDK 1.7 in a default location, you will probably need to configure the PATH in .bashrc or .bash_profile:

````shell
export JAVA_HOME=/yourpath/local/java17
export PATH=${JAVA_HOME}/bin:${PATH}
````

If you use tcsh you will need to specify it in your .login as follows:

````shell
setenv JAVA_HOME /usr/java/java17
setenv PATH ${JAVA_HOME}/bin:${PATH}
````

If you re-login into your shell and run the command

````shell
java -version
````

You should now see that your jdk is 1.7

2. Install MAVEN 3
------------------

Download MAVEN 3 from

````shell
wget http://apache.rediris.es/maven/maven-3/3.0.5/binaries/apache-maven-3.0.5-bin.tar.gz
````

Now you need to configure the PATH. For Bash Shell:

````shell
export MAVEN_HOME=/home/myuser/local/apache-maven-3.0.5
export PATH=${MAVEN_HOME}/bin:${PATH}
````

For tcsh shell:

````shell
setenv MAVEN3_HOME ~/local/apache-maven-3.0.5
setenv PATH ${MAVEN3}/bin:{PATH}
````

If you re-login into your shell and run the command

````shell
mvn -version
````

You should see reference to the MAVEN version you have just installed plus the JDK 6 that is using.

2. Get module source code
--------------------------

````shell
git clone https://bitbucket.org/elh-eus/bilakit
````

3. Installing using maven
---------------------------

````shell
cd bilakit
sh install.sh package
````

This step will create a directory called target/ which contains various directories and files. Most importantly, there you will find the plugin executables:

BasqueLemmatizer.jar
SpanishLemmatizer.jar
TextProcessor.jar

This executable contains every dependency the module needs, so it is completely portable as long
as you have a JVM 1.7 installed.

To install the module in the local maven repository, usually located in ~/.m2/, execute:

````shell
sh install.sh install
````