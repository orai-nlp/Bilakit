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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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

import elhuyar.solr.LanguageDefiner;
import elhuyar.solr.BasqueLemmatizer;
import eus.ixa.ixa.pipe.nerc.train.Flags;

public class ElhuyarTextProcessorFactory extends UpdateRequestProcessorFactory
{
	private Boolean hasEu;
	private Boolean hasEs;
	private Boolean hasEn;

	public void init(NamedList args) {
		if (args != null){
			SolrParams params = SolrParams.toSolrParams(args);
			List<String> languages = Arrays.asList(params.get("languages").split(","));
			hasEu = languages.contains("eu");
			hasEs = languages.contains("es");
			hasEn = languages.contains("en");
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
		    if (string == null || string.length() == 0) {
		        return string;
		    }
	
		    Matcher m = REMOVE_TAGS.matcher(string);
		    return m.replaceAll(" ");
		}
		
		@Override
		public void processAdd(AddUpdateCommand cmd) throws IOException {
			String nerDir = "solr/lib/ixa-pipes/";
			String lexer = Flags.DEFAULT_LEXER;
			String dictTag = Flags.DEFAULT_DICT_OPTION;
			String dictPath = Flags.DEFAULT_DICT_PATH;
			String clearFeatures = Flags.DEFAULT_FEATURE_FLAG;

			SolrInputDocument doc = cmd.getSolrInputDocument();
			
			Object language_obj = doc.getFieldValue( "language" );
	
			List<String> field_values = new ArrayList<String>();
			
			Object field_obj;
			String full_text = "";
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
			if(language_obj != null){
				language = language_obj.toString();
			}
			else{
				LanguageDefiner langDefiner =  new LanguageDefiner();
				language = langDefiner.getLanguage(full_text);
				doc.addField( "language", language);
			}
			String lang = language;

			Object title_obj = doc.getFieldValue("title_st");
			String title_value = "";
			String l_title_value = "";
			if(title_obj != null) {
				title_value = removeTags(StringEscapeUtils.unescapeHtml(title_obj.toString()));
			}
			
			Set<String> person_list = new HashSet<String>();
			Set<String> location_list = new HashSet<String>();
			Set<String> organization_list = new HashSet<String>();
			/*ArrayList<String> person_list = new ArrayList<String>();
			ArrayList<String> location_list = new ArrayList<String>();
			ArrayList<String> organization_list = new ArrayList<String>();*/

			if (language.equals("eu")){
				BasqueLemmatizer basqueLemmatizer =  new BasqueLemmatizer();
				String posModelPath = nerDir+"pos-models-1.4.0/es/es-maxent-100-c5-baseline-autodict01-ancora.bin";
				String nercModelPath = nerDir+"nerc-models-1.5.0/eu/eu-clusters-egunkaria.bin";
				
				eus.ixa.ixa.pipe.pos.Annotate postagger = TextProcess.getPostagger(posModelPath,"es");
				eus.ixa.ixa.pipe.nerc.Annotate nerctagger = TextProcess.getNercTagger(nercModelPath,lang,lexer,dictTag,dictPath,clearFeatures);

				ArrayList<String> title_leu = new ArrayList<String>();
				ArrayList<String> text_leu = new ArrayList<String>();
				//for highlighting
				ArrayList<String> text_eu = new ArrayList<String>();
				
				ArrayList<String> title_leu2en = new ArrayList<String>();
				ArrayList<String> text_leu2en = new ArrayList<String>();
				ArrayList<String> text_eu2en = new ArrayList<String>();
				ArrayList<String> title_leu2es = new ArrayList<String>();
				ArrayList<String> text_leu2es = new ArrayList<String>();
				ArrayList<String> text_eu2es = new ArrayList<String>();
				
				if(title_value.length() > 0) {
					l_title_value = basqueLemmatizer.getLemma(title_value);
					TextProcess title_value_text = new TextProcess(title_value,lang,posModelPath,postagger,nercModelPath,nerctagger);
					person_list.addAll(title_value_text.getNER("PER"));
					location_list.addAll(title_value_text.getNER("LOC"));
					organization_list.addAll(title_value_text.getNER("ORG"));

					title_leu.add(l_title_value);
					if (hasEn){title_leu2en.add(l_title_value);}
					if (hasEs){title_leu2es.add(l_title_value);}
				}

				for (String field_value: field_values){
					if (!field_value.isEmpty()){
						String l_field_value = basqueLemmatizer.getLemma(field_value);
						TextProcess field_value_text = new TextProcess(field_value,lang,posModelPath,postagger,nercModelPath,nerctagger);
						person_list.addAll(field_value_text.getNER("PER"));
						location_list.addAll(field_value_text.getNER("LOC"));
						organization_list.addAll(field_value_text.getNER("ORG"));
						
						text_leu.add(l_field_value);
						if (hasEn){text_leu2en.add(l_field_value);}
						if (hasEs){text_leu2es.add(l_field_value);}
						text_eu.add(field_value);
						if (hasEn){text_eu2en.add(field_value);}
						if (hasEs){text_eu2es.add(field_value);}
					}
				}
				if (!title_leu.isEmpty()){doc.addField( "title_leu", title_leu);}
				if (hasEn && !title_leu2en.isEmpty()){doc.addField( "title_leu2en", title_leu2en);}
				if (hasEs && !title_leu2es.isEmpty()){doc.addField( "title_leu2es", title_leu2es);}
				if (!text_leu.isEmpty()){doc.addField( "text_leu", text_leu);}
				if (hasEs && !text_leu2es.isEmpty()){doc.addField( "text_leu2es", text_leu2es);}
				if (hasEn && !text_leu2en.isEmpty()){doc.addField( "text_leu2en", text_leu2en);}
				if (!text_eu.isEmpty()){doc.addField( "text_eu", text_eu);}
				if (hasEs && !text_eu2es.isEmpty()){doc.addField( "text_eu2es", text_eu2es);}
				if (hasEn && !text_eu2en.isEmpty()){doc.addField( "text_eu2en", text_eu2en);}
			}
			else if (language.equals("en")){
				String posModelPath = nerDir+"pos-models-1.4.0/en/en-maxent-100-c5-baseline-dict-penn.bin";
				String nercModelPath = nerDir+"nerc-models-1.5.0/en/conll03/en-light-clusters-conll03.bin";
				
				eus.ixa.ixa.pipe.pos.Annotate postagger = TextProcess.getPostagger(posModelPath,lang);
				eus.ixa.ixa.pipe.nerc.Annotate nerctagger = TextProcess.getNercTagger(nercModelPath,lang,lexer,dictTag,dictPath,clearFeatures);

				ArrayList<String> title_len = new ArrayList<String>();
				ArrayList<String> text_len = new ArrayList<String>();
				//for highlighting
				ArrayList<String> text_en = new ArrayList<String>();
				
				ArrayList<String> title_len2eu = new ArrayList<String>();
				ArrayList<String> text_len2eu = new ArrayList<String>();
				ArrayList<String> text_en2eu = new ArrayList<String>();
				ArrayList<String> title_len2es = new ArrayList<String>();
				ArrayList<String> text_len2es = new ArrayList<String>();
				ArrayList<String> text_en2es = new ArrayList<String>();

				if(title_value.length() > 0) {
					TextProcess title_value_text = new TextProcess(title_value,lang,posModelPath,postagger,nercModelPath,nerctagger);
					l_title_value = title_value_text.getLemmatizedText();
					person_list.addAll(title_value_text.getNER("PER"));
					location_list.addAll(title_value_text.getNER("LOC"));
					organization_list.addAll(title_value_text.getNER("ORG"));
					
					title_len.add(l_title_value);
					if (hasEu){title_len2eu.add(l_title_value);}
					if (hasEs){title_len2es.add(l_title_value);}
				}
				
				for (String field_value: field_values){
					if (!field_value.isEmpty()){
						TextProcess field_value_text = new TextProcess(field_value,lang,posModelPath,postagger,nercModelPath,nerctagger);
						String l_field_value = field_value_text.getLemmatizedText();
						person_list.addAll(field_value_text.getNER("PER"));
						location_list.addAll(field_value_text.getNER("LOC"));
						organization_list.addAll(field_value_text.getNER("ORG"));
						
						text_len.add(l_field_value);
						if (hasEu){text_len2eu.add(l_field_value);}
						if (hasEs){text_len2es.add(l_field_value);}
						text_en.add(field_value);
						if (hasEu){text_en2eu.add(field_value);}
						if (hasEs){text_en2es.add(field_value);}
					}
				}
				if(!title_len.isEmpty()){doc.addField( "title_len", title_len);}
				if (hasEn && hasEu && !title_len2eu.isEmpty()){doc.addField( "title_len2eu", title_len2eu);}
				if (hasEn && hasEs && !title_len2es.isEmpty()){doc.addField( "title_len2es", title_len2es);}
				if (!text_len.isEmpty()){doc.addField( "text_len", text_len);}
				if (hasEu && !text_len2eu.isEmpty()){doc.addField( "text_len2eu", text_len2eu);}
				if (hasEs && !text_len2es.isEmpty()){doc.addField( "text_len2es", text_len2es);}
				if (!text_en.isEmpty()){doc.addField( "text_en", text_en);}
				if (hasEu && !text_en2eu.isEmpty()){doc.addField( "text_en2eu", text_en2eu);}
				if (hasEs && !text_en2es.isEmpty()){doc.addField( "text_en2es", text_en2es);}
			}
			else if (language.equals("es")){
				String posModelPath = nerDir+"pos-models-1.4.0/es/es-maxent-100-c5-baseline-autodict01-ancora.bin";
				String nercModelPath = nerDir+"nerc-models-1.5.0/es/es-clusters-conll02.bin";

				eus.ixa.ixa.pipe.pos.Annotate postagger = TextProcess.getPostagger(posModelPath,lang);
				eus.ixa.ixa.pipe.nerc.Annotate nerctagger = TextProcess.getNercTagger(nercModelPath,lang,lexer,dictTag,dictPath,clearFeatures);

				ArrayList<String> title_les = new ArrayList<String>();
				ArrayList<String> text_les = new ArrayList<String>();
				//for highlighting
				ArrayList<String> text_es = new ArrayList<String>();

				ArrayList<String> title_les2eu = new ArrayList<String>();
				ArrayList<String> text_les2eu = new ArrayList<String>();
				ArrayList<String> text_es2eu = new ArrayList<String>();
				ArrayList<String> title_les2en = new ArrayList<String>();
				ArrayList<String> text_les2en = new ArrayList<String>();
				ArrayList<String> text_es2en = new ArrayList<String>();

				if(title_value.length() > 0) {
					TextProcess title_value_text = new TextProcess(title_value,lang,posModelPath,postagger,nercModelPath,nerctagger);
					l_title_value = title_value_text.getLemmatizedText();
					person_list.addAll(title_value_text.getNER("PER"));
					location_list.addAll(title_value_text.getNER("LOC"));
					organization_list.addAll(title_value_text.getNER("ORG"));

					title_les.add(l_title_value);
					if (hasEu){title_les2eu.add(l_title_value);}
					if (hasEn){title_les2en.add(l_title_value);}
				}
				
				for (String field_value: field_values){
					if (!field_value.isEmpty()){
						TextProcess field_value_text = new TextProcess(field_value,lang,posModelPath,postagger,nercModelPath,nerctagger);
						String l_field_value = field_value_text.getLemmatizedText();
						person_list.addAll(field_value_text.getNER("PER"));
						location_list.addAll(field_value_text.getNER("LOC"));
						organization_list.addAll(field_value_text.getNER("ORG"));
						
						text_les.add(l_field_value);
						if (hasEu){text_les2eu.add(l_field_value);}
						if (hasEn){text_les2en.add(l_field_value);}
						text_es.add(field_value);
						if (hasEu){text_es2eu.add(field_value);}
						if (hasEn){text_es2en.add(field_value);}
					}
				}
				if(!title_les.isEmpty()){doc.addField( "title_les", title_les);}
				if (hasEu && !title_les2eu.isEmpty()){doc.addField( "title_les2eu", title_les2eu);}
				if (hasEn && !title_les2en.isEmpty()){doc.addField( "title_les2en", title_les2en);}
				if (!text_les.isEmpty()){doc.addField( "text_les", text_les);}
				if (hasEu && !text_les2eu.isEmpty()){doc.addField( "text_les2eu", text_les2eu);}
				if (hasEn && !text_les2en.isEmpty()){doc.addField( "text_les2en", text_les2en);}
				if (!text_es.isEmpty()){doc.addField( "text_es", text_es);}
				if (hasEu && !text_es2eu.isEmpty()){doc.addField( "text_es2eu", text_es2eu);}
				if (hasEn && !text_es2en.isEmpty()){doc.addField( "text_es2en", text_es2en);}
			}

			doc.addField("person", person_list);
			doc.addField("location", location_list);
			doc.addField("organization", organization_list);

			// pass it up the chain
			super.processAdd(cmd);
		}
	}
}