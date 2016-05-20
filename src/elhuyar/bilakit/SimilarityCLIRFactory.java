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

import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.SmallFloat;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.schema.SimilarityFactory;


/** 
 * Similarity for CLIR
 */


public class SimilarityCLIRFactory extends SimilarityFactory {
  @Override
  public void init(SolrParams params) {
    super.init(params);
  }
  @Override
  public Similarity getSimilarity() {
    return new SimilarityCLIR();
  }
}

class SimilarityCLIR extends Similarity {
	 /** Cache of decoded bytes. */
	  private static final float[] NORM_TABLE = new float[256];

	  static {
	    for (int i = 0; i < 256; i++) {
	      NORM_TABLE[i] = SmallFloat.byte315ToFloat((byte)i);
	    }
	  }	  	  	
	  /**
	   * Sole constructor. (For invocation by subclass 
	   * constructors, typically implicit.)
	   */
	  public SimilarityCLIR() {}	  
	  /** Implemented as <code>overlap / maxOverlap</code>. */
	  @Override
	  public float coord(int overlap, int maxOverlap) {
	    return overlap / (float)maxOverlap;
	  }	
	  /** Implemented as <code>1/sqrt(sumOfSquaredWeights)</code>. */
	  @Override
	  public float queryNorm(float sumOfSquaredWeights) {
	    return (float)(1.0 / Math.sqrt(sumOfSquaredWeights));
	  }
	  	  	 
	  /**
	   * Computes a score factor for a simple term and returns an explanation
	   * for that score factor.
	   * 
	   * <p>
	   * The default implementation uses:
	   * 
	   * <pre class="prettyprint">
	   * idf(docFreq, searcher.maxDoc());
	   * </pre>
	   * 
	   * Note that {@link CollectionStatistics#maxDoc()} is used instead of
	   * {@link org.apache.lucene.index.IndexReader#numDocs() IndexReader#numDocs()} because also 
	   * {@link TermStatistics#docFreq()} is used, and when the latter 
	   * is inaccurate, so is {@link CollectionStatistics#maxDoc()}, and in the same direction.
	   * In addition, {@link CollectionStatistics#maxDoc()} is more efficient to compute
	   *   
	   * @param collectionStats collection-level statistics
	   * @param termStats term-level statistics for the term
	   * @return an Explain object that includes both an idf score factor 
	             and an explanation for the term.
	   */
	  public Explanation idfExplain(CollectionStatistics collectionStats, TermStatistics termStats) {
	    final long df = termStats.docFreq();
	    final long max = collectionStats.maxDoc();
	    final float idf = idf(df, max);
	    return new Explanation(idf, "idf(docFreq=" + df + ", maxDocs=" + max + ")");
	  }

	  /**
	   * Computes a score factor for a phrase.
	   * 
	   * <p>
	   * The default implementation sums the idf factor for
	   * each term in the phrase.
	   * 
	   * @param collectionStats collection-level statistics
	   * @param termStats term-level statistics for the terms in the phrase
	   * @return an Explain object that includes both an idf 
	   *         score factor for the phrase and an explanation 
	   *         for each term.
	   */
	  public Explanation idfExplain(CollectionStatistics collectionStats, TermStatistics termStats[]) {
	    final long max = collectionStats.maxDoc();
	    float idf = 0.0f;
	    final Explanation exp = new Explanation();
	    exp.setDescription("idf(), sum of:");
	    for (final TermStatistics stat : termStats ) {
	      final long df = stat.docFreq();
	      final float termIdf = idf(df, max);
	      exp.addDetail(new Explanation(termIdf, "idf(docFreq=" + df + ", maxDocs=" + max + ")"));
	      idf += termIdf;
	    }
	    exp.setValue(idf);
	    return exp;
	  }

	  /** Implemented as <code>log(numDocs/(docFreq+1)) + 1</code>. */
	  public float idf(long docFreq, long numDocs) {
	    return (float)(Math.log(numDocs/(double)(docFreq+1)) + 1.0);
	  }
	  
	  
	  /** 
	   * True if overlap tokens (tokens with a position of increment of zero) are
	   * discounted from the document's length.
	   */
	  protected boolean discountOverlaps = true;
	  
	  
	  
