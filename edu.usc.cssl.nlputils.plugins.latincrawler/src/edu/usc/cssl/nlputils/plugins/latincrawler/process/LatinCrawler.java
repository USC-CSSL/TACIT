/**
 * @author Niki Parmar <nikijitp@usc.edu>
 */

package edu.usc.cssl.nlputils.plugins.latincrawler.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.usc.cssl.nlputils.utilities.Log;

public class LatinCrawler implements ILatinCrawlerConstants {
	
	private String outputDir;
	private Map<String, String> authorNames; 
	private Set<String> skipBooks; //list of authors who has to be skipped because of irregular formatting of their pages
	
	public LatinCrawler() {
		authorNames = new HashMap<String, String>();
		skipBooks = new HashSet<String>();
		
		skipBooks.add("The Latin Library");
		skipBooks.add("The Classics Page");
		skipBooks.add("The Classics Homepage");
		skipBooks.add("Christian Latin");
		skipBooks.add("Thomas May");
		skipBooks.add("Contemporary Latin");
		skipBooks.add("Apollinaris Sidonius");
		skipBooks.add("The Miscellany");
		skipBooks.add("St. Thomas Aquinas");
		skipBooks.add("St. Jerome");
		//skipBooks.add("Leo the Great"); //check this
		skipBooks.add("Isidore of Seville");
		skipBooks.add("Seneca the Younger");
		skipBooks.add("Seneca the Elder");
		skipBooks.add("Miscellanea Carminum");
		skipBooks.add("Velleius");
		skipBooks.add("Neo-Latin");
		skipBooks.add("The Bible");
		skipBooks.add("Medieval Latin");
		skipBooks.add("Christian");
		skipBooks.add("Christina Latin");
		skipBooks.add("Medieval");
		skipBooks.add("Ius Romanum");
		skipBooks.add("Miscellany");
		skipBooks.add("Paulus Diaconus");
		
		getAuthors(); //get all the author names in the website
	}
	
	public Map<String, String> getAuthorNames() {
		return (Map<String, String>) ((HashMap<String, String>)authorNames).clone();
	}
	
	public Set<String> getSkipBooks() {
		return skipBooks;
	}
	
	public void setOutputDir(String outputDir){
		this.outputDir = outputDir;	
		createIfMissing(outputDir);
	}
	
	public static void main(String[] args) throws Exception{
		LatinCrawler lc = new LatinCrawler();
		lc.setOutputDir("C:\\Linda2");
		lc.crawl();
		//lc.getBooksByAuthor("Abbo Floriacensis","http://www.thelatinlibrary.com/abbofloracensis.html");
	}
	
	/** 
	 * Connect to the url and retrieve the document 
	 */
	
	protected Document retrieveDocumentFromUrl(String url) {
		Document doc = null;
		try {
			doc = Jsoup.connect(url).timeout(10 * 1000).get();
		} catch (IOException e) {
			// Error handling->will do later
		}
		return doc;
	}

