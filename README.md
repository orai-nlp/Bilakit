Bilakit
=======

Bilakit is an open-source package for implementing multilingual (Basque, Spanish, English, French as of today) information retrieval functions on Solr. It is developed and maintained by Gotzon Santander and Xabier Saralegi (main contact) from Elhuyar Foundation. The main features Bilakit provides are:
+ Multilingual and cross-lingual information retrieval.
+ Multilingual lemmatization.
+ Multilingual entity recognition and classification.
+ Easily extensible to other languages by providing corresponding lexical data.


Contents
========

Solr plugins:
+ ElhuyarSolrPlugin/
+ ClassicTokenizerImpl.java         # Classic lucene StandardTokenizer up until 3.0
+ Dicfile.java                      # Methods related to bilingual and MWU dictionaries
+ Dictionary.java                   # Manipulation of Hunspell dictionaries
+ ElhuyarLemmatizerTokenizer.java   # Tokenizer based on Ixapipes and Eustagger
+ ElhuyarLemmatizerTokenizerFactory.java # Tokenizer based on Ixapipes and Eustagger
+ ElhuyarTextProcessorFactory.java       # Extension of the UpdateRequestProcessorFactory for including lemmatization, NERC and translation at index time
+ EustaggerLemmatizer.java               # Socket based client for using Eustagger, Basque POS lemmatizer
+ IxaPipesLemmatizer.java                # Socket based client for using IXA pipes linguistic processor (lemmatization and NERC)
+ IPText.java
+ ISO8859_14Decoder.java                 # Encoding for Hunspell dictionaries
+ LanguageDefiner.java                   # Language detector
+ PayloadQParserPlugin.java              # Query parser for CLIR
+ SimilarityCLIRFactory.java             # Extension of similarity for performing CLIR
+ Stemmer.java                           # Stemmer based on hunspell

Taggers' launchers:
+ ElhuyarSolrPlugin/
+ IPLemmatizerServer.java 
+ IPText.java
+ TextProcess.java

Language resources:
+ dicts/
+ MWUs/*             # MWU dictionaries (eu, es, fr, en)
+ bilingualdics/*    # Bilingual dictionaries (eu-es, eu-fr, eu-en)
+ stopwords/*        # Stopword lists (eu, es, fr, en)
+ Hunspelldics/*     # Hunspell dictionaries (eu, es, fr, en)
+ examplefiles/*     # Configuration files and example collection


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

INSTALLATION ON SOLR
====================

1. Copy Bilakit files to ../collection1/conf
-------------------------------------------
Plugin:
./bilakit/plugin/BilakitSolrPlugin0.9.jar

Language resources required by each L language:
+ Stopwords list: bilakit/stopwords/stopwords_$L.txt
+ Hunspell dictionary. bilakit/Hunspelldics/elhuyar/hunspell/$L.txt
+ POS and NERC tagger (optional):
  + IXA pipes: for es, en and fr languages (installation guide here).
  + Eustagger: for eu (installation guide here).

Language resources required by each L1->L2 language pair:
+ Multiword units list (optional): elhuyar/MWU_$L1.txt (format here).
+ Bilingual L1->L2 dictionary: elhuyar/dic_$L1L2.txt (format here).
+ Stopwords list: elhuyar/stopwords_$L2.txt.




