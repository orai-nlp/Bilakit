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

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import com.google.gson.Gson;
 
public class IxaPipesLemmatizer {
	private IPText processedText;

	public IxaPipesLemmatizer(){}

	public void setText(String text,String lang){
        try{
        	int port;
        	if (lang.equalsIgnoreCase("eu")){
        		port = 50030;
        	}
        	else if (lang.equalsIgnoreCase("en")){
        		port = 50032;
        	}
        	else{ //es
        		port = 50031;
        	}
	        //get the localhost IP address, if server is running on some other IP, you need to use that
	        InetAddress host = InetAddress.getLocalHost();
	        Socket socket = null;
	        ObjectOutputStream oos = null;
	        ObjectInputStream ois = null;
	        //establish socket connection to server
	        socket = new Socket(host.getHostName(), port);
	        //write to socket using ObjectOutputStream
	        oos = new ObjectOutputStream(socket.getOutputStream());
	        System.out.println("Sending request to Socket Server");
	        oos.writeObject(text);
	        //read the server response message
	        ois = new ObjectInputStream(socket.getInputStream());
	        String iptextJSON = (String) ois.readObject();
	        Gson gson = new Gson();
	        IPText iptext = gson.fromJson(iptextJSON, IPText.class);
	        this.processedText = iptext;
	        //close resources
	        oos.close();
	        socket.close();
        }
        catch(Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        }
	}
	
	public String getLemmatizedText(String text,String lang){
		setText(text,lang);
		if (this.processedText != null){
			return this.processedText.getLemmatizedText();
		}
		return "";
	}
	
	public Set<String> getNER(String termType){
		if (this.processedText != null){
			return this.processedText.getList(termType);
		}
		return new HashSet<String>();
	}
}

