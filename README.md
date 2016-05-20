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

WARNING: Only tested on solr-4.10.4


1. Copy Bilakit files to ../collection1/conf
-------------------------------------------
Plugin:
+ ./bilakit/plugin/BilakitSolrPlugin0.9.jar

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

2. Edit Solr’s solrconfig.xml
------------------------------

Enable *ElhuyarTextProcessorFactory*  plugin (for NERC and lemmatization tagging at index time):

````shell
<lib path="./plugin/BilakitSolrPlugin0.9.jar" />
<processor class="elhuyar.bilakit.ElhuyarTextProcessorFactory">
           <str name="languages">eu,es,en,fr</str>
    <str name="language_pairs">eu-es,eu-en,eu-fr</str>
           <str name="lemmatizer">ixapipes</str>
      </processor>
````

Required parameters:
+ languages: document languages. Currently supported: eu, es, en, and fr.
+ languages_pairs: Pairs for cross-lingual retrieval. Currently supported: eu<->es, eu<->en, eu<->fr.
+ lemmatizer: IXA-pipes or Hunspell. NERC only with IXA-pipes.

Required fields for collection documents:
+ *title_st*
+ *_st* (fields which will be lemmatized)
+ *language* =(eu|es|en|fr)
+ *title_$lang* which includes lemmatized title_st and translation in case of a different source language.
+ *source_title* which includes lemmatized title_st of source language. Needed for CLIR similarity computation. Set up automatically at index time.
+ *text_l$lang* which includes all of lemmatized *_st and translation in case of a different source language.
+ *source_text* which includes all of lemmatized *_st of source language. Needed for CLIR similarity computation.
+ *person*, *organization* and *location* which include entities identified when NERC is enabled.

Enable *elhuyar.solr.PayloadSimilarityFactory* similarity plugin (for building cross-lingual rankings) in solrconfig.xml:

````shell
<queryParser name="myparser" class="elhuyar.bilakit.PayloadQParserPlugin" />
````

3. Edit Solr’s schema.xml
---------------------------

Defining the following fields is mandatory:

+ *title_l$lang* and *text_l$lang* fields must be defined as *text_l$lang_payloads*. 
+ *text_l$lang_payloads* type. 

Enable the lemmatizer (IXA-pipes *elhuyar.solr.ElhuyarLemmatizerTokenizerFactory* or Hunspell *solr.HunspellStemFilterFactory*).

Select *elhuyar.solr.PayloadSimilarityFactory* similarity class for generating proper cross-lingual rankings.


````shell
<fieldType name="text_l$lang_payloads" class="solr.TextField" positionIncrementGap="100">
     <analyzer type="index">
   <tokenizer class="solr.WhitespaceTokenizerFactory"/>    
 <filter class="solr.DelimitedPayloadTokenFilterFactory" encoder="float"/>
<filter class="solr.StopFilterFactory" ignoreCase="true" words="stopwords/$lang_stopwords.txt"/>
       <filter class="solr.ASCIIFoldingFilterFactory"/>
     </analyzer>
     <analyzer type="query">
       <tokenizer class="elhuyar.bilakit.ElhuyarLemmatizerTokenizerFactory" lang="$lang"/>
       <filter class="solr.StopFilterFactory" ignoreCase="true" words="stopwords/$lang_stopwords.txt"/>
       <filter class="solr.LowerCaseFilterFactory"/>
       <filter class="solr.ASCIIFoldingFilterFactory"/>
     </analyzer>
     <similarity class="elhuyar.bilakit.PayloadSimilarityFactory" />
</fieldType>
````

Define title_$lang and text_$lang fields as text_$lang in schema.xml. These fields are just used for showing snippets.

````shell
<fieldType name="text_es" class="solr.TextField" positionIncrementGap="100">
     <analyzer>
       <charFilter class="solr.HTMLStripCharFilterFactory"/>
       <tokenizer class="solr.StandardTokenizerFactory"/>
       <filter class="solr.HunspellStemFilterFactory" dictionary="Hunspelldics/$lang_solr.dic" affix="Hunspelldics/$lang_solr.aff" ignoreCase="true" />
       <filter class="solr.StopFilterFactory" ignoreCase="true" words="stopwords/$lang_stopwords.txt" format="snowball"/>
       <filter class="solr.LowerCaseFilterFactory"/>
       <filter class="solr.ASCIIFoldingFilterFactory"/>
     </analyzer>
    </fieldType>
````

[If IXA-pipes used]
Define person, location and organization fields:

````shell
field name="person" type="string" indexed="true" stored="true" multiValued="true"/>
<field name="location" type="string" indexed="true" stored="true" multiValued="true"/>
<field name="organization" type="string" indexed="true" stored="true" multiValued="true"/>
````