	  /** Determines whether overlap tokens (Tokens with
	   *  0 position increment) are ignored when computing
	   *  norm.  By default this is true, meaning overlap
	   *  tokens do not count when computing norms.
	   *
	   *  @lucene.experimental
	   *
	   *  @see #computeNorm
	   */
	  public void setDiscountOverlaps(boolean v) {
	    discountOverlaps = v;
	  }
	  
	  /**
	   * Returns true if overlap tokens are discounted from the document's length. 
	   * @see #setDiscountOverlaps 
	   */
	  public boolean getDiscountOverlaps() {
	    return discountOverlaps;
	  }

	  @Override
	  public String toString() {
	    return "SimilarityCLIR";
	  }
	  
	  
	  /** Implemented as
	   *  <code>state.getBoost()*lengthNorm(numTerms)</code>, where
	   *  <code>numTerms</code> is {@link FieldInvertState#getLength()} if {@link
	   *  #setDiscountOverlaps} is false, else it's {@link
	   *  FieldInvertState#getLength()} - {@link
	   *  FieldInvertState#getNumOverlap()}.
	   *
	   *  @lucene.experimental */
	  public float lengthNorm(FieldInvertState state) {
	    final int numTerms;
	    if (discountOverlaps)
	      numTerms = state.getLength() - state.getNumOverlap();
	    else
	      numTerms = state.getLength();
	    return state.getBoost() * ((float) (1.0 / Math.sqrt(numTerms)));
	  }
	  
	  /** Implemented as <code>sqrt(freq)</code>. */
	  public float tf(float freq) {
	    return (float)Math.sqrt(freq);
	  }
	    
	  /** Implemented as <code>1 / (distance + 1)</code>. */
	  public float sloppyFreq(int distance) {
	    return 1.0f / (distance + 1);
	  }
	  
	  
	  
	  
	  
	  
	  @Override
	  public final long computeNorm(FieldInvertState state) {
	    float normValue = lengthNorm(state);
	    return encodeNormValue(normValue);
	  }
	  
	
	  
	  
	  /**
	   * Encodes a normalization factor for storage in an index.
	   * <p>
	   * The encoding uses a three-bit mantissa, a five-bit exponent, and the
	   * zero-exponent point at 15, thus representing values from around 7x10^9 to
	   * 2x10^-9 with about one significant decimal digit of accuracy. Zero is also
	   * represented. Negative numbers are rounded up to zero. Values too large to
	   * represent are rounded down to the largest representable value. Positive
	   * values too small to represent are rounded up to the smallest positive
	   * representable value.
	   * 
	   * @see org.apache.lucene.document.Field#setBoost(float)
	   * @see org.apache.lucene.util.SmallFloat
	   */
	  public final long encodeNormValue(float f) {
	    return SmallFloat.floatToByte315(f);
	  }

	  /**
	   * Decodes the norm value, assuming it is a single byte.
	   * 
	   * @see #encodeNormValue(float)
	   */
	  public final float decodeNormValue(long norm) {
	    return NORM_TABLE[(int) (norm & 0xFF)];  // & 0xFF maps negative bytes to positive above 127
	  }
	  
	  
	 

	  //Here's where we actually decode the payload and return it.
	  public float scorePayload(int doc, int start, int end, BytesRef payload) {
	    if (payload == null) return 1.0F;
	    return PayloadHelper.decodeFloat(payload.bytes, payload.offset);
	  }
	  

	  @Override
	  public final SimWeight computeWeight(float queryBoost, CollectionStatistics collectionStats, TermStatistics... termStats) {
	    final Explanation idf = termStats.length == 1
	    ? idfExplain(collectionStats, termStats[0])
	    : idfExplain(collectionStats, termStats);
	    return new IDFStats(collectionStats.field(), idf, queryBoost);
	  }

	  @Override
	  public final SimScorer simScorer(SimWeight stats, AtomicReaderContext context) throws IOException {		
		IDFStats idfstats = (IDFStats) stats;
		String fieldsource=idfstats.field;
				

		if (idfstats.field.startsWith("text_l"))
		{
			fieldsource="source_text";
		}
		else if (idfstats.field.startsWith("title_l"))
		{
			fieldsource="source_title";
		}				
		
	

		return new TFIDFSimScorer(idfstats, context.reader().getNormValues(fieldsource));
	  }
	  
	  private final class TFIDFSimScorer extends SimScorer {
	    private final IDFStats stats;
	    private final float weightValue;
	    private final NumericDocValues norms;
	    
