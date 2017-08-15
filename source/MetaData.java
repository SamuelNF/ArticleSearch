/*
*  Retrives and stores metadata of article from XML Document object.
*  If tags used in XML files change, all needed changes are confined to this class.
*/

import org.w3c.dom.*;
import java.util.*;

class MetaData
{
   private String journalTitle;
   private String articleTitle;
   private String issn;
   private String articleID = null;
   private String iDtype = null;
   private String pubDay = null;
   private String pubMonth = null;
   private String pubYear = null;

   public MetaData(Document doc)
   {
      Extractor e = new Extractor(doc);
      setTitles(e);
      setID(e);
      setPubDate(e);
   }

   // Extracts and stores publication date from document object
   private void setPubDate(Extractor e)
   {
      List<Node> dateList = e.getNode("article-meta","pub-date");
      for(Node n : dateList)
      {
         Element m = (Element) n;
         if(m.getAttribute("pub-type").equals("epub"))
         {
            parseDate(m);
         }
         else if(!m.getAttribute("pub-type").equals("collection") && pubYear==null)
         {
            parseDate(m);
         }
      }
   }            

   // Reads child elements from pub-date element
   private void parseDate(Element m)
   {
      pubYear = m.getElementsByTagName​("year").item(0).getTextContent();
      if(m.getElementsByTagName​("month").getLength()>0)
      {
         pubMonth = m.getElementsByTagName​("month").item(0).getTextContent();
      }
      if(pubMonth!=null && pubMonth.length()==1) 
      {
         pubMonth = "0"+pubMonth;
      }
      if(m.getElementsByTagName​("day").getLength()>0)
      {
         pubDay = m.getElementsByTagName​("day").item(0).getTextContent();
      }
      if(pubDay!=null && pubDay.length()==1)
      {
         pubDay = "0"+pubDay;
      }
   }

   // Sets article ID and ID type
   private void setID(Extractor e)
   {
      List<Node> idList = e.getNode("article-meta","article-id");
      for(Node n : idList)
      {
         Element m = (Element) n;
         if(m.getAttribute("pub-id-type").equals("doi"))
         {
            iDtype = "DOI";
            articleID = n.getTextContent();
         }
         else if(iDtype==null && m.getAttribute("pub-id-type").equals("PMCID"))
         {
            iDtype = "PMCID";
            articleID = n.getTextContent();
         }
      }
   }

   // Sets article and journal title, and issn 
   private void setTitles(Extractor e)
   {
      journalTitle = e.getText("journal-meta", "journal-title-group", "journal-title").get(0);
      articleTitle = e.getText("article-meta", "title-group", "article-title").get(0);
      issn = e.getText("journal-meta", "issn").get(0);
   }

   // Returns date as integer (for the sole purpose of comparison - not accurate date)
   public int getDateAsInt()
   {
      int date = Integer.parseInt(pubYear)*10000;
      if(pubMonth!=null)
      {
         date = date + (Integer.parseInt(pubMonth)*100);
      }
      else return date;
      if(pubDay!=null)
      {
         date = date + Integer.parseInt(pubDay);
      }
      return date;
   }

   public String getJournalTitle()
   {
      return journalTitle;
   }
   
   public String getArticleTitle()
   {
      return articleTitle;
   }
   
   public String getIssn()
   {
      return issn;
   }

   public String getArticleID()
   {
      return articleID;
   }

   public String getIDType()
   {
      return iDtype;
   }

   public String getYear()
   {
      return pubYear;
   }

   public String getMonth()
   {
      if(pubMonth==null) return "";
      return pubMonth;
   }

   public String getDay()
   {
      if(pubDay==null) return "";
      return pubDay;
   }
}
