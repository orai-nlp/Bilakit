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

package lemmatizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import eus.ixa.ixa.pipe.nerc.train.Flags;
import lemmatizer.EnglishLemmatizer;
import lemmatizer.SpanishLemmatizer;
import lemmatizer.BasqueLemmatizer;

public class TermLemmatizer {
	public static void main(String[] args) throws Exception {
		String nerDir = "ixa-pipes/";
		String lexer = Flags.DEFAULT_LEXER;
		String dictTag = Flags.DEFAULT_DICT_OPTION;
		String dictPath = Flags.DEFAULT_DICT_PATH;
		String clearFeatures = Flags.DEFAULT_FEATURE_FLAG;
		
		String lang = args[0];
		int use_freeling = Integer.parseInt(args[1]);

		BufferedReader br;
		BufferedWriter bw;
		String thisLine = null;
		String lemmatized = "";
		
		for (int i = 2; i < args.length; i++) {
			String filename = args[i];
			br = new BufferedReader(new FileReader(filename));
			bw = new BufferedWriter(new FileWriter("lem_" + filename));
			if (lang.equals("eu")){
				BasqueLemmatizer basqueLemmatizer =  new BasqueLemmatizer();
				while ((thisLine = br.readLine()) != null) {
					lemmatized = "";
					lemmatized = basqueLemmatizer.getLemma(thisLine);
					bw.write(thisLine.trim() + " => " + lemmatized.trim() + "\n");
			        bw.flush();
				}
			}
			else if (lang.equals("en")){
				if (use_freeling != 0){
					EnglishLemmatizer englishLemmatizer =  new EnglishLemmatizer();
					while ((thisLine = br.readLine()) != null) {
						lemmatized = "";
						lemmatized = englishLemmatizer.getLemma(thisLine);
						bw.write(thisLine.trim() + " => " + lemmatized.trim() + "\n");
				        bw.flush();
					}
				}
				else{
					String posModelPath = nerDir+"pos-models-1.4.0/en/en-maxent-100-c5-baseline-dict-penn.bin";
					String nercModelPath = nerDir+"nerc-models-1.5.0/en/conll03/en-light-clusters-conll03.bin";
					eus.ixa.ixa.pipe.pos.Annotate postagger = TextProcess.getPostagger(posModelPath,lang);
					eus.ixa.ixa.pipe.nerc.Annotate nerctagger = TextProcess.getNercTagger(nercModelPath,lang,lexer,dictTag,dictPath,clearFeatures);
					while ((thisLine = br.readLine()) != null) {
						lemmatized = "";
						TextProcess processed_text = new TextProcess(thisLine,lang,posModelPath,postagger,nercModelPath,nerctagger);
						lemmatized = processed_text.getLemmatizedText();
						bw.write(thisLine.trim() + " => " + lemmatized.trim() + "\n");
				        bw.flush();
					}
				}
			}
			else if (lang.equals("es")){
				if (use_freeling != 0){
					SpanishLemmatizer spanishLemmatizer =  new SpanishLemmatizer();
					while ((thisLine = br.readLine()) != null) {
						lemmatized = "";
						lemmatized = spanishLemmatizer.getLemma(thisLine);
						bw.write(thisLine.trim() + " => " + lemmatized.trim() + "\n");
				        bw.flush();
					}
				}
				else{
					String posModelPath = nerDir+"pos-models-1.4.0/es/es-maxent-100-c5-baseline-autodict01-ancora.bin";
					String nercModelPath = nerDir+"nerc-models-1.5.0/es/es-clusters-conll02.bin";
					eus.ixa.ixa.pipe.pos.Annotate postagger = TextProcess.getPostagger(posModelPath,lang);
					eus.ixa.ixa.pipe.nerc.Annotate nerctagger = TextProcess.getNercTagger(nercModelPath,lang,lexer,dictTag,dictPath,clearFeatures);
					while ((thisLine = br.readLine()) != null) {
						lemmatized = "";
						TextProcess processed_text = new TextProcess(thisLine,lang,posModelPath,postagger,nercModelPath,nerctagger);
						lemmatized = processed_text.getLemmatizedText();
						bw.write(thisLine.trim() + " => " + lemmatized.trim() + "\n");
				        bw.flush();
					}
				}
			}
			br.close();
			bw.close();
		}
	}
}