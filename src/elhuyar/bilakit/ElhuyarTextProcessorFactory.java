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


package elhuyar.bilakit;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.util.CharsRef;

import elhuyar.bilakit.EustaggerLemmatizer;




//Extension of the UpdateRequestProcessorFactory for including lemmatization, NERC and translation at index time

public class ElhuyarTextProcessorFactory extends UpdateRequestProcessorFactory
{			
	private String lemmatizer;
	private IxaPipesLemmatizer IPLemmatizer =new IxaPipesLemmatizer(); 
	private EustaggerLemmatizer ELemmatizer = new EustaggerLemmatizer();
	//For MWU dics
	private Map<String,Map<String, String>> MWUs = new LinkedHashMap<String, Map<String,String>>();	
	//For bilingual dics
	private Map<String,Map<String, String>> dics = new LinkedHashMap<String, Map<String,String>>();	
	//languages pairs
	private List<String> langpairs = new ArrayList<String>();			
	//hunspell stemmerrak
	private Map<String, Stemmer>  hunspells = new HashMap<String, Stemmer>();
	//stopwords
	private Map<String,List<String>> stopwds = new LinkedHashMap<String, List<String>>();
	
	
	@SuppressWarnings("rawtypes")
	public void init(NamedList args) {	
		if (args != null){
			SolrParams params = SolrParams.toSolrParams(args);			
			//get parameters:
			//Languages: es,eu,en,fr
			List<String> languages = Arrays.asList(params.get("languages").split(","));
			//language pairs es-eu, eu-en, eu-fr
			List<String> languagepairs=new ArrayList<String>();								
			if (params.get("language_pairs") != null)
			{	
				languagepairs = Arrays.asList(params.get("language_pairs").split(","));				
			}			
			//lemmatizer: ixapipes ala hunspell
			if (params.get("lemmatizer") != null)
			{	
				lemmatizer = params.get("lemmatizer","");
			}			
			//current path
			String current="";
			try {
				current = new java.io.File( "." ).getCanonicalPath();				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	        					
			//Load MWU files, lemmatizers and stopwords
			for (int i = 0; i < languages.size(); i++) 
			{							
				//load MWU files
				Map<String, String> MWU = new LinkedHashMap<String, String>();
				MWU=Dicfile.MWUFileLoad(current+"/solr/collection1/conf/bilakit/MWUs/MWU_"+languages.get(i)+".txt");				
				MWUs.put(languages.get(i), MWU);				
				//load stopwords
				List<String> stopwlist = new ArrayList<String>();
				stopwlist=Dicfile.stopwordLoad(current+"/solr/collection1/conf/bilakit/stopwords/"+languages.get(i)+"_stopwords.txt");
				stopwds.put(languages.get(i), stopwlist);
										
				//load lemmatizers
				if (lemmatizer.equals("ixapipes"))
				{	
					if (languages.get(i).equals("eu"))
					{
						ELemmatizer = new EustaggerLemmatizer();
					}
					else
					{
						IPLemmatizer =new IxaPipesLemmatizer();
					}
				}
				else if (lemmatizer.equals("hunspell"))
				{														
					Stemmer stemmer;					
					Dictionary dic;
					try {											
						dic=new Dictionary(new FileInputStream(current+"/solr/collection1/conf/bilakit/Hunspelldics/"+languages.get(i)+"_solr.aff"),new FileInputStream(current+"/solr/collection1/conf/bilakit/Hunspelldics/"+languages.get(i)+"_solr.dic"));						
						stemmer = new Stemmer(dic);
						hunspells.put(languages.get(i), stemmer);
					} catch (IOException
							| ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}							
			}																				
			//Load bilingual dictionaries
			for (int i = 0; i < languagepairs.size(); i++) 
			{	
						        		        
				Map<String, String> dic = new HashMap<String, String>();
				Map<String, String> dic2 = new HashMap<String, String>();							
				String[] pairitem = languagepairs.get(i).split("-");				
				dic=Dicfile.dicFileLoad(current+"/solr/collection1/conf/bilakit/bilingualdics/DicSolrPay_"+pairitem[0]+"-"+pairitem[1]+".txt");										
				dic2=Dicfile.dicFileLoad(current+"/solr/collection1/conf/bilakit/bilingualdics/DicSolrPay_"+pairitem[1]+"-"+pairitem[0]+".txt");
				dics.put(pairitem[0]+"-"+pairitem[1], dic);
				dics.put(pairitem[1]+"-"+pairitem[0], dic2);
				langpairs.add(pairitem[0]+"-"+pairitem[1]);
				langpairs.add(pairitem[1]+"-"+pairitem[0]);				
			}					
		}
	}
	@Override
	public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next)
	{	
		return new TexProcessor(next);
	}
	
	class TexProcessor extends UpdateRequestProcessor
	{
		public TexProcessor( UpdateRequestProcessor next) {
			super( next );
		}
		final Pattern REMOVE_TAGS = Pattern.compile("<.+?>");
		public String removeTags(String string) {
			if (string == null || string.isEmpty()) {
				return string;
			}	
			Matcher m = REMOVE_TAGS.matcher(string);
			return m.replaceAll(" ");
		}

		@Override
		public void processAdd(AddUpdateCommand cmd) throws IOException {
			SolrInputDocument doc = cmd.getSolrInputDocument();
			Object language_obj = doc.getFieldValue("language");
			List<String> field_values = new ArrayList<String>();			
			Object field_obj;
			//join text from every fields for langid (and for lemmatizing), when language field is empty			
			String full_text = "";
			//identify _st suffix including field names. All of them will be lemmatized  
			for (String fieldName : doc.getFieldNames()){
				if (fieldName.length() > 3 && (fieldName.substring(fieldName.length()-3).equalsIgnoreCase("_st"))){
					field_obj = doc.getFieldValue( fieldName );				
					if( field_obj != null ) {
						field_values.add(removeTags(StringEscapeUtils.unescapeHtml(field_obj.toString())));
						full_text += " " + field_obj.toString();
					}
				}
			}							
			String language = "";			
			//get or set language
			if(language_obj != null)
			{
				language = language_obj.toString();
			}
			
			String lang = language;						
			Object title_obj = doc.getFieldValue("title_st");					
			String title_value = "";
			String l_title_value = "";						
			if(title_obj != null) 
			{
				title_value = removeTags(StringEscapeUtils.unescapeHtml(title_obj.toString()));
			}			
			
			//remove | character. incompatible with payloads
			title_value=title_value.replaceAll("[|]", " ");
			full_text=full_text.replaceAll("[|]", " ");
			
			
			Set<String> person_list = new HashSet<String>();
			Set<String> location_list = new HashSet<String>();
			Set<String> organization_list = new HashSet<String>();
			//fields for lemmatised texts
			ArrayList<String> title_la = new ArrayList<String>();
			ArrayList<String> text_la = new ArrayList<String>();
			//for highlighting. Hunspell
			ArrayList<String> text_a = new ArrayList<String>();				
			//	Lemmatization of title_st, and  _st fields and extraction of entities from them											
			//System.out.println("TITLE TOKEN:  "+title_value);
			//System.out.println("TEXT TOKEN:  "+full_text);			
			//tagging title_st
			if(!title_value.isEmpty()) 
			{										
				//if ixapipes	
				if (lemmatizer.equals("ixapipes"))
				{					
					if (language.equalsIgnoreCase("eu"))
					{
						l_title_value = ELemmatizer.getLemmatizedText(title_value);
						IPLemmatizer.setText(title_value,lang);					
					}
					else
					{
						l_title_value = IPLemmatizer.getLemmatizedText(title_value,lang);					
					}														
					person_list.addAll(IPLemmatizer.getNER("PER"));
					location_list.addAll(IPLemmatizer.getNER("LOC"));
					organization_list.addAll(IPLemmatizer.getNER("ORG"));
				}						
				//if hunspell
				else if (lemmatizer.equals("hunspell"))
				{				
					//split the text
					String title=title_value.replaceAll("[\\n\\t.,?!;:\"“”()‘’' ]+", " ");										
					List<String> tokenak = Arrays.asList(title.split(" "));					
					for (int i = 0; i < tokenak.size(); i++) 
					{							
						String forma=tokenak.get(i);
						String lema="";						
						List<CharsRef> stemak;					
						stemak=hunspells.get(language).uniqueStems(forma.toCharArray(), forma.length());											
						//just the first one
						if (stemak.size()>0)
						{
							//if source eq language translate
							lema=stemak.get(0).toString();
						}
						else
						{
							lema=forma;
						}						
						l_title_value=l_title_value+" "+lema;
					}
				}				
				//lemmatized text to lc
				l_title_value=l_title_value.toLowerCase();		
				title_la.add(l_title_value);	
					//title ere itzuli?
			}								
			//tagging *_st fields and save the results in text_leu field. In text_eu only forms.
			if (! full_text.isEmpty())
			{				
				String l_full_text="";
				String l_full_text_MWU="";							
				//if ixapipes
				if (lemmatizer.equals("ixapipes"))
				{					
					if (language.equalsIgnoreCase("eu"))
					{
						l_full_text = ELemmatizer.getLemmatizedText(full_text);									
						IPLemmatizer.setText(full_text,lang);					
					}
					else
					{
						l_full_text = IPLemmatizer.getLemmatizedText(full_text,lang);
					}																
					//	extracting entities
					person_list.addAll(IPLemmatizer.getNER("PER"));
					location_list.addAll(IPLemmatizer.getNER("LOC"));
					organization_list.addAll(IPLemmatizer.getNER("ORG"));
				}							
				//if hunspell
				else if (lemmatizer.equals("hunspell"))
				{				
					//split the text
					String full_t=full_text.replaceAll("[\\n\\t.,?!;:\"“”()‘’' ]+", " ");									
					List<String> tokenak = Arrays.asList(full_t.split(" "));					
					for (int i = 0; i < tokenak.size(); i++) 
					{							
						String forma=tokenak.get(i);
						String lema="";						
						List<CharsRef> stemak;					
						stemak=hunspells.get(language).uniqueStems(forma.toCharArray(), forma.length());											
						//just the first one
						if (stemak.size()>0)
						{
							//if source eq language translate
							lema=stemak.get(0).toString();
						}
						else
						{
							lema=forma;
						}						
						l_full_text=l_full_text+" "+lema;
					}
				}										
				//lemmatized text to lc
				l_full_text =l_full_text.toLowerCase();
				text_la.add(l_full_text);				
				text_a.add(full_text);									
	//			System.out.println("Itzuli aurretik :"+l_full_text);																												
				//tagging MWU
				l_full_text_MWU=Dicfile.MWUtagOpt(l_full_text,MWUs.get(language));								
	//			System.out.println("MWU:  "+l_full_text_MWU);				
				//stopwordsak kendu						
				l_full_text_MWU=Dicfile.remove_stopwords(l_full_text_MWU,stopwds.get(language));				
	//			System.out.println("stopwordak kenduta :"+l_full_text_MWU);
				//Translations
//				System.out.println("MWUak markatu ondoren :"+l_full_text_MWU);
				for (int i = 0; i < langpairs.size(); i++)
				{					
					//if source eq language translate
					if (langpairs.get(i).startsWith(language+"-"))
					{
						String l_full_text_tr="";
						//for translation
						ArrayList<String> text_a_tr = new ArrayList<String>();	
						l_full_text_tr=Dicfile.itzul(l_full_text_MWU,dics.get(langpairs.get(i)));						
		//				System.out.println("Itzulpena :"+l_full_text_tr);	
						text_a_tr.add(l_full_text_tr);						
						String[] pairs = langpairs.get(i).split("-");																					
						//add the translation to the index
						if (!text_a_tr.isEmpty()){doc.addField( "text_l"+pairs[1], text_a_tr);}
					}					
				}																																
			}		
		
			
			// add lemmatized title, text and original text to the index
			if (!title_la.isEmpty())
				{
				doc.addField( "title_l"+language, title_la);				
				doc.addField( "source_title", title_la);
				//System.out.println("TITLE :"+title_la);
				}				
			if (!text_la.isEmpty())
				{
				doc.addField( "text_l"+language, text_la);			
				doc.addField( "source_text", text_la);	
				//System.out.println("TEXT :"+text_la);
				}				
			if (!text_a.isEmpty())
				{
				doc.addField( "text_"+language, text_a);
				}						
			//add entities to the index1
			doc.addField("person", person_list);
			doc.addField("location", location_list);
			doc.addField("organization", organization_list);
			// pass it up the chain
			super.processAdd(cmd);
		}
	}
}