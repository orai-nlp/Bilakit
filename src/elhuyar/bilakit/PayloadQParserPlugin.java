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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.payloads.AveragePayloadFunction;
import org.apache.lucene.search.payloads.PayloadTermQuery;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.parser.QueryParser;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SyntaxError;

/**
 * Query parser for CLIR (averaging payloads) 
 */


// Just the factory class that doesn't do very much in this 
// case but is necessary for registration in solrconfig.xml.
public class PayloadQParserPlugin extends QParserPlugin {

  @Override
  public void init(NamedList args) {
    // Might want to do something here if you want to preserve information for subsequent calls!
  }

  @Override
  public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    return new PayloadQParser(qstr, localParams, params, req);
  }
}


// The actual parser. Note that it relies heavily on the superclass
class PayloadQParser extends QParser {
  PayloadQueryParser pqParser;

  public PayloadQParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    super(qstr, localParams, params, req);
  }

  // This is kind of tricky. The deal here is that you do NOT 
  // want to get into all the process of parsing parentheses,
  // operators like AND/OR/NOT/+/- etc, it's difficult. So we'll 
  // let the default parsing do all this for us.
  // Eventually the complex logic will resolve to asking for 
  // fielded query, which we define in the PayloadQueryParser
  // below.
  @Override
  public Query parse() throws SyntaxError {
    String qstr = getString();        
    if (qstr == null || qstr.length() == 0) return null;
    String defaultField = getParam(CommonParams.DF);
    if (defaultField == null) {
      defaultField = getReq().getSchema().getDefaultSearchFieldName();
    }
    pqParser = new PayloadQueryParser(this, defaultField);
    pqParser.setDefaultOperator
        (QueryParsing.getQueryParserDefaultOperator(getReq().getSchema(),
            getParam(QueryParsing.OP)));
    
    
    return pqParser.parse(qstr);
  }

  @Override
  public String[] getDefaultHighlightFields() {
    return pqParser == null ? new String[]{} :
                              new String[] {pqParser.getDefaultField()};
  }

}


// Here's the tricky bit. You let the methods defined in the 
// superclass do the heavy lifting, parsing all the
// parentheses/AND/OR/NOT/+/- whatever. Then, eventually, when 
// all that's resolved down to a field and a term, and
// BOOM, you're here at the simple "getFieldQuery" call.
// NOTE: this is not suitable for phrase queries, the limitation 
// here is that we're only evaluating payloads for
// queries that can resolve to combinations of single word 
// fielded queries.
class PayloadQueryParser extends QueryParser {
  PayloadQueryParser(QParser parser, String defaultField) {
    super(parser.getReq().getCore().getSolrConfig().luceneMatchVersion, defaultField, parser);
  }

  @Override
  protected Query getFieldQuery(String field, String queryText, boolean quoted) throws SyntaxError {
    SchemaField sf = this.schema.getFieldOrNull(field);	
	if (!quoted && sf != null && sf.getType().getTypeName().endsWith("_payloads"))
	{	
		//analyze queryText
		List<String> result = new ArrayList<String>();
		try 
		{    			
			TokenStream stream  = getAnalyzer().tokenStream(field, new StringReader(queryText));
			stream.reset();
			while (stream.incrementToken())
			{
				result.add(stream.getAttribute(CharTermAttribute.class).toString());
			}
			stream.end();
			stream.close();
		}
		catch (IOException e) 
		{
			// 	not thrown b/c we're using a string reader...
			throw new RuntimeException(e);
		}   	
		String analyzedqueryText="";
		analyzedqueryText=result.toString().replaceAll("\\[|\\]", "").replaceAll(", "," ");	
		queryText=analyzedqueryText;	
		// Note that this will work for any field defined with the
		// 	<fieldType> of "*_payloads"
		Query plter=new PayloadTermQuery(new Term(field, queryText), new AveragePayloadFunction(), true);

		return  plter;
		
	}
    return super.getFieldQuery(field, queryText, quoted);
  }
}
