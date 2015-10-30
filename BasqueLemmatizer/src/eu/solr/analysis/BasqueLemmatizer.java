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

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.Normalizer;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileWriter;

public class BasqueLemmatizer {

	class FreelingSocketClient {
		private static final String ENCODING = "UTF8";
		private final static int BUF_SIZE = 2048;

		Socket socket;
		DataInputStream bufferEntrada;
		DataOutputStream bufferSalida;

		FreelingSocketClient(String host,int port) {
			try
			{
				socket = new Socket (host, port);
				socket.setSoLinger (true, 10);
				socket.setKeepAlive(true);
				socket.setSoTimeout(0);
				bufferEntrada = new DataInputStream (socket.getInputStream());
				bufferSalida = new DataOutputStream (socket.getOutputStream());
			}
			catch (Exception e){
				e.printStackTrace();
				try {
					socket.close();
				} catch (IOException e1) {}
			}
		}

		public String processSegment(String text) throws IOException {
			writeMessage(bufferSalida, text,ENCODING);
			StringBuffer sb = readMessage(bufferEntrada);
			return sb.toString().trim();
		}

		void writeMessage(java.io.DataOutputStream out, String message, String encoding)
				throws IOException
		{
			out.write(message.getBytes(encoding));
			out.write(0);
			out.flush();
		}

		void close() throws IOException {
			socket.close();
		}

		private synchronized StringBuffer readMessage(DataInputStream bufferEntrada)
				throws IOException {

			byte[] buffer = new byte[BUF_SIZE];
			int bl =  0;
			StringBuffer sb = new StringBuffer();

			//messages ends with 
			do {
				bl =  bufferEntrada.read(buffer, 0, BUF_SIZE);
				if(bl>0) sb.append(new String (buffer,0,bl));
			}while (bl>0 && buffer[bl-1]!=0);
			return sb;
		}

	}

	public BasqueLemmatizer(){}

	public String getLemma(String text){
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = text.replaceAll("\n", " ").trim(); 
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9 .,;:\\-_()\"\'¿?¡!]+");
        text = pattern.matcher(text).replaceAll("");
        if ((text.length() > 0) && (text.substring(text.length()-1).equals("-"))){
        	if (text.length() > 1){
        		text = text.substring(0, text.length()-2);
        	}
        	else{
        		text = "";
        	}
        }
        if (text.length() > 0){
        	//System.out.println("### Eustagger IN =>[" + text + "]");
        	System.out.println("### Eustagger text IN length =>[" + text.length() + "]");
			String text_lemmatized="";
			FreelingSocketClient server = null;
			try {
				File tempFile = File.createTempFile("BasqueLemmatizer",null);
				BufferedWriter out = new BufferedWriter(new FileWriter(tempFile));
				out.write(text);
				out.close();
				server = new FreelingSocketClient("localhost", 50005);
				text_lemmatized = server.processSegment(tempFile.getAbsolutePath());			
				tempFile.delete();
				server.close();
			}
			catch(IOException e) {
				try {
					server.close();
				} catch (IOException e1) {}
			}
    		//System.out.println("### Eustagger OUT =>[" + text_lemmatized + "]");
    		System.out.println("### Eustagger text OUT length =>[" + text_lemmatized.length() + "]");
			return text_lemmatized;
        }
        return "";
	}
}