/**
 * @author Niki Parmar <nikijitp@usc.edu>
 */

package edu.usc.cssl.nlputils.crawlers.latin.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LatinCrawler {
	private StringBuilder readMe = new StringBuilder();
	String outputDir;
	private Map<String, String> authorNames;
	Set<String> skipBooks;
	private SubProgressMonitor monitor;
	private int work;

	public LatinCrawler() {
		authorNames = new HashMap<String, String>();
		// authorUrl = new HashSet<String>();
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
		// skipBooks.add("Leo the Great"); //check this
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
	 * Crawls the website http://www.thelatinlibrary.com/ and extract all books into specified output folder 
	 * @throws IOException
	 */
	public void crawl(SubProgressMonitor monitor,int work) throws IOException{
		this.monitor = monitor;
		this.work = work;
		monitor.beginTask("crawling Author list...", work);
		getAllBooks();
		this.monitor.done();
	}
	
	private void getAllBooks() throws IOException {
		Set<String> authors = (Set<String>) authorNames.keySet();
		int singleWork = work / authors.size();
		for(String name:authors){
			if(monitor.isCanceled()){
				throw new OperationCanceledException(); 
			}
			getBooksByAuthor(name, authorNames.get(name));
			monitor.worked(singleWork);
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

	public void initialize(String outputDir) {
		this.outputDir = outputDir;
	}

	public Map<String, String> getAuthorNames() throws Exception {
		Map<String, String> authorNames = new HashMap<String, String>();
		authorNames.putAll(getAuthorsList("http://www.thelatinlibrary.com/",
				false));
		authorNames.putAll(getAuthorsList(
				"http://www.thelatinlibrary.com/medieval.html", true));
		authorNames.putAll(getAuthorsList(
				"http://www.thelatinlibrary.com/christian.html", true));
		authorNames.putAll(getAuthorsList(
				"http://www.thelatinlibrary.com/neo.html", true));
		authorNames.putAll(getAuthorsList(
				"http://www.thelatinlibrary.com/misc.html", true));
		authorNames.putAll(getAuthorsList(
				"http://www.thelatinlibrary.com/ius.html", true));
		//authorNames.put("<All>", "All authors in the list");
		return authorNames;
	}

	public Map<String, String> getAuthorsList(String url, boolean isSubAuthor)
			throws Exception {
		int i, size = 0;
		String name;
		Map<String, String> authorNames = new HashMap<String, String>();

		Document doc = Jsoup.connect(url).timeout(10 * 1000).get();
		if (!isSubAuthor) {
			Elements authorsList = doc.getElementsByTag("option");

			size = authorsList.size();
			int count = 0;
			for (i = 0; i < size; i++) {
				name = authorsList.get(i).text();
				// url = "http://www.thelatinlibrary.com/" +
				// authorsList.get(i).attr("value");
				if (skipBooks.contains(name))
					continue;
				authorNames.put(name, "http://www.thelatinlibrary.com/"
						+ authorsList.get(i).attr("value"));
				count++;
			}
		}

		Element secondList = null;
		if (isSubAuthor)
			secondList = doc.getElementsByTag("table").get(0);
		else
			secondList = doc.getElementsByTag("table").get(1);
		Elements auth2List = secondList.getElementsByTag("td");
		for (Element auth : auth2List) {
			name = auth.text();
			// url = auth.getElementsByTag("a").attr("abs:href");
			if (authorNames.containsKey(name))
				continue;
			if (skipBooks.contains(name))
				continue;
			authorNames.put(name, auth.getElementsByTag("a").attr("abs:href"));
		}

		return authorNames;
	}

	public void getAllAuthors() throws IOException {
		Set<String> authors = (Set<String>) authorNames.keySet();
		for (String name : authors) {
			getSingleAuthor(name, authorNames.get(name));
		}
		return;
	}

	public void getSingleAuthor(String name, String url) throws IOException {
		String aurl = outputDir + File.separator + name;
		File authorDir = new File(aurl);
		if (!authorDir.exists()) {
			authorDir.mkdirs();
		}
		getBooks(name, url, aurl);
	}

	/*
	 * Get all books of a single author Recursive function to trace all books
	 * and links of a particular author
	 */
	private void getBooks(String author, String aurl, String apath)
			throws IOException {

		try {
			Document doc = Jsoup.connect(aurl).timeout(10 * 1000).get();
			Elements subLists = doc.select("div.work");
			if (subLists != null && subLists.size() > 0) {
				getBooksList(author, aurl, doc, apath);
				return;
			}

			Elements booksList = doc.getElementsByTag("td");

			int count = 0;
			String bookname = "";
			int i = 0;
			if (booksList != null) {
				int size = booksList.size();
				for (i = 0; i < size; i++) {
					Element bookItem = booksList.get(i);
					String bookText = bookItem.getElementsByTag("a").attr(
							"abs:href");
					if (bookText.contains("#"))
						continue;
					bookname = bookItem.text();
					if (bookText.isEmpty() || bookText == null)
						continue;
					if (skipBooks.contains(bookname))
						continue;
					if (authorNames.containsKey(bookname))
						continue;
					getBooks(bookname, bookText, apath);
					count++;

				}
			}
			if (count == 0) {
				if (doc.select("title") != null
						&& doc.select("title").size() > 0)
					bookname = doc.select("title").first().text();
				else if (doc.select("p.pagehead") != null
						&& doc.select("p.pagehead").size() > 0)
					bookname = doc.select("p.pagehead").first().text();
				else if (doc.select("h1") != null
						&& doc.select("h1").size() > 0)
					bookname = doc.select("h1").first().text();
				else
					bookname = author;
				getContent(aurl, bookname, apath);
			}
		} catch (Exception e) {
			// ConsoleView.writeInConsole("Something went wrong when extracting books of Author "
			// + author + e);
		}
	}

	private void getBooksList(String author, String aurl, Document doc,
			String apath) {
		Elements subLists = doc.select("div.work");
		Elements subHeaders = doc.select("h2.work");
		int i = 0, size1 = subLists.size(), size2 = subHeaders.size(), j = 0;
		String bookText = "";
		String bookname = "";
		Element head = null;
		int k;

		// handle count of headers and div
		while (i < size1 || j < size2) {
			try {
				if (j < size2) {
					head = subHeaders.get(j);
					Elements bookLink = head.getElementsByTag("a");
					if (bookLink != null && bookLink.size() > 0) {
						// make for all links
						bookText = bookLink.get(0).attr("abs:href");
						bookname = bookLink.get(0).text();
						if (skipBooks.contains(bookname))
							continue;
						if (authorNames.containsKey(bookname)
								|| (bookname.toLowerCase()).equals(author
										.toLowerCase()))
							continue;
						File authorDir = new File(apath + File.separator
								+ bookname);
						if (!authorDir.exists()) {
							authorDir.mkdirs();
						}
						String apath1 = authorDir.toString();
						getBooks(bookname, bookText, apath1);
						j++;
						continue;
					}
				}

				if (i < size1) {
					Element list = subLists.get(i);
					Elements booksList = list.getElementsByTag("td");

					String authorNewDir = apath + File.separator;
					if (j < size2)
						authorNewDir += head.text();
					else
						authorNewDir += "Others";
					File authorDir = new File(authorNewDir);
					if (!authorDir.exists()) {
						authorDir.mkdirs();
					}
					String apath1 = authorDir.toString();

					k = 0;
					int count1 = 0;
					Element bookItem = null;
					if (booksList != null) {
						for (k = 0; k < booksList.size(); k++) {
							bookItem = booksList.get(k);
							bookText = bookItem.getElementsByTag("a").attr(
									"abs:href");

							if (bookText == null)
								continue;
							bookname = bookItem.text();
							if (skipBooks.contains(bookname))
								continue;
							if (authorNames.containsKey(bookname))
								continue;
							getBooks(bookname, bookText, apath1);
							count1++;
						}
					}

					if (count1 == 0) {
						if (doc.select("title") != null)
							bookname = doc.select("title").first().text();
						else if (doc.select("p.pagehead") != null
								&& doc.select("p.pagehead").size() > 0)
							bookname = doc.select("p.pagehead").first().text();
						else if (doc.select("h1") != null
								&& doc.select("h1").size() > 0)
							bookname = doc.select("h1").first().text();
						else
							bookname = author;
						getContent(aurl, bookname, apath1);
					}
					i++;

				}
				j++;
			} catch (Exception e) {
				// ConsoleView.writeInConsole("Something went wrong when extracting books of Author "
				// + author);
			}
		}
	}

	private void getContent(String bookUri, String bookDir, String authorDir)
			throws IOException {
		BufferedWriter csvWriter = null;
		try {
			bookDir = bookDir.replaceAll(
					"[.,;\"!-()\\[\\]{}:?'/\\`~$%#@&*_=+<>*$]", "");
			csvWriter = new BufferedWriter(new FileWriter(new File(authorDir
					+ System.getProperty("file.separator") + bookDir + ".txt")));
			Document doc = Jsoup.connect(bookUri).timeout(10 * 10000).get();
			Elements content = doc.getElementsByTag("p");
			if (content.size() == 0) {
				if (csvWriter != null)
					csvWriter.close();
				getBookContent(bookUri, bookDir, authorDir);
				return;
			}
			for (Element c : content) {
				csvWriter.write(c.text() + "\n");
			}
		} catch (Exception e) {
			getBookContent(bookUri, bookDir, authorDir);
		} finally {
			if (csvWriter != null)
				csvWriter.close();
		}
	}


	private void getBookContent(String bookUri, String bookName, String bookDir) throws IOException {
		BufferedWriter csvWriter= null;
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
			}finally{
				if(csvWriter!=null)
					csvWriter.close();
			}
	}	/* The sub libraries */
	/*
	 * public void getAllSubAuthors(String connectUrl, String output) throws
	 * IOException{ int i = 0, size = 0; String name, url; Document doc =
	 * Jsoup.connect(connectUrl).timeout(10*1000).get();
	 * 
	 * Element secondList = doc.getElementsByTag("table").get(0); Elements
	 * auth2List = secondList.getElementsByTag("td"); for(Element auth :
	 * auth2List) { name = auth.text(); url =
	 * auth.getElementsByTag("a").attr("abs:href");
	 * if(authorNames.containsKey(name)) continue; //authorNames.add(name);
	 * if(skipBooks.contains(name)) continue; String aurl = output +
	 * File.separator + name; File authorDir = new File(aurl);
	 * if(!authorDir.exists()){ authorDir.mkdirs(); }
	 * //ConsoleView.writeInConsole("Extracting Books of Author  "+ name +"...");
	 * appendLog("\nExtracting Books of Author  "+ name +"..."); getBooks(name,
	 * url, aurl); i++;
	 * 
	 * }
	 * 
	 * return; }
	 */
	/*
	 * private void callSpecialAuthors(String url, String name) throws
	 * IOException{ File authorDir = new File(outputDir + File.separator +
	 * name); if(!authorDir.exists()){ authorDir.mkdirs(); }
	 * //getAllSubAuthors(url, outputDir + File.separator + name); }
	 * 
	 * 
	 * }
	 */

	public void writeReadMe(String location) {
		File readme = new File(location + "/README.txt");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(readme));
			String plugV = Platform
					.getBundle("edu.usc.cssl.nlputils.plugins.latincrawler")
					.getHeaders().get("Bundle-Version");
			String appV = Platform
					.getBundle("edu.usc.cssl.nlputils.application")
					.getHeaders().get("Bundle-Version");
			Date date = new Date();
			bw.write("Latin Crawler Output\n--------------------\n\nApplication Version: "
					+ appV
					+ "\nPlugin Version: "
					+ plugV
					+ "\nDate: "
					+ date.toString() + "\n\n");
			bw.write(readMe.toString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
