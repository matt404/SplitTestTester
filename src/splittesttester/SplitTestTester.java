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

			System.out.println("==========================");
			
			boolean haveFirstPass = false;

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					
					if(haveFirstPass){
						System.out.println("--------------------------");
					}
					Element testElement = (Element) nNode;
					System.out.println("Running Test # " + (temp+1));
					RunTest(testElement);
					haveFirstPass = true;

				}
			}
			
			System.out.println("==========================");
			System.out.println("Test Complete...");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean RunTest(Element testElement){
		boolean blnSiteIsUp = false;
		
		int qtyToRun = Integer.parseInt(testElement.getAttribute("qtyToRun"));
		System.out.println("Iterating " + qtyToRun + " times");
		
		NodeList nodeListStage = testElement.getElementsByTagName("stage");
		NodeList nodeListTarget = testElement.getElementsByTagName("target");
		
		CookieManager cm = new CookieManager();
		int matchCount = 0;

		for(int i=0; i < qtyToRun; i++){
		System.out.print("Executing Iteration # " + (i+1) + "... ");

			if(nodeListStage.getLength() > 0){
				NodeList stagePaths = nodeListStage.item(0).getChildNodes();
				for (int temp = 0; temp < stagePaths.getLength(); temp++) {
					Node itemNode = stagePaths.item(temp);
					if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
						String stagePath = itemNode.getTextContent();
//						System.out.println(stagePath);

						try{
							URL url = new URL(stagePath);
							URLConnection urlConn = url.openConnection();
							urlConn.setReadTimeout(5000);
							urlConn.setConnectTimeout(10000);
							cm.storeCookies(urlConn);
							urlConn.connect();
							cm.setCookies(url.openConnection());
//
//							BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
//							String inputLine;
//							while ((inputLine = in.readLine()) != null) {
//								//System.out.println(inputLine);
//							}
//							in.close();
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
				String targetPath = "";
				String targetMatchString = "";
				for (int temp = 0; temp < targetItems.getLength(); temp++) {
					Node itemNode = targetItems.item(temp);
					if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
						if("path".equals(itemNode.getNodeName())){
							targetPath = itemNode.getTextContent();
						}
						if("textToMatch".equals(itemNode.getNodeName())){
							targetMatchString = itemNode.getTextContent();
						}
//						System.out.println(targetPath);
//						System.out.println(targetMatchString);
					}
				}

				try{
					URL url = new URL(targetPath);
					URLConnection urlConn = url.openConnection();
					urlConn.setReadTimeout(5000);
					urlConn.setConnectTimeout(10000);
					cm.setCookies(urlConn);
					urlConn.connect();

					BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
					String inputLine;
					while ((inputLine = in.readLine()) != null) {
						if(inputLine.contains(targetMatchString)){
							matchCount++;
							break;
						}
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
			System.out.println("Done");
		}
		
		int hitPercent = 0;
		if(matchCount > 0){
			hitPercent = Math.round((matchCount/qtyToRun)*100);
		}

		System.out.println("Hit Percentage: " + hitPercent + "%");
				
		return blnSiteIsUp;
	}
}