	/**
	 * Get all author names from the website http://www.thelatinlibrary.com/
	 */
	public void getAuthors() {		
		try {
			getAuthorsByCategory("");
			getAuthorsByCategory("medieval.html");
			getAuthorsByCategory("christian.html");
			getAuthorsByCategory("neo.html");
			getAuthorsByCategory("misc.html");
			getAuthorsByCategory("ius.html");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get all author names belonging to a category
	 * @param category : all authors belonging to this category will be fetched
	 * @param isSubAuthor :
	 * @throws Exception
	 */
	private void getAuthorsByCategory(String category ) throws Exception {	
		String url = ILatinCrawlerConstants.CRAWLER_URL + category;
		Document doc = retrieveDocumentFromUrl(url);//Jsoup.connect(url).timeout(10*1000).get();
		Elements comboBoxAuthList = doc.getElementsByTag("option");
		Element authorTable = doc.getElementsByTag("table").get(0);
		
		if(comboBoxAuthList.size() != 0) {
			int size = comboBoxAuthList.size();
			for(int i =0; i<size; i++) {
					String name = comboBoxAuthList.get(i).text();
					//url = "http://www.thelatinlibrary.com/" + authorsList.get(i).attr("value");
					if(skipBooks.contains(name))
						continue;
					authorNames.put(name, ILatinCrawlerConstants.CRAWLER_URL + comboBoxAuthList.get(i).attr("value") );
			}
			authorTable = doc.getElementsByTag("table").get(1);
		}
		
		Elements tableAuthList = authorTable.getElementsByTag("td");
		for(Element auth : tableAuthList) {
			String name = auth.text();
			//url = auth.getElementsByTag("a").attr("abs:href");
			if(authorNames.containsKey(name) || skipBooks.contains(name))
				continue;
			authorNames.put(name,auth.getElementsByTag("a").attr("abs:href") );
		}
	}
	
	/**
	 * Crawls the website http://www.thelatinlibrary.com/ and extract all books into specified output folder 
	 * @throws IOException
	 */
	public void crawl() throws IOException{
		getAllBooks();
	}
	
	private void getAllBooks() throws IOException {
		Set<String> authors = (Set<String>) authorNames.keySet();
		for(String name:authors){
			getBooksByAuthor(name, authorNames.get(name));
		}
		return;
	}
	
	/**
	 * 
	 * @param name : name of the author
	 * @param url : url of author's page which lists all his books
	 * @throws IOException
	 */
	public void getBooksByAuthor(String author, String url) throws IOException {		
		Assert.isNotNull(author, "Parameter author can't be empty");
		Assert.isNotNull(author, "Parameter url can't be empty");
		
		String authDir = outputDir + File.separator + author;
		createIfMissing(authDir);
		appendLog("\nExtracting Books of Author  "+ author +"...");
		
		Map<String, BookData> myBooks = new HashMap<String, BookData>();
		try {
			Document doc = retrieveDocumentFromUrl(url);// Jsoup.connect(url).timeout(10*1000).get();
			Boolean isText = doc.getElementsByTag("a").get(0).attr("abs:href").contains("#");
			if(!isText) { 
				getBooksByPage(doc, author, authDir, myBooks);
			}
			if(isText || myBooks.size() == 0) {
				myBooks.put(url, new BookData(author, url, authDir));
			}
		} catch(Exception e) {	
			appendLog("Something went wrong when extracting books of Author " + author);
		}
		
		for (Map.Entry<String, BookData> entry : myBooks.entrySet())
		{
			String bookName = entry.getValue().getBookName();
			String bookUrl = entry.getValue().getBookUrl();
			String bookDir = entry.getValue().getBookDir();
			getBookContent(bookUrl, bookName, bookDir);
		}
	}		
	
	class BookData {
		
		private String bookName;
		private String bookUrl;
		private String bookDir;
		
		BookData(String bookName, String bookUrl, String bookDir) {
			this.bookName = bookName;
			this.bookUrl = bookUrl;
			this.bookDir = bookDir;
		}
		public String getBookUrl() {
			return bookUrl;
		}
		public String getBookDir() {
			return bookDir;
		}
		public String getBookName() {
			return bookName;
		}
		public void setBookName(String bookName) {
			this.bookName = bookName;
		}
		public void setBookUrl(String bookUrl) {
			this.bookUrl = bookUrl;
		}
		public void setBookDir(String bookDir) {
			this.bookDir = bookDir;
		}		
	}

	/**
	 * 
	 * @param doc This is the document from which books are collected.
	 * @param author This is the author of all the books in this page
	 * @param authDir This is the output directory of author. Books are to be extracted into this directory 
	 * @param bookList The book information gathered from the page is filled into this Map for the callee function to access
	 */
	private void getBooksByPage(Document doc, String author, String authDir, Map<String, BookData> bookList){
		Elements subLists = doc.select("div.work"); //the books are listed inside div.work
		Elements subHeaders = doc.select("h2.work"); //headers of sections 
		
		//sometimes there are no div.work . Instead books are listed inside a table element.
		if(subLists.size() == 0 ) {
			Elements tableLists = doc.select("table");
			for (Element table : tableLists ) {
				if(table.getElementsByTag("a").size() > 0) {
					subLists.add(table);
				}
				else {
					subHeaders.add(table);
				}
			}
		}	
		
		int i = 0, size1 = subLists.size(), size2 = subHeaders.size(), j = 0;
		Element head = null;
		String subDir = "";
		authDir +=  File.separator; //books will be inside authDir>subDir>
		
		while(i< size1 || j<size2) {
			try{
				if(j < size2) {
					head = subHeaders.get(j);
					Elements booksLink = head.getElementsByTag("a");
					boolean booksPresent = getBooksFromElement(booksLink, bookList, subDir, author);
					if (booksPresent) {
						j++;
						continue;
					}
					subDir = authDir + head.text() + File.separator;		
				}
				else
					subDir = authDir + File.separator;

				if(i < size1) {
					Elements booksLink = subLists.get(i).getElementsByTag("a");				
					getBooksFromElement(booksLink, bookList, subDir, author);
					i++;	
				}
				j++;
			} catch(Exception e) {
				appendLog("Something went wrong when extracting books of Author " + author);
			}
		}
	}
	
	private boolean getBooksFromElement(Elements bookLinks, Map<String, BookData> bookList, String bookDir, String author) {
		if(bookLinks.size() <= 0)
			return false;
		for(int i=0; i<bookLinks.size(); i++) {
			String bookUrl = bookLinks.get(i).attr("abs:href");
			String bookName  = bookLinks.get(i).text();
			if(skipBooks.contains(bookName))
				continue;
			if(authorNames.containsKey(bookName)|| (bookName.toLowerCase()).equals(author.toLowerCase()))
				continue;						
			BookData book = new BookData(bookName, bookUrl, bookDir);
			bookList.put(bookUrl, book);
		}
		return true;	
	}
	
	private String getBookNameFromBook(Document bookDoc, String defBookName) {
		
		String bookName = defBookName;
		if(bookDoc.select("p.pagehead")!= null && bookDoc.select("p.pagehead").size() > 0 )
			bookName = bookDoc.select("p.pagehead").last().text();
		else if(bookDoc.select("h1")!= null && bookDoc.select("h1").size() >  0)
			bookName = bookDoc.select("h1").first().text();
		else if(bookDoc.select("title")!=null)
			bookName = bookDoc.select("title").first().text();
		return bookName;
	}
	
	private void getBookContent(String bookUri, String bookName, String bookDir) throws IOException {
		BufferedWriter csvWriter= null;
		appendLog("Extracting Content of book  "+ bookName +"...");
		try {
			Document doc = retrieveDocumentFromUrl(bookUri);//Jsoup.connect(bookUri).timeout(10*10000).get();
			//bookName = getBookNameFromDoc(doc, bookName);
			bookName = bookName.replaceAll("[.,;\"!-()\\[\\]{}:?'/\\`~$%#@&*_=+<>*$]", "");
			createIfMissing(bookDir);
			csvWriter  = new BufferedWriter(new FileWriter(new File(bookDir + System.getProperty("file.separator") + bookName+".txt")));
			Elements content = doc.getElementsByTag("p"); 
			if(content.size() == 0)
			{
				if(csvWriter!=null)
					csvWriter.close();
				getBookContent2(bookUri, bookName, bookDir);
				return;
			}
			for (Element c : content){
				csvWriter.write(c.text()+"\n");
			}
		}catch(Exception e){
			getBookContent2(bookUri, bookName, bookDir);
		}finally{
			if(csvWriter!=null)
				csvWriter.close();
		}
	}
	

	private void getBookContent2(String bookUri, String bookDir, String authorDir) throws IOException {
		BufferedWriter csvWriter= null;
		try{
			csvWriter  = new BufferedWriter(new FileWriter(new File(authorDir + System.getProperty("file.separator") + bookDir+".txt")));
			Document doc = Jsoup.parse(new URL(bookUri).openStream(), "UTF-16", bookUri);
			Elements content = doc.getElementsByTag("p"); 
				for (Element c : content){
					csvWriter.write(c.text()+"\n");
				}
			}catch(Exception e){
				appendLog("Something went wrong when extracting book " + bookDir);
			}finally{
				if(csvWriter!=null)
					csvWriter.close();
			}
	}
	
	/**
	 * Creates a directory in the file system if it does not already exists
	 * @param folder : full path of the directory which has to be created. 
	 */
	private void createIfMissing(String folder) {
		File path = new File(folder);
		if (!path.exists()){
			path.mkdirs();
		}
	}
	
	@Inject IEclipseContext context;	
	private void appendLog(String message){
		Log.append(context, message);
	}
	public void writeReadMe(String location){
		StringBuilder readMe = new StringBuilder();
		File readme = new File(location+"/README.txt");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(readme));
			String plugV = Platform.getBundle("edu.usc.cssl.nlputils.plugins.latincrawler").getHeaders().get("Bundle-Version");
			String appV = Platform.getBundle("edu.usc.cssl.nlputils.application").getHeaders().get("Bundle-Version");
			Date date = new Date();
			bw.write("Latin Crawler Output\n--------------------\n\nApplication Version: "+appV+"\nPlugin Version: "+plugV+"\nDate: "+date.toString()+"\n\n");
			bw.write(readMe.toString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
