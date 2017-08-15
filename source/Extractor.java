/*
*  Extractor class - once constructed with an XML doc, can be given names of elements in tree, and
*  can return both lists of nodes that are a leaf in a matching tree, or the text content of those
*  nodes (so lessen library dependencies in other classes). Sole class that traverses Document 
*  objects.
*/

import org.w3c.dom.*;
import org.w3c.dom.traversal.*;
import java.net.*;
import java.io.*;
import java.util.*;

class Extractor
{
   private Document doc;

   public Extractor(Document d)
   {
      doc = d;
   }

   // Checks whether node matches the tree provided through String arugments. Arguments are read in reverse order
   // For example, if the Strings were ["body", "title"] then checkNode would return true if the node is called 
   // "title" and is a child of a node called "body". There is no limit to number of arguments, and so specificity
   // of tree. 
   private boolean checkNode(Node n, String... args)
   {
      int current = args.length-1;
      if(n.getNodeName().equals(args[current]))
      {
         Node e = n;
         while(e.getNodeName().equals(args[current]))
         {
            if(current==0)
            {
               return true;
            }
            else
            {
               e = e.getParentNode();
               current--;
            }
         }
      }
      return false;
   }

   // Goes through each node in document and returns list of those that are in tree matching that in argument
   public List<Node> getNode(String... args)
   {
      List<Node> list = new ArrayList<Node>();
      if(args==null || args.length<=0) return list;
      DocumentTraversal traversal = (DocumentTraversal) doc;
      Node a = doc.getDocumentElement();
      NodeIterator iterator = traversal.createNodeIterator(a, NodeFilter.SHOW_ELEMENT, null, true);
      for(Node n = iterator.nextNode(); n != null; n = iterator.nextNode())
      {
         if(checkNode(n,args))
         {
            list.add(n);
         }
      }
      return list;
   }

   // As with getNode, except the text contents of the nodes are returned
   public List<String> getText(String... args)
   {
      List<String> list = new ArrayList<String>();
      if(args==null || args.length<=0) return list;
      DocumentTraversal traversal = (DocumentTraversal) doc;
      Node a = doc.getDocumentElement();
      NodeIterator iterator = traversal.createNodeIterator(a, NodeFilter.SHOW_ELEMENT, null, true);
      for(Node n = iterator.nextNode(); n != null; n = iterator.nextNode())
      {
         if(checkNode(n,args))
         {
            list.add(n.getTextContent());
         }
      }
      return list;
   }

   // For unit testing
   public static void main(String[] args)
   {
      boolean testing = false;
      assert(testing = true);
      if(testing)
      {
         Retriever r = new Retriever("http://www.ebi.ac.uk/europepmc/webservices/rest/PMC3257301/fullTextXML");
         Extractor e = new Extractor(r.getDoc());
         assert(e.getNode("journal-meta","journal-title-group","journal-title").get(0).getTextContent().equals("PLoS Pathogens"));
         System.out.println("Test 1 (Single Node result) passed");
         assert(e.getNode("surname").get(6).getTextContent().equals("Lorenz"));
         System.out.println("Test 2 (Multiple Node result) passed");
         assert(e.getText().size()==0);
         System.out.println("Test 3 (Empty Text arguments) passed");
         assert(e.getNode().size()==0);
         System.out.println("Test 4 (Empty Node arguments) passed");
         assert(e.getText("journal-meta","journal-title-group","journal-title").get(0).equals("PLoS Pathogens"));
         System.out.println("Test 5 (Single Text result) passed");
         assert(e.getText("surname").get(6).equals("Lorenz"));
         System.out.println("Test 6 (Multiple Node result) passed");
      }
      else System.out.println("Use 'java -ea Extractor' for testing");
   }
}
