/*
*  Top class of Jisc article meta-data retriever program.
*  Key variables (max number of results, default query, name of log file etc) are easy to change as statics at top of class .
*  User may provide extra string when program runs to override default search query.
*/

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

class ArticleSearch
{
   static private String searchAddress = "http://www.ebi.ac.uk/europepmc/webservices/rest/search?query=";
   static private String query = "singularity";
   static private int maxResultNum = 10;
   static private int maxTitleLength = 50;
   static private String XMLAddress = "http://www.ebi.ac.uk/europepmc/webservices/rest/DOC_ID/fullTextXML";
   static private String logName = "LogBot.txt";

   public static void main(String[] args)
   {
      ArticleSearch a = new ArticleSearch();
      if(args.length == 1)
      {
         query = args[0];
      }

      List<String> idList = a.getSearchResults();
      if(idList.size()>maxResultNum) 
      {
         idList = idList.subList(0,maxResultNum);
      }
      System.out.println(idList.size()+" Articles Found");

      List<MetaData> metaList = a.getMetaData(idList);

      a.printAllMetaData(metaList);
      a.printSummary(metaList);
      a.writeLog(metaList.size());
   }

   // Retrieves XML docs based on article id, and returns list of parsed metadata from these
   private List<MetaData> getMetaData(List<String> idList)
   {
      System.out.print("Parsing XML documents");
      List<MetaData> metaList = new ArrayList<MetaData>();
      for(String s : idList)
      {
         System.out.print(".");
         Retriever r = new Retriever(XMLAddress.replace("DOC_ID", s));
         Document doc = r.getDoc();
         if(doc!=null)
         {
            metaList.add(new MetaData(doc));
         }
      }
      System.out.print("\n");
      return metaList;
   }

   // Prints to terminal all MetaData objects in list
   private void printAllMetaData(List<MetaData> list)
   {
      int count = 1;
      for(MetaData m : list)
      {
         System.out.println("\nARTICLE "+count++);
         printMetaData(m);
      }
   }

   // Prints summary sections
   private void printSummary(List<MetaData> list)
   {
      int count = 1;

      System.out.println("\nSUMMARY - Section 1");
      System.out.println("Total number of XML docs processed: "+list.size());
      System.out.println("Earliest publication date:          "+pubDate(getEarliest(list)));
      System.out.println("Latest publication date:            "+pubDate(getLatest(list)));
      
      System.out.println("\nSUMMARY - Section 2");
      for(MetaData m : list)
      {
         String title = m.getArticleTitle();
         if(title.length() > maxTitleLength)
         {
            title = title.substring(0,maxTitleLength-1);
         }
         title = title + "...";
         System.out.println(count++ +". "+title);
      }
   } 

   // Returns article with earliest pubication date from list
   private MetaData getEarliest(List<MetaData> list)
   {
      int lowest = 30000000; //This is equivalent to year 3000
      MetaData earliest = null;
      for(MetaData m : list)
      {
         if(m.getDateAsInt()<lowest)
         {
            lowest = m.getDateAsInt();
            earliest = m;
         }
      }
      return earliest;
   }

   // Returns article with latest pubication date from list
   private MetaData getLatest(List<MetaData> list)
   {
      int highest = 0;
      MetaData latest = null;
      for(MetaData m : list)
      {
         if(m.getDateAsInt()>highest)
         {
            highest = m.getDateAsInt();
            latest = m;
         }
      }
      return latest;
   }

   // Prints single article's metadata to terminal
   private void printMetaData(MetaData m)
   {
      System.out.println("Article Title: "+m.getArticleTitle());
      System.out.println("Journal Title: "+m.getJournalTitle());
      System.out.println("ISSN:          "+m.getIssn());
      System.out.println(m.getIDType()+":           "+m.getArticleID());
      System.out.println("Pub Date:      "+pubDate(m));
   }

   // Returns publication article as consistent string from metadata
   private String pubDate(MetaData m)
   {
      String date = "";
      String help = "";
      if(m.getYear()!=null)
      {
         date = m.getYear();
         help = "YYYY";
      }
      if(m.getMonth()!=null)
      {
         date = date + "-" + m.getMonth();
         help += "-MM";
      }
      if(m.getDay()!=null) 
      {
         date = date + "-" + m.getDay();
         help += "-DD";
      }
      if(date.equals("")) return "N/A";
      else return (date + " (" + help + ")");
   }

   // Writes activity to log file
   private void writeLog(int count)
   {
      DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
      String timestamp = dateFormat.format(Calendar.getInstance().getTime());

      try
      {
      Writer output = new BufferedWriter(new FileWriter(logName, true));
      String log = timestamp + " - " + count + " XML docs processed\n";
      output.append(log);
      output.close();
      }
      catch (IOException e)
      {
         System.out.println("\nError - Unsuccessful write to log");
      }    
   }

   // Returns list of ID numbers for articles found via search query
   private List<String> getSearchResults()
   {
      Retriever r = new Retriever(searchAddress + query);
      Document resultDoc = r.getDoc();
      if(resultDoc==null)
      {
         System.out.println("Fatal error - unable to connect to search API");
         System.exit(1);
      }
      Extractor e = new Extractor(resultDoc);
      return (e.getText("resultList","result","pmcid"));
   }
}
