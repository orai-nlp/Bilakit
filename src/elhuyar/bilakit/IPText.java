
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

import java.util.HashSet;
import java.util.Set;

public class IPText {
	private String lemmatizedText = "";
    private Set<String> person_list = new HashSet<String>();
    private Set<String> location_list = new HashSet<String>();
    private Set<String> organization_list = new HashSet<String>();
    
    public void setLemmatizedText(String ltext){
    	lemmatizedText = ltext;
    }
    
    public String getLemmatizedText(){
    	return lemmatizedText;
    }
    
    public void setList(Set<String> list,String type){
    	if (type.equals("LOC")){
    		this.location_list = list;
    	}
    	else if (type.equals("ORG")){
    		this.organization_list = list;
    	}
    	else{//PER
    		this.person_list = list;
    	}
    }

    public Set<String> getList(String type){
    	Set<String> list;
    	if (type.equals("LOC")){
    		list = this.location_list;
    	}
    	else if (type.equals("ORG")){
    		list = this.organization_list;
    	}
    	else{//PER
    		list = this.person_list;
    	}
    	return list;
    }
    
    public static void main(String[] args) {}
}