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


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * This class implements methods related to bilingual and MWU dictionaries 
 */

public final class Dicfile {

	
	//load a bilingual dictionary
	public static Map<String, String> dicFileLoad(String filename)
	{	    	
		Map<String, String> dic = new HashMap<String, String>();
		BufferedReader breader;
		try {
			breader = new BufferedReader(new FileReader(filename));			
			String line;
			while ((line = breader.readLine()) != null)
			{
				if (line.startsWith("#") || line.matches("^\\s*$"))
				{
					continue;
				}
				String[] fields = line.split("\t");
				try{
					dic.put(fields[0], fields[1]);
				}catch (IndexOutOfBoundsException ioobe){
					System.err.println("FileUtilsElh::loadTwoColumnResource - "+line);
				}
			}
			breader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Error MWUFileLoad");
			e.printStackTrace();
		}										
		return dic;	    	    
	
	}
	
	
	//load a stopword list
	public static List<String> stopwordLoad(String filename)
	{		
		List<String> stopwords = new ArrayList<String>();				
		BufferedReader breader;
		try {
			breader = new BufferedReader(new FileReader(filename));			
			String line;
			while ((line = breader.readLine()) != null)
			{	
				if (line.startsWith("#"))
				{
					continue;
				}
				
				try{
					stopwords.add(line);
				}catch (IndexOutOfBoundsException ioobe){
					System.err.println("Dicfile::stopwordLoad - "+line);
				}
			}
			breader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Error stopwordsfile");
			e.printStackTrace();
		}										
		return stopwords;	    	 
	}
	
	//load MWU list
	public static Map<String, String> MWUFileLoad(String filename)
	{	    	
		Map<String, String> MWU = new LinkedHashMap<String, String>();
		BufferedReader breader;
		try {
			breader = new BufferedReader(new FileReader(filename));			
			String line;
			while ((line = breader.readLine()) != null)
			{
				if (line.startsWith("#") || line.matches("^\\s*$"))
				{
					continue;
				}
				String[] fields = line.split("\t");
				try{
					MWU.put(fields[0], fields[1]);
				}catch (IndexOutOfBoundsException ioobe){
					System.err.println("Dicfile::loadTwoColumnResource - "+line);
				}
			}
			breader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Error MWUFileLoad");
			e.printStackTrace();
		}										
		return MWU;	    	    	  
	}
	  
	
	
	
	
	//remove stopwords from a string
	public static String remove_stopwords(String lemak,List<String> stopwords)
	{	  
		String 	field_value="";
		String[] temp;
		//split lemak string
		temp=lemak.split(" ");
		for(int i =0; i < temp.length ; i++)
		{			
			if (stopwords.contains(temp[i]))
			{
				field_value=field_value+" ";			
			}
			else
			{
				field_value=field_value+" "+temp[i];
			}         
		}		
		return field_value;	    	    	  
	}
	
	//tag MWU in a string
	public static String MWUtagOld(String lemak,Map<String, String> MWU)
	{	    	
		String 	field_value_MWU="";
		//Identify MWUs				
		field_value_MWU=lemak.toLowerCase();
		field_value_MWU = field_value_MWU.replaceAll("-", " ");						
		for (String k: MWU.keySet())
		{
			//there are 1-n standarizations, where word boundaries are marked by '_' chars.
			// for the moment convert them to separate words - 2015/07/24
			String kk = k.replaceAll("\\?", "\\\\\\\\?");
			field_value_MWU=field_value_MWU.replaceAll("\\b"+kk+"\\b",MWU.get(k));
		}		
		return field_value_MWU;	    	    	  
	}
	
	//tag MWU in a string (fast)
	public static String MWUtagOpt(String lemak,Map<String, String> MWU)
	{	    	
		String 	field_value="";
		String field_result="";
		
		String 	token1="";
		String 	token2="";
		String 	token3="";
		String 	token4="";
		
		int winsize =0;		
		//Identify MWUs				
		field_value=lemak.toLowerCase();
		field_value = field_value.replaceAll("-", " ");
		String[] temp;
		//Slit the lemmatized text
		temp=field_value.split(" ");
		for(int i =0; i < temp.length ; i++)
		{
			
			if (winsize==0)
			{
				token1=temp[i];
				winsize++;
			}	
			else if (winsize==1) 
			{
				token2=temp[i];
				winsize++;
			}			
			else if (winsize==2)
			{
				token3=temp[i];
				winsize++;
			}
			else if (winsize==3)
			{
				token4=temp[i];
				winsize++;
			}					
			//window full?
			if (winsize==4)
			{
					if (MWU.containsKey(token1+" "+token2+" "+token3+" "+token4))
					{
						field_result=field_result+" "+MWU.get(token1+" "+token2+" "+token3+" "+token4);						
						winsize=0;
						token1="";
						token2="";
						token3="";
						token4="";
					}					
					else if (MWU.containsKey(token1+" "+token2+" "+token3))
					{
						field_result=field_result+" "+MWU.get(token1+" "+token2+" "+token3);						
						winsize=1;
						token1=token2;
						token2=token3;
						token3=token4;
						token4="";
					}										
					else if (MWU.containsKey(token1+" "+token2))
					{
						field_result=field_result+" "+MWU.get(token1+" "+token2);						
						winsize=2;
						token1=token3;
						token2=token4;
						token3="";
						token4="";
					}
						
					else
					{
						field_result=field_result+" "+token1;						
						winsize=3;
						token1=token2;
						token2=token3;
						token3=token4;
						token4="";
					}						
			}									
		}					
		if (winsize > 0)
		{					
			if (MWU.containsKey(token1+" "+token2+" "+token3))
			{
				field_result=field_result+" "+MWU.get(token1+" "+token2+" "+token3);						
			}					
			else if (MWU.containsKey(token1+" "+token2))
			{
				field_result=field_result+" "+MWU.get(token1+" "+token2)+" "+token3;									
			}
			else if (MWU.containsKey(token2+" "+token3))
			{
				field_result=field_result+" "+token1+" "+MWU.get(token2+" "+token3);									
			}
			else
			{
				field_result=field_result+" "+token1+" "+token2+" "+token3;									
			}
		}							
		return field_result;	    	    	  
	}
	
	
	//translate words of a text by using a bilingual dict
	public static String itzul(String mwu,Map<String, String> dic)
	{	    	
		String 	field_value_itzul="";
		String[] temp;
		//split mwu string
		temp=mwu.split(" ");
		for(int i =0; i < temp.length ; i++)
		{
			//remove | characters. Problematic with payloads
			if (! temp[i].equals("|"))
			{										
				if (dic.get(temp[i]) != null)
				{
					field_value_itzul=field_value_itzul+" "+dic.get(temp[i]);
				}
				else
				{
					field_value_itzul=field_value_itzul+" "+temp[i];
				}
			}	
		}	
		return field_value_itzul;	    	    	  
	}
	
	
}
