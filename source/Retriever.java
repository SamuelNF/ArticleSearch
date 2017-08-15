/*
*  Retriever class - makes connection to website, gets XML input, and parses
*  into a Document object. Used for both initial search query and meta-data
*  retrieval.
*/

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;
import java.net.*;
import java.io.*;
import java.util.*;

class Retriever
{
   private HttpURLConnection connection;

   public Retriever(String web)
   {
      try
      {
         connection = (HttpURLConnection) new URL(web).openConnection();
         connection.setRequestMethod("GET");
         connection.connect();   
      }      
      catch (IOException e)
      {
         System.out.println("Error - No response from "+web);
      }
   }

   // Creates document builder object for use in parsing XML
   private DocumentBuilder getBuilder()
   {
      DocumentBuilder builder = null;
      try
      {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setNamespaceAware(true);
         factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
         builder = factory.newDocumentBuilder();
      }
      catch (ParserConfigurationException e)
      {
         System.out.println("Fatal Error - Could not set up XML parser");
         System.exit(1);
      }
      finally
      {
         return builder;
      }
   }  

   // Scans xml document from URL into string, and then parses string as Document object.
   public Document getDoc()
   {
      DocumentBuilder builder = getBuilder();
      Document doc = null;
      try
      {
         Scanner scanner = new Scanner(connection.getInputStream());
         String xml = "";
         do
         {
            xml = xml + scanner.nextLine();
         }while (scanner.hasNextLine());
         scanner.close();
         doc = builder.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))));
         doc.normalizeDocument();
      }
      catch (SAXException e)
      {
         System.out.println("Fatal Error - Error while parsing");
         System.exit(1);
      }
      finally 
      {
         return doc;
      }
   }
      
   // For unit testing
   public static void main(String[] args)
   {
      boolean testing = false;
      assert(testing = true);
      if(testing)
      {
         Retriever r = new Retriever("http://www.ebi.ac.uk/europepmc/webservices/rest/PMC3257301/fullTextXML");
         Document d = r.getDoc();
         assert(d!=null);
         System.out.println("Test 1 (Not Null) Passed");
         assert(d.getDocumentElement().getAttribute("article-type").equals("research-article"));
         System.out.println("Test 2 (Article Type) Passed");
      }
      else System.out.println("Use 'java -ea Retriever' for testing");
   }
}
