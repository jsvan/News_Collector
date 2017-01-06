import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by julianvanecek on 1/2/17.
 *
 *
 *
 * Nice. some observations: when continueRequest becomes false, just brake out of the for loop instead of processing all remaining elements
 * and do nothing with them. Basically, take continueRequest out and replace the continueRequest=false lines with 'break'. Also, flush the
 * StringBuffer to the JScrollPane after ever iteration.The content of the scroll pane can be appended to. so use it just as the the string
 * buffer. Next, did you want all the articles on the NY times page or recursivelly drill into all stories from a single article? Finally,
 * I may want to use this idea to pump articles into Perce, once I can have it process english properly. :)
 *
 *
 *
 *
 *
 */

public class Driver {
    static Document article;
    static Elements articleContent;

    public static void main(String[] args){
        // displayText(combFrontpage("http://www.nytimes.com/", "*.story-heading", "p.story-body-text"));
        // displayText(combFrontpage("https://www.washingtonpost.com/", "div.headline", "div.article-body"));
        // displayText(combFrontpage("https://www.bbc.com/news/", "a.gs-c-promo-heading", "div.story-body__inner"));
          displayText(combFrontpage("https://www.bbc.com/news/", "*.title-link*", "div.story-body__inner"));

    }




    private static String combFrontpage(String frontPageURL, String headlineTag, String storyBodyTag, Boolean firstStoryOnly) {
        StringBuilder buff = new StringBuilder(150000);
        try {
            Document doc = Jsoup.connect(frontPageURL).get();
            Elements headlines = doc.select(headlineTag);
            String storyURLtoGet="";
            int i=0;
            if(!firstStoryOnly){
                for (Element ele : headlines) {
                    //passing in empty arguments so that a new object isn't created for every iteration.
                    getStories(i, frontPageURL,storyURLtoGet, ele, storyBodyTag, buff);
                    i++;
                }
            } else {

                getStories(i,frontPageURL,storyURLtoGet,headlines.first(), storyBodyTag,buff);
            }

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            buff.trimToSize();
            return buff.toString();
        }
    }
    private static void getStories(int i, String frontPageURL, String storyURLtoGet, Element ele, String storyBodyTag, StringBuilder buff){

            System.out.println("\n"+i);
            storyURLtoGet = ele.select("a[href]").attr("href");


            if (!storyURLtoGet.equals("")) {

                try {
                    String attemptFullHref=completeHref(frontPageURL,storyURLtoGet);
                    System.out.println(attemptFullHref);
                    article = Jsoup.connect(attemptFullHref).get();

                    article.select("div.image").remove();
                    articleContent = article.select(storyBodyTag);

                    buff.append(ele.text() + " : ");

                    for (Element artEle : articleContent) {
                        buff.append(cleanHTML(artEle.html()) + "\n");
                    }
                    buff.append("\n\n");

                } catch (IllegalArgumentException a) {
                    a.printStackTrace();
                    System.out.print(" Finished IllArgument for ");


                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.print(" Finished IOExcept for ");


                } finally {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

    }
    private static String combFrontpage(String frontPageURL, String headlineTag, String storyBodyTag){
        return combFrontpage(frontPageURL, headlineTag,storyBodyTag, false);
    }
    private static String obliterateNonNouns(String dirtyString){
        return "";
    }
    private static String cleanHTML(String dirtyHTML){
        String out;
        dirtyHTML=dirtyHTML.replaceAll("\\<[^<]*\\>", "");
        dirtyHTML=dirtyHTML.replaceAll("\\.","\\. ");
        out=dirtyHTML.replaceAll("\\.  ", "\\. ");
        out=dirtyHTML.replaceAll("(\\n *){2,}","");
        return out;
    }
    private static String extraCleaning(String dirtyText,String regex){
        String out;
        out=dirtyText.replaceAll(regex,"");
        return out;
    }
    private static void displayText(String in){
        JTextArea text = new JTextArea();
        text.append(in);
        JScrollPane scroller = new JScrollPane(text);

        JFrame frame = new JFrame();
        frame.add(scroller);
        frame.setSize(1000, 1500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    private static String completeHref(String parentSite, String href){
        String fullHref;
        if(href.startsWith("http://w"))
            fullHref=href;
        else{
            //concat with no overlap
            fullHref=parentSite.substring(0,parentSite.length()-(overlapSize(parentSite,href)+1))+href;
        }

        return fullHref;
    }
    private static int overlapSize(String a, String b){
        //get rid of slashes. This is probably gonna break shit, but I can't figure the algorithm out otherwise.
        a=a.substring(0,a.length()-1);
        b=b.substring(1);
       char aLast=a.charAt(a.length()-1);
       int bLastIndex=b.length()-1;
       int aLastIndex=a.length()-1;
       int backtrack=0;
       int forwardtrack=0;
       int overlapLength=0;

       for(int c=1;c<bLastIndex;c++){
           //find aLast to begin pattern matching
               backtrack=c;

               while(backtrack>=0 && forwardtrack<a.length()){
                   if(a.charAt(aLastIndex-forwardtrack)!=b.charAt(backtrack)){
                       break; //end loop, search further in b
                   } else {
                       backtrack--;
                       forwardtrack++;
                   }
               } //Checks that the string's overlap is on it's border, and not found in the middle of the word.

           if(backtrack<=0) {
               overlapLength= forwardtrack;
                break;
           }
       }

        return overlapLength+1;
    }

}
