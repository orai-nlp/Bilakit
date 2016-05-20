
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




import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizerImpl;
import org.apache.lucene.analysis.standard.StandardTokenizerInterface;
import org.apache.lucene.analysis.standard.std31.StandardTokenizerImpl31;
import org.apache.lucene.analysis.standard.std34.StandardTokenizerImpl34;
import org.apache.lucene.analysis.standard.std40.StandardTokenizerImpl40;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeFactory;
import org.apache.lucene.util.Version;

/** 
 * Tokenizer based on Ixapipes and Eustagger
 */

@SuppressWarnings("deprecation")
public final class ElhuyarLemmatizerTokenizer extends Tokenizer {
	private IxaPipesLemmatizer IPLemmatizer;
	private EustaggerLemmatizer ELemmatizer;
	private String lang;
	private String lemmatizer;

    public String processReader(Reader reader){
        char[] arr = new char[8 * 1024]; // 8K at a time
        StringBuffer buf = new StringBuffer();
        int numChars;
        try {
			while ((numChars = reader.read(arr, 0, arr.length)) > 0) {
			    buf.append(arr, 0, numChars);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
        String text = buf.toString();
        String text_lemmatized = "";
        if (this.lang.equals("eu")){
        	text_lemmatized = this.ELemmatizer.getLemmatizedText(text);
        }
        else{
        	
        text_lemmatized = this.IPLemmatizer.getLemmatizedText(text,lang);	
        	
        }
    	return text_lemmatized;
    }

  /** A private instance of the JFlex-constructed scanner */
  private StandardTokenizerInterface scanner;

  public static final int ALPHANUM          = 0;
  /** @deprecated (3.1) */
  @Deprecated
  public static final int APOSTROPHE        = 1;
  /** @deprecated (3.1) */
  @Deprecated
  public static final int ACRONYM           = 2;
  /** @deprecated (3.1) */
  @Deprecated
  public static final int COMPANY           = 3;
  public static final int EMAIL             = 4;
  /** @deprecated (3.1) */
  @Deprecated
  public static final int HOST              = 5;
  public static final int NUM               = 6;
  /** @deprecated (3.1) */
  @Deprecated
  public static final int CJ                = 7;

  /** @deprecated (3.1) */
  @Deprecated
  public static final int ACRONYM_DEP       = 8;

  public static final int SOUTHEAST_ASIAN = 9;
  public static final int IDEOGRAPHIC = 10;
  public static final int HIRAGANA = 11;
  public static final int KATAKANA = 12;
  public static final int HANGUL = 13;
  
  /** String token types that correspond to token type int constants */
  public static final String [] TOKEN_TYPES = new String [] {
    "<ALPHANUM>",
    "<APOSTROPHE>",
    "<ACRONYM>",
    "<COMPANY>",
    "<EMAIL>",
    "<HOST>",
    "<NUM>",
    "<CJ>",
    "<ACRONYM_DEP>",
    "<SOUTHEAST_ASIAN>",
    "<IDEOGRAPHIC>",
    "<HIRAGANA>",
    "<KATAKANA>",
    "<HANGUL>"
  };
  
  private int skippedPositions;

  private int maxTokenLength = StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH;

  /** Set the max allowed token length.  Any token longer
   *  than this is skipped. */
  public void setMaxTokenLength(int length) {
    if (length < 1) {
      throw new IllegalArgumentException("maxTokenLength must be greater than zero");
    }
    this.maxTokenLength = length;
    if (scanner instanceof StandardTokenizerImpl) {
      scanner.setBufferSize(Math.min(length, 1024 * 1024)); // limit buffer size to 1M chars
    }
  }

  /** @see #setMaxTokenLength */
  public int getMaxTokenLength() {
    return maxTokenLength;
  }

  /**
   * Creates a new instance of the {@link org.apache.lucene.analysis.standard.StandardTokenizer}.  Attaches
   * the <code>input</code> to the newly created JFlex scanner.
   *
   * @param input The input reader
   *
   * See http://issues.apache.org/jira/browse/LUCENE-1068
   */
  public ElhuyarLemmatizerTokenizer(Reader input,String lang, String lemmatizer) {
    this(Version.LATEST, input, lang, lemmatizer);
  }

  /**
   * @deprecated Use {@link #StandardTokenizer(Reader)}
   */
  @Deprecated
  public ElhuyarLemmatizerTokenizer(Version matchVersion, Reader input,String lang, String lemmatizer) {
    super(input);
    init(matchVersion);
    this.lang = lang;
    this.lemmatizer = lemmatizer;
    if (!this.lang.equalsIgnoreCase("eu")){
    	
    		this.IPLemmatizer = new IxaPipesLemmatizer();
    	
    }
    else{
    	this.ELemmatizer =  new EustaggerLemmatizer();
    	if (this.lemmatizer.equalsIgnoreCase("ixapipes")){
    		this.IPLemmatizer = new IxaPipesLemmatizer();
    	}
    }
  }

  /**
   * Creates a new StandardTokenizer with a given {@link org.apache.lucene.util.AttributeFactory} 
   */
  public ElhuyarLemmatizerTokenizer(AttributeFactory factory, Reader input,String lang, String lemmatizer) {
    this(Version.LATEST, factory, input, lang, lemmatizer);
  }

  /**
   * @deprecated Use {@link #StandardTokenizer(AttributeFactory, Reader)}
   */
  @Deprecated
  public ElhuyarLemmatizerTokenizer(Version matchVersion, AttributeFactory factory, Reader input,String lang, String lemmatizer) {
    super(factory, input);
    init(matchVersion);
    this.lang = lang;
    this.lemmatizer = lemmatizer;
    if (!this.lang.equalsIgnoreCase("eu")){
    	
    		this.IPLemmatizer = new IxaPipesLemmatizer();
    	
    }
    else{
    	this.ELemmatizer =  new EustaggerLemmatizer();
    	if (this.lemmatizer.equalsIgnoreCase("ixapipes")){
    		this.IPLemmatizer = new IxaPipesLemmatizer();
    	}
    }
  }

  private final void init(Version matchVersion) {
    if (matchVersion.onOrAfter(Version.LUCENE_4_7)) {
      this.scanner = new StandardTokenizerImpl(input);
    } else if (matchVersion.onOrAfter(Version.LUCENE_4_0)) {
      this.scanner = new StandardTokenizerImpl40(input);
    } else if (matchVersion.onOrAfter(Version.LUCENE_3_4)) {
      this.scanner = new StandardTokenizerImpl34(input);
    } else if (matchVersion.onOrAfter(Version.LUCENE_3_1)) {
      this.scanner = new StandardTokenizerImpl31(input);
    } else {
      this.scanner = new ClassicTokenizerImpl(input);
    }
  }

  // this tokenizer generates three attributes:
  // term offset, positionIncrement and type
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
  private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
  private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

  /*
   * (non-Javadoc)
   *
   * @see org.apache.lucene.analysis.TokenStream#next()
   */
  @Override
  public final boolean incrementToken() throws IOException {
    clearAttributes();
    skippedPositions = 0;

    while(true) {
      int tokenType = scanner.getNextToken();

      if (tokenType == StandardTokenizerInterface.YYEOF) {
        return false;
      }

      if (scanner.yylength() <= maxTokenLength) {
        posIncrAtt.setPositionIncrement(skippedPositions+1);
        scanner.getText(termAtt);
        final int start = scanner.yychar();
        offsetAtt.setOffset(correctOffset(start), correctOffset(start+termAtt.length()));
        // This 'if' should be removed in the next release. For now, it converts
        // invalid acronyms to HOST. When removed, only the 'else' part should
        // remain.
        if (tokenType == ElhuyarLemmatizerTokenizer.ACRONYM_DEP) {
          typeAtt.setType(ElhuyarLemmatizerTokenizer.TOKEN_TYPES[ElhuyarLemmatizerTokenizer.HOST]);
          termAtt.setLength(termAtt.length() - 1); // remove extra '.'
        } else {
          typeAtt.setType(ElhuyarLemmatizerTokenizer.TOKEN_TYPES[tokenType]);
        }
        return true;
      } else
        // When we skip a too-long term, we still increment the
        // position increment
        skippedPositions++;
    }
  }
  
  @Override
  public final void end() throws IOException {
    super.end();
    // set final offset
    int finalOffset = correctOffset(scanner.yychar() + scanner.yylength());
    offsetAtt.setOffset(finalOffset, finalOffset);
    // adjust any skipped tokens
    posIncrAtt.setPositionIncrement(posIncrAtt.getPositionIncrement()+skippedPositions);
  }

  @Override
  public void close() throws IOException {
    super.close();
    scanner.yyreset(input);
  }

  @Override
  public void reset() throws IOException {
    super.reset();
    scanner.yyreset(new StringReader(processReader(input)));
    skippedPositions = 0;
  }
}