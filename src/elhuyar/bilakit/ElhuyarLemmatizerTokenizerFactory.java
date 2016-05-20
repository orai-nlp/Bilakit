
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



import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;

import java.io.Reader;
import java.util.Map;

/** 
 * Tokenizer based on Ixapipes and Eustagger
 */

public class ElhuyarLemmatizerTokenizerFactory extends TokenizerFactory {
	private final String lang;
	private final String lemmatizer;
	private final int maxTokenLength;
		  
	/** Creates a new BasqueLemmatizerTokenizerFactory */
	public ElhuyarLemmatizerTokenizerFactory(Map<String,String> args) {
		super(args);
		
		if (args.containsKey("lang"))
		{
            this.lang = get(args,"lang");
		}
		else{
            this.lang = "es";
		}
		if (args.containsKey("lemmatizer")){
            this.lemmatizer = get(args,"lemmatizer");
		}
		else{
            this.lemmatizer = "";
		}
				

		maxTokenLength = getInt(args, "maxTokenLength", StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH);
		if (!args.isEmpty()) {
			throw new IllegalArgumentException("Unknown parameters: " + args);
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public ElhuyarLemmatizerTokenizer create(AttributeFactory factory, Reader input) {
		ElhuyarLemmatizerTokenizer tokenizer;
		if (luceneMatchVersion == null) {
			tokenizer = new ElhuyarLemmatizerTokenizer(factory, input, lang, lemmatizer);
		} else {
			tokenizer = new ElhuyarLemmatizerTokenizer(luceneMatchVersion, factory, input, lang, lemmatizer);
		}
		tokenizer.setMaxTokenLength(maxTokenLength);
		return tokenizer;
	}
}