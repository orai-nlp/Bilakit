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

package eu.solr.analysis;


import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;

import java.io.Reader;
import java.util.Map;


public class BasqueLemmatizerTokenizerFactory extends TokenizerFactory {
  private final int maxTokenLength;
  
  /** Creates a new BasqueLemmatizerTokenizerFactory */
  public BasqueLemmatizerTokenizerFactory(Map<String,String> args) {
    super(args);
    maxTokenLength = getInt(args, "maxTokenLength", StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH);
    if (!args.isEmpty()) {
      throw new IllegalArgumentException("Unknown parameters: " + args);
    }
  }

  @SuppressWarnings("deprecation")
@Override
  public BasqueLemmatizerTokenizer create(AttributeFactory factory, Reader input) {
    BasqueLemmatizerTokenizer tokenizer;
    if (luceneMatchVersion == null) {
      tokenizer = new BasqueLemmatizerTokenizer(factory, input);
    } else {
      tokenizer = new BasqueLemmatizerTokenizer(luceneMatchVersion, factory, input);
    }
    tokenizer.setMaxTokenLength(maxTokenLength);
    return tokenizer;
  }
}