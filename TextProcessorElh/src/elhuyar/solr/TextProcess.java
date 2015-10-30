/*
 * Copyright 2015 Elhuyar Fundazioa

This file is part of Bilakit.

    Bilakit is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Bilakit is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Bilakit.  If not, see <http://www.gnu.org/licenses/>.
 */

package elhuyar.solr;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.jdom2.JDOMException;

import ixa.kaflib.Entity;
import ixa.kaflib.KAFDocument;
import ixa.kaflib.Term;

public class TextProcess {
	private KAFDocument kaf;

	public TextProcess(String text,String lang,String posModelPath,eus.ixa.ixa.pipe.pos.Annotate postagger,String nercModelPath,eus.ixa.ixa.pipe.nerc.Annotate nerctagger){
		try{
			this.kaf = newIxaPipesTokPosNERC(text,lang,posModelPath,postagger,nercModelPath,nerctagger);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Set<String> getNER(String termType){
		Set<String> NElist = new HashSet<String>();
		List<Entity> entities = this.kaf.getEntities();
		for(Entity ent : entities){
			if (ent.getType().equals(termType)){
				NElist.add(ent.getStr());
			}
		}
		return NElist;
	}

	public String getLemmatizedText(){
		String LemmatizedText = "";
		List<Term> terms = this.kaf.getTerms();
		for(Term term : terms){
			LemmatizedText += term.getLemma() + " "; 
		}
		return LemmatizedText.trim();
	}

	/**
	 * Tokenizes and NERC a given string with Ixa-pipes.
	 * 
	 * @param String text : input text
	 * @param String lang : input text language (ISO-639 code) 
	 * @param String nercModelPath : path to the NERC tagger model
	 * 
	 * @return KAFDocument : PoStagged and NERC tagged input text in KAF format
	 * 
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static KAFDocument ixaPipesTokNERC(String text, String lang, String nercModelPath, String lexer, String dictTag, String dictPath, String clearFeatures) throws IOException, JDOMException
	{
		return ixaPipesNERC(ixaPipesTok(text, lang),nercModelPath,lexer,dictTag, dictPath, clearFeatures);
	}
	
	/**
	 * Tokenizes, PoS tags and NERC a given string with Ixa-pipes.
	 * 
	 * @param String text : input text
	 * @param String lang : input text language (ISO-639 code) 
	 * @param String posModelPath : path to the pos tagger model
	 * @param String nercModelPath : path to the NERC tagger model
	 * 
	 * @return KAFDocument : PoStagged and NERC tagged input text in KAF format
	 * 
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static KAFDocument ixaPipesTokPosNERC(String text, String lang, String posModelPath, String nercModelPath, String lexer, String dictTag, String dictPath, String clearFeatures) throws IOException, JDOMException
	{
		return ixaPipesNERC(ixaPipesPos(ixaPipesTok(text, lang), posModelPath),nercModelPath,lexer,dictTag, dictPath, clearFeatures);
	}
	
	public static KAFDocument newIxaPipesTokPosNERC(String text, String lang, String posModelPath, eus.ixa.ixa.pipe.pos.Annotate postagger,String nercModelPath, eus.ixa.ixa.pipe.nerc.Annotate nerctagger) throws IOException, JDOMException
	{
		return newIxaPipesNERC(newIxaPipesPos(ixaPipesTok(text, lang), posModelPath,postagger),lang,nercModelPath,nerctagger);
	}

	/**
	 * Tokenizes and PoS tags a given string with Ixa-pipes.
	 * 
	 * @param String text : input text
	 * @param String lang : input text language (ISO-639 code) 
	 * @return KAFDocument : PoStagged input text in KAF format
	 * 
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static KAFDocument ixaPipesTokPos(String text, String lang, String posModelPath) throws IOException, JDOMException
	{
		return ixaPipesPos(ixaPipesTok(text, lang), posModelPath);
	}

	/**
	 * Processes a given string with the Ixa-pipe tokenizer.
	 * 
	 * @param String text : input text
	 * @return KAFDocument : tokenized input text in kaf format
	 * 
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static KAFDocument ixaPipesTok(String text, String lang) throws IOException, JDOMException
	{
		if (lang == "eu"){lang="es";}
		//kaf document to store tokenized text
		KAFDocument kaf = new KAFDocument(lang, "v1.naf");
		KAFDocument.LinguisticProcessor newLp = kaf.addLinguisticProcessor("text", "ixa-pipe-tok-"+lang, 
				"v1.naf" + "-" + "elh-absa");
		newLp.setBeginTimestamp();
		// objects needed to call the tokenizer
		BufferedReader breader = new BufferedReader(new StringReader(text));
		Properties tokProp = setTokenizerProperties(lang, "default", "no", "no");
		
		// tokenizer call
		eus.ixa.ixa.pipe.tok.Annotate tokenizer = new eus.ixa.ixa.pipe.tok.Annotate(breader,tokProp);
		tokenizer.tokenizeToKAF(kaf);
		newLp.setEndTimestamp();
		
		breader.close();
								
		return kaf;
	}

	/**
	 * Processes a given string with the Ixa-pipe PoS tagger.
	 * 
	 * @param KAFDocument tokenizedKaf: tokenized input text in KAF format
	 * @param String posModelPath : path to the pos tagger model
	 * 
	 * @return KAFDocument : PoStagged input text in KAF format
	 * 
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static KAFDocument ixaPipesPos(KAFDocument tokenizedKaf, String posModelPath) throws IOException, JDOMException
	{
		KAFDocument.LinguisticProcessor posLp = tokenizedKaf.addLinguisticProcessor(
				"terms", "ixa-pipe-pos-"+fileName(posModelPath), "v1.naf" + "-" + "elh-absa");			
		//pos tagger parameters
		if (! checkFile(posModelPath))
		{
			System.err.println("NLPpipelineWrapper::ixaPipesPos() - provided pos model path is problematic, "
					+ "probably pos tagging will end up badly...");
		}
		Properties posProp = setPostaggerProperties(posModelPath,
				tokenizedKaf.getLang(), "3", "bin", "false");
		//pos tagger call
		eus.ixa.ixa.pipe.pos.Annotate postagger = new eus.ixa.ixa.pipe.pos.Annotate(posProp);
		posLp.setBeginTimestamp();		
		postagger.annotatePOSToKAF(tokenizedKaf);
		posLp.setEndTimestamp();
		
		return tokenizedKaf;
	}
	
	public static eus.ixa.ixa.pipe.pos.Annotate getPostagger(String posModelPath,String lang) throws IOException
	{
		if (lang == "eu"){lang="es";}
		Properties posProp = setPostaggerProperties(posModelPath,lang, "3", "bin", "false");
		//pos tagger call
		eus.ixa.ixa.pipe.pos.Annotate postagger = new eus.ixa.ixa.pipe.pos.Annotate(posProp);
		return postagger;
	}
	
	public static KAFDocument newIxaPipesPos(KAFDocument tokenizedKaf, String posModelPath,eus.ixa.ixa.pipe.pos.Annotate postagger)
	{
		KAFDocument.LinguisticProcessor posLp = tokenizedKaf.addLinguisticProcessor(
				"terms", "ixa-pipe-pos-"+fileName(posModelPath), "v1.naf" + "-" + "elh-absa");			
		//pos tagger parameters
		if (! checkFile(posModelPath))
		{
			System.err.println("NLPpipelineWrapper::ixaPipesPos() - provided pos model path is problematic, "
					+ "probably pos tagging will end up badly...");
		}
		posLp.setBeginTimestamp();		
		postagger.annotatePOSToKAF(tokenizedKaf);
		posLp.setEndTimestamp();
		
		return tokenizedKaf;
	}
	
	/**
	 * Processes a given string with the Ixa-pipe NERC tagger.
	 * 
	 * @param KAFDocument tokenizedKaf: tokenized input text in KAF format
	 * @param String nercModelPath : path to the NERC tagger model
	 * 
	 * @return KAFDocument : NERC tagged input text in KAF format
	 * 
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static KAFDocument ixaPipesNERC(KAFDocument tokenizedKaf, String nercModelPath, String lexer, String dictTag, String dictPath, String clearFeatures) throws IOException, JDOMException
	{
		KAFDocument.LinguisticProcessor nercLp = tokenizedKaf.addLinguisticProcessor(
				"entities", "ixa-pipe-nerc-"+fileName(nercModelPath), "v1.naf" + "-" + "elh-absa");			
		//ner tagger parameters
		if (! checkFile(nercModelPath))
		{
			System.err.println("NLPpipelineWrapper : ixaPipesPos() - provided pos model path is problematic, "
					+ "probably pos tagging will end up badly...");
		}
		Properties nercProp = setIxaPipesNERCProperties(nercModelPath,
				tokenizedKaf.getLang(), lexer, dictTag, dictPath,clearFeatures);
		//ner tagger call
		eus.ixa.ixa.pipe.nerc.Annotate nerctagger = new eus.ixa.ixa.pipe.nerc.Annotate(nercProp);
		nercLp.setBeginTimestamp();		
		nerctagger.annotateNEs(tokenizedKaf);
		nercLp.setEndTimestamp();
		
		return tokenizedKaf;
	}
	
	public static eus.ixa.ixa.pipe.nerc.Annotate getNercTagger(String nercModelPath, String lang, String lexer, String dictTag, String dictPath, String clearFeatures) throws IOException
	{
		Properties nercProp = setIxaPipesNERCProperties(nercModelPath,
				lang, lexer, dictTag, dictPath,clearFeatures);
		//ner tagger call
		eus.ixa.ixa.pipe.nerc.Annotate nerctagger = new eus.ixa.ixa.pipe.nerc.Annotate(nercProp);
		return nerctagger;
	}
	
	public static KAFDocument newIxaPipesNERC(KAFDocument tokenizedKaf,String lang,String nercModelPath,eus.ixa.ixa.pipe.nerc.Annotate nerctagger) throws IOException, JDOMException
	{
		KAFDocument.LinguisticProcessor nercLp = tokenizedKaf.addLinguisticProcessor(
				"entities", "ixa-pipe-nerc-"+fileName(nercModelPath), "v1.naf" + "-" + "elh-absa");			
		//ner tagger parameters
		if (! checkFile(nercModelPath))
		{
			System.err.println("NLPpipelineWrapper : ixaPipesPos() - provided pos model path is problematic, "
					+ "probably pos tagging will end up badly...");
		}
		nercLp.setBeginTimestamp();
		if (lang.equals("eu")){tokenizedKaf.setLang("eu");}
		nerctagger.annotateNEs(tokenizedKaf);
		nercLp.setEndTimestamp();
		return tokenizedKaf;
	}

	/**
	*
	* Set properties for the Ixa-pipe-tok tokenizer module
	*
	* @param language (ISO-639 code)
	* @param normalize
	* @param untokenizable
	* @param hardParagraph
	*
	* @return Properties props
	*
	*/
	private static Properties setTokenizerProperties(String language, String normalize, String untokenizable, String hardParagraph) {
		Properties props = new Properties();
		props.setProperty("language", language);
		props.setProperty("normalize", normalize);
		props.setProperty("untokenizable", untokenizable);
		props.setProperty("hardParagraph", hardParagraph);
		return props;
	}

	/**
	 * 
	 * Set properties for the Ixa-pipe-pos tagger module
	 * 
	 * @param model
	 * @param language (ISO-639 code) 
	 * @param beamSize
	 * @param lemmatize
	 * @param multiwords
	 * 
	 * @return Properties props
	 * 
	 */
	private static Properties setPostaggerProperties(String model, String language, String beamSize, String lemmatize, String multiwords) {
		Properties props = new Properties();
		props.setProperty("model", model);
		props.setProperty("language", language);
		props.setProperty("beamSize", beamSize);
		props.setProperty("lemmatize", lemmatize);
		props.setProperty("multiwords", multiwords);
		return props;
	}
	
	/**
	 * Set a Properties object with the CLI parameters for annotation.
	 * @param model the model parameter
	 * @param language language parameter
	 * @param lexer rule based parameter
	 * @param dictTag directly tag from a dictionary
	 * @param dictPath directory to the dictionaries
	 * @return the properties object
	 */
	private static Properties setIxaPipesNERCProperties(String model, String language, String lexer, String dictTag, String dictPath, String clearFeatures) {
	    Properties annotateProperties = new Properties();
	    annotateProperties.setProperty("model", model);
	    annotateProperties.setProperty("language", language);
	    annotateProperties.setProperty("ruleBasedOption", lexer);
	    annotateProperties.setProperty("dictTag", dictTag);
	    annotateProperties.setProperty("dictPath", dictPath);
	    annotateProperties.setProperty("clearFeatures", clearFeatures);
		return annotateProperties;
	}
	
	/**
	* return the name (without path) of a string. The function assumes that the input is a file path.
	* 
	* @param fname : string to extract the name from
	* the directory
	*/
	public static String fileName(String fname) 
	{
		File f = new File(fname);
		return f.getName();
	}
	
	
	/**
	* Check input file integrity.
	* @param name
	* the name of the file
	* @param inFile
	* the file
	*/
	public static boolean checkFile(final String name) 
	{
		return checkFile(new File(name));
	}

	/**
	* Check input file integrity.
	* @param name
	* the name of the file
	* @param inFile
	* the file
	*/
	public static boolean checkFile(final File f) 
	{
		boolean isFailure = true;
		
		if (! f.isFile()) {
			isFailure = false;
		} 
		else if (!f.canRead()) {
			isFailure = false;
		}
		return isFailure;
	}
}
