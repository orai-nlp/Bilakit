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

package es.solr.analysis;

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

/** A grammar-based tokenizer constructed with JFlex.
 * <p>
 * As of Lucene version 3.1, this class implements the Word Break rules from the
 * Unicode Text Segmentation algorithm, as specified in 
 * <a href="http://unicode.org/reports/tr29/">Unicode Standard Annex #29</a>.
 * <p/>
 * <p>Many applications have specific tokenizer needs.  If this tokenizer does
 * not suit your application, please consider copying this source code
 * directory to your project and maintaining your own grammar-based tokenizer.
 *
 * <a name="version"/>
 * <p>You must specify the required {@link Version}
 * compatibility when creating StandardTokenizer:
 * <ul>
 *   <li> As of 3.4, Hiragana and Han characters are no longer wrongly split
 *   from their combining characters. If you use a previous version number,
 *   you get the exact broken behavior for backwards compatibility.
 *   <li> As of 3.1, StandardTokenizer implements Unicode text segmentation.
 *   If you use a previous version number, you get the exact behavior of
 *   {@link ClassicTokenizer} for backwards compatibility.
 * </ul>
 */

@SuppressWarnings("deprecation")
public final class SpanishLemmatizerTokenizer extends Tokenizer {
    private static SpanishLemmatizer eslem = new SpanishLemmatizer();

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
        String text_lemmatized = eslem.getLemma(text);
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
  public SpanishLemmatizerTokenizer(Reader input) {
    this(Version.LATEST, input);
  }

  /**
   * @deprecated Use {@link #StandardTokenizer(Reader)}
   */
  @Deprecated
  public SpanishLemmatizerTokenizer(Version matchVersion, Reader input) {
    super(input);
    init(matchVersion);
  }

  /**
   * Creates a new StandardTokenizer with a given {@link org.apache.lucene.util.AttributeFactory} 
   */
  public SpanishLemmatizerTokenizer(AttributeFactory factory, Reader input) {
    this(Version.LATEST, factory, input);
  }

  /**
   * @deprecated Use {@link #StandardTokenizer(AttributeFactory, Reader)}
   */
  @Deprecated
  public SpanishLemmatizerTokenizer(Version matchVersion, AttributeFactory factory, Reader input) {
    super(factory, input);
    init(matchVersion);
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
        if (tokenType == SpanishLemmatizerTokenizer.ACRONYM_DEP) {
          typeAtt.setType(SpanishLemmatizerTokenizer.TOKEN_TYPES[SpanishLemmatizerTokenizer.HOST]);
          termAtt.setLength(termAtt.length() - 1); // remove extra '.'
        } else {
          typeAtt.setType(SpanishLemmatizerTokenizer.TOKEN_TYPES[tokenType]);
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