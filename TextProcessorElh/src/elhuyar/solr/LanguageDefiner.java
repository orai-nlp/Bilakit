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

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

public class LanguageDefiner {
	public LanguageDefiner(){}
	
	public String getLanguage(String text){
		String language = "es"; //default language
        DetectorFactory.clear();
		String profiles = System.getProperty("user.dir") + "/solr/lib/lang_profiles";
        try {
			DetectorFactory.loadProfile(profiles);
	        Detector detector = DetectorFactory.create();
			detector.append(text);
			language = detector.detect();
			System.out.println("Language detector: [" + language + "] => " + text.substring(0,30) + "...");
		} catch (LangDetectException e) {
			e.printStackTrace();
		}
        return language;
	}
}