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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SpanishLemmatizer {

	class FreelingSocketClient {
		private static final String SERVER_READY_MSG = "FL-SERVER-READY";
		private static final String RESET_STATS_MSG = "RESET_STATS";
		private static final String ENCODING = "UTF8";
		private static final String FLUSH_BUFFER_MSG = "FLUSH_BUFFER";
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
				writeMessage(bufferSalida, RESET_STATS_MSG,ENCODING);

				StringBuffer sb=readMessage(bufferEntrada);
				if(sb.toString().trim().compareTo(SERVER_READY_MSG)!=0) 
					System.err.println("SERVERREADY!");
				
				writeMessage(bufferSalida, FLUSH_BUFFER_MSG,ENCODING);
				readMessage(bufferEntrada);
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
			writeMessage(bufferSalida, FLUSH_BUFFER_MSG,ENCODING);
			readMessage(bufferEntrada);
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

	public SpanishLemmatizer(){}

	public String getLemma(String text){
		if (((text.length() - text.replace("\"", "").length()) % 2) != 0){
			text = text.replace("\"","");
		}
		text = text.replace("("," ").replace(")", " ").trim();
        if (text.length() > 0){
        	//System.out.println("### Freeling IN =>[" + text + "]");
        	System.out.println("### Freeling text IN length =>[" + text.length() + "]");
    		String text_lemmatized="";
    		FreelingSocketClient server = null;
    		try {
    			server = new FreelingSocketClient("localhost", 50010);
    			text_lemmatized = server.processSegment(text);			
    			server.close();
    		}
    		catch(IOException e) {
    			try {
    				server.close();
    			} catch (IOException e1) {}
    		}
    		//System.out.println("### Freeling OUT =>[" + text_lemmatized + "]");
    		System.out.println("### Freeling text OUT length =>[" + text_lemmatized.length() + "]");
	        return text_lemmatized;
        }
        return "";
	}
}