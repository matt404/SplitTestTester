/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splittesttester;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author ubu
 */
public class SplitTestTester {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {

		try {

			File fXmlFile = new File("tests.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

			NodeList nList = doc.getElementsByTagName("test");

			System.out.println("----------------------------");

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element testElement = (Element) nNode;
					
					RunTest(testElement);

				}
			}
			
			System.out.println("----------------------------");
			System.out.println("Test Complete...");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean RunTest(Element testElement){
		boolean blnSiteIsUp = false;
		
		int qtyToRun = Integer.parseInt(testElement.getAttribute("qtyToRun"));
		
		NodeList nodeListStage = testElement.getElementsByTagName("stage");
		NodeList nodeListTarget = testElement.getElementsByTagName("target");
		
		for(int i=0; i < qtyToRun; i++){

			CookieManager cm = new CookieManager();
			if(nodeListStage.getLength() > 0){
				NodeList stagePaths = nodeListStage.item(0).getChildNodes();
				for (int temp = 0; temp < stagePaths.getLength(); temp++) {
					Node itemNode = stagePaths.item(temp);
					if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
						String stagePath = itemNode.getTextContent();
						System.out.println(stagePath);

						try{
							URL url = new URL(stagePath);
							URLConnection urlConn = url.openConnection();
							urlConn.setReadTimeout(5000);
							urlConn.setConnectTimeout(10000);
							urlConn.connect();

							String headerName=null;
							int cookieCounter = 0;
							for (int headerIndex=1; (headerName = urlConn.getHeaderFieldKey(headerIndex))!=null; headerIndex++) {
								if (headerName.equals("Set-Cookie")) {
									cookies[cookieCounter] = urlConn.getHeaderField(headerIndex);
									cookieCounter++;
									System.out.println(urlConn.getHeaderField(headerIndex));
								}
							}

							BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
							String inputLine;
							while ((inputLine = in.readLine()) != null) {
								//System.out.println(inputLine);
							}
							in.close();
						}
						catch (MalformedURLException e) { 
							e.printStackTrace();
						} 
						catch (IOException e) {   
							e.printStackTrace();
						}
					}
				}
			}

			if(nodeListTarget.getLength() > 0){
				NodeList targetItems = nodeListTarget.item(0).getChildNodes();
				for (int temp = 0; temp < targetItems.getLength(); temp++) {
					Node itemNode = targetItems.item(temp);
					if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
						String targetPath = "";
						String targetMatchString = "";
						if("path".equals(itemNode.getNodeName())){
							targetPath = itemNode.getTextContent();
						}
						if("textToMatch".equals(itemNode.getNodeName())){
							targetMatchString = itemNode.getTextContent();
						}
						System.out.println(targetPath);
						System.out.println(targetMatchString);
					}
				}
			}			
		}

		return blnSiteIsUp;
	}
}