	    TFIDFSimScorer(IDFStats stats, NumericDocValues norms) throws IOException {
	      this.stats = stats;
	      this.weightValue = stats.value;
	      this.norms = norms;
	    }
	    
	    @Override
	    public float score(int doc, float freq) {
	      final float raw = tf(freq) * weightValue; // compute tf(f)*weight
	      
	      return norms == null ? raw : raw * decodeNormValue(norms.get(doc));  // normalize for field
	    }
	    
	    @Override
	    public float computeSlopFactor(int distance) {
	      return sloppyFreq(distance);
	    }

	    @Override
	    public float computePayloadFactor(int doc, int start, int end, BytesRef payload) {
	      return scorePayload(doc, start, end, payload);
	    }

	    @Override
	    public Explanation explain(int doc, Explanation freq) {
	      return explainScore(doc, freq, stats, norms);
	    }
	  }
	  
	  /** Collection statistics for the TF-IDF model. The only statistic of interest
	   * to this model is idf. */
	  private static class IDFStats extends SimWeight {
	    private final String field;
	    /** The idf and its explanation */
	    private final Explanation idf;
	    private float queryNorm;
	    private float queryWeight;
	    private final float queryBoost;
	    private float value;
	    
	    public IDFStats(String field, Explanation idf, float queryBoost) {
	      // TODO: Validate?
	      this.field = field;
	      this.idf = idf;
	      this.queryBoost = queryBoost;
	      this.queryWeight = idf.getValue() * queryBoost; // compute query weight
	    }

	    @Override
	    public float getValueForNormalization() {
	      // TODO: (sorta LUCENE-1907) make non-static class and expose this squaring via a nice method to subclasses?
	      return queryWeight * queryWeight;  // sum of squared weights
	    }

	    @Override
	    public void normalize(float queryNorm, float topLevelBoost) {
	      this.queryNorm = queryNorm * topLevelBoost;
	      queryWeight *= this.queryNorm;              // normalize query weight
	      value = queryWeight * idf.getValue();         // idf for document
	    }
	  }  

	  private Explanation explainScore(int doc, Explanation freq, IDFStats stats, NumericDocValues norms) {
	    Explanation result = new Explanation();
	    result.setDescription("score(doc="+doc+",freq="+freq.getValue()+"), product of:");

	    // explain query weight
	    Explanation queryExpl = new Explanation();
	    queryExpl.setDescription("queryWeight, product of:");

	    Explanation boostExpl = new Explanation(stats.queryBoost, "boost");
	    if (stats.queryBoost != 1.0f)
	      queryExpl.addDetail(boostExpl);
	    queryExpl.addDetail(stats.idf);

	    Explanation queryNormExpl = new Explanation(stats.queryNorm,"queryNorm");
	    queryExpl.addDetail(queryNormExpl);

	    queryExpl.setValue(boostExpl.getValue() *
	                       stats.idf.getValue() *
	                       queryNormExpl.getValue());

	    result.addDetail(queryExpl);

	    // explain field weight
	    Explanation fieldExpl = new Explanation();
	    fieldExpl.setDescription("fieldWeight in "+doc+
	                             ", product of:");

	    Explanation tfExplanation = new Explanation();
	    tfExplanation.setValue(tf(freq.getValue()));
	    tfExplanation.setDescription("tf(freq="+freq.getValue()+"), with freq of:");
	    tfExplanation.addDetail(freq);
	    fieldExpl.addDetail(tfExplanation);
	    fieldExpl.addDetail(stats.idf);

	    Explanation fieldNormExpl = new Explanation();
	    float fieldNorm = norms != null ? decodeNormValue(norms.get(doc)) : 1.0f;
	    fieldNormExpl.setValue(fieldNorm);
	    fieldNormExpl.setDescription("fieldNorm(doc="+doc+")");
	    fieldExpl.addDetail(fieldNormExpl);
	    
	    fieldExpl.setValue(tfExplanation.getValue() *
	                       stats.idf.getValue() *
	                       fieldNormExpl.getValue());

	    result.addDetail(fieldExpl);
	    
	    // combine them
	    result.setValue(queryExpl.getValue() * fieldExpl.getValue());

	    if (queryExpl.getValue() == 1.0f)
	      return fieldExpl;

	    return result;
	  }
	}

