package edu.usc.cssl.tacit.crawlers.gutenberg.services;

import java.util.HashMap;

public class GutenbergConstants {
	public static HashMap<String, String> site2Link = new HashMap<String, String>();
	public static HashMap<Integer, String[]> sites = new HashMap<Integer, String[]>();
	static String animals[] = new String[]{"Birds",
			"Insects",
			"Mammals",
			"Reptiles and Amphibians",
			"Trapping"};
	
	static String children[] = new String[]{"Anthologies",
			"Biography",
			"Book Series",
			"Verse",
			"Christmas",
			"Fiction",
			"History",
			"Instructional Books",
			"Literature",
			"Myths and Fairy Tales",
			"Religion",
			"School Stories"
			
	};
	
	static String classics[] = new String[]{"Classics"};
	
	static String Countries[] =new String[]{"Africa",
			"Argentina",
			"Australia",
			"Bulgaria",
			"Canada",
			"Czech",
			"Egypt",
			"France",
			"Germany",
			"Greece",
			"India",
			"Italy",
			"New Zealand",
			"Norway",
			"South Africa",
			"South America",
			"Travel",
			"United Kingdom",
			"United States"
	};
	static String Crime[] = new String[]{"Crime Fiction",
			"Crime Non Fiction",
			"Detective Fiction",
			"Mystery Fiction"
	};
	
	static String Knowledge[] = new String[]{"Education",
			"Language Education"
	};
	
	static String fiction[] = new String[]{"Adventure",
			"Children's Fiction",
			"Crime Fiction",
			"Detective Fiction",
			"Erotic Fiction",
			"Fantasy",
			"General Fiction",
			"Gothic Fiction",
			"Historical Fiction",
			"Horror",
			"Humor",
			"Movie Books",
			"Mystery Fiction",
			"Precursors of Science Fiction",
			"Romantic Fiction",
			"School Stories",
			"Science Fiction",
			"Western"
	};
	
	static String fine_arts[] = new String[]{"Architecture",
			"Art"
	};
	
	static String general_works[] = new String[]{"Children's Periodicals:Dew Drops",
			"Children's Periodicals:The Girls Own Paper",
			"Children's Periodicals:Golden Days for Boys and Girls",
			"Children's Periodicals:The Great Round World And What Is Going On In It",
			"Children's Periodicals:The Nursery",
			"Children's Periodicals:St. Nicholas Magazine for Boys and Girls",
			"Reference"
	};
	
	static String geography[] = new String[]{"Anthropology",
			"CIA World Factbooks",
			"Folklore",
			"Maps and Cartography",
			"Women's Travel Journals"
	};
	
	static String history[] = new String[]{"Archaeology",
			"Biographies",
			"Children's History",
			"Classical Antiquity"
	};
	
	static String language_and_literature[] = new String[]{"Esperanto",
			"German Language Books",
			"Language Education",
			"Plays"
	};
	
	static String law[] = new String[]{"British Law",
			"Canon Law",
			"Noteworthy Trials",
			"United States Law"
	};
	
	static String music[] = new String[]{"Music",
			"Opera"
	};
	
	static String periodicals[] = new String[]{"Ainslee's",
			"The Aldine",
			"The American Architect and Building News",
			"The American Journal of Archaeology",
			"The American Missionary",
			"The American Quarterly Review",
			"The Arena",
			"The Argosy",
			"Armour's Monthly Cook Book",
			"Astounding Stories",
			"The Atlantic Monthly",
			"The Baptist Magazine",
			"Barnavännen",
			"The Bay State Monthly",
			"Bird-Lore",
			"Birds, Illustrated by Color Photography",
			"Blackwood's Edinburgh Magazine",
			"The Botanical Magazine",
			"The Brochure Series of Architectural Illustration",
			"Buchanan's Journal of Man",
			"Bulletin of Lille",                          //French.Page requires translation
			"The Catholic World",
			"Celtic Magazine",
			"Chambers's Edinburgh Journal",
			"The Christian Foundation",
			"The Church of England Magazine",
			"The Contemporary Review",
			"Continental Monthly",
			"Current History",
			"De Aarde en haar Volken",                //Other language.Page requires translation
			"Donahoe's Magazine",
			"The Economist",
			"The Esperantist",
			"The Galaxy",
			"Garden and Forest",
			"Godey's Lady's Book",
			"Graham's Magazine",
			"Harper's New Monthly Magazine",
			"Harper's Young People",
			"The Idler",
			"The Illustrated War News",
			"The International Magazine of Literature, Art, and Science",
			"The Irish Ecclesiastical Record",
			"The Irish Penny Journal",
			"Journal of Entomology and Zoology",
			"The Journal of Negro History",
			"The Knickerbocker",
			"L'Illustration",
			"Lippincott's Magazine",
			"Little Folks",
			"London Medical Gazette",
			"The Mayflower",
			"McClure's Magazine",
			"The Menorah Journal",
			"The Mentor",
			"The Mirror of Literature, Amusement, and Instruction",
			"The Mirror of Taste, and Dramatic Censor",
			"Mother Earth",
			"Mrs Whittelsey's Magazine for Mothers and Daughters",
			"The National Preacher",
			"The North American Medical and Surgical Journal",
			"Northern Nut Growers Association",
			"Notes and Queries",               
			"Our Young Folks",
			"Poetry, A Magazine of Verse",
			"Popular Science Monthly",
			"Prairie Farmer",
			"Punch",
			"Punchinello",
			"Scientific American",
			"The Scrap Book",
			"Scribner's Magazine",
			"The Speaker",
			"The Stars and Stripes",
			"The Strand Magazine",
			"The Haslemere Museum Gazette",
			"The Unpopular Review",
			"The Writer",
			"The Yellow Book",

	};
	
	static String psychology_and_philosophy[] = new String[]{"Bibliomania",
			"Philosophy",
			"Psychology",
			"Witchcraft"
	};
	
	static String religion[] = new String[]{"Atheism",
			"Bahá'í Faith",
			"Buddhism",
			"Christianity",
			"Hinduism",
			"Islam",
			"Judaism",
			"Latter Day Saints",
			"Mythology",
			"Paganism"
	};
	
	static String science[] = new String[]{"Astronomy",
			"Biology",
			"Botany",
			"Chemistry",
			"Ecology",
			"Geology",
			"Mathematics",
			"Microbiology",
			"Microscopy",
			"Mycology",
			"Natural History",
			"Physics",
			"Physiology",
			"Science",
			"Scientific American",
			"Zoology"
	};
	
	static String social_sciences[] = new String[]{"Anarchism",
			"Racism",
			"Slavery",
			"Sociology",
			"Suffrage",
			"Transportation"
	};
	
	static String technology[] = new String[]{"Cookery",
			"Crafts",
			"Engineering",
			"Manufacturing",
			"Technology",
			"Woodwork"
			
	};

	static String wars[] = new String[]{"American Revolutionary War",
			"Boer War",
			"English Civil War",
			"Spanish American War",
			"US Civil War",
			"World War I",
			"World War II"
	}; 
	
	static{
	sites.put(0, animals);
	sites.put(1, children);
	sites.put(2,classics);
	sites.put(3, Countries);
	sites.put(4, Crime);
	sites.put(5, Knowledge);
	sites.put(6, fiction);
	sites.put(7, fine_arts);
	sites.put(8, general_works);
	sites.put(9, geography);
	sites.put(10, history);
	sites.put(11, language_and_literature);
	sites.put(12, law);
	sites.put(13, music);
	sites.put(14, periodicals);
	sites.put(15, psychology_and_philosophy);
	sites.put(16, religion);
	sites.put(17, science);
	sites.put(18, social_sciences);
	sites.put(19, technology);
	sites.put(20, wars);
	site2Link.put("Birds", "Animals-Wild_(Bookshelf)-Birds");
	site2Link.put("Insects", "Animals-Wild_(Bookshelf)-Insects");
	site2Link.put("Mammals", "Animals-Wild_(Bookshelf)-Mammals");
	site2Link.put("Reptiles and Amphibians", "Animals-Wild_(Bookshelf)-Reptiles_and_Amphibians");
	site2Link.put("Trapping", "Animals-Wild_(Bookshelf)-Trapping");
	
	site2Link.put("Anthologies", "Children%27s_Anthologies_(Bookshelf)");
	site2Link.put("Biography", "Children%27s_Biography_(Bookshelf)");
	site2Link.put("Book Series", "Children%27s_Book_Series_(Bookshelf)");
	site2Link.put("Verse", "Children%27s_Verse_(Bookshelf)");
	site2Link.put("Christmas", "Christmas_(Bookshelf)");
	site2Link.put("Fiction", "Children%27s_Fiction_(Bookshelf)");
	site2Link.put("History", "Children%27s_History_(Bookshelf)");
	site2Link.put("Instructional Books", "Children%27s_Instructional_Books_(Bookshelf)");
	site2Link.put("Literature", "Children%27s_Literature_(Bookshelf)");
	site2Link.put("Myths and Fairy Tales", "Children%27s_Myths,_Fairy_Tales,_etc._(Bookshelf)");
	site2Link.put("Religion", "Children%27s_Religion_(Bookshelf)");
	site2Link.put("School Stories", "School_Stories_(Bookshelf)");
	
	site2Link.put("Classics", "Category:Classics_Bookshelf");
	
	site2Link.put("Africa", "Africa_(Bookshelf)");
	site2Link.put("Argentina", "Argentina_(Bookshelf)");
	site2Link.put("Australia", "Australia_(Bookshelf)");
	site2Link.put("Bulgaria","Bulgaria_(Bookshelf)");
	site2Link.put("Canada","Canada_(Bookshelf)");
	site2Link.put("Czech","Czech_(Bookshelf)");
	site2Link.put("Egypt","Egypt_(Bookshelf)");
	site2Link.put("France","France_(Bookshelf)");
	site2Link.put("Germany","Germany_(Bookshelf)");
	site2Link.put("Greece","Greece_(Bookshelf)");
	site2Link.put("India","India_(Bookshelf)");
	site2Link.put("Italy","Italy_(Bookshelf)");
	site2Link.put("New Zealand","New_Zealand");
	site2Link.put("Norway","Norway_(Bookshelf)");
	site2Link.put("South Africa","South_Africa_(Bookshelf)");
	site2Link.put("South America","South_America_(Bookshelf)");
	site2Link.put("Travel","Travel_(Bookshelf)");
	site2Link.put("United Kingdom","United_Kingdom_(Bookshelf)");
	site2Link.put("United States","United_States_(Bookshelf)");
	
	site2Link.put("Crime Fiction","Crime_Fiction_(Bookshelf)");
	site2Link.put("Crime Non Fiction","Crime_Nonfiction_(Bookshelf)");
	site2Link.put("Detective Fiction","Detective_Fiction_(Bookshelf)");
	site2Link.put("Mystery Fiction","Mystery_Fiction_(Bookshelf)");
	
	site2Link.put("Education","Education");
	site2Link.put("Language Education","Language_Education_(Bookshelf)");
	
	site2Link.put("Adventure","Adventure_(Bookshelf)");
	site2Link.put("Children's Fiction","Children%27s_Fiction_(Bookshelf)");
	site2Link.put("Crime Fiction","Crime_Fiction_(Bookshelf)");
	site2Link.put("Detective Fiction","Detective_Fiction_(Bookshelf)");
	site2Link.put("Erotic Fiction","Erotic_Fiction_(Bookshelf)");
	site2Link.put("Fantasy","Fantasy_(Bookshelf)");
	site2Link.put("General Fiction","General_Fiction");
	site2Link.put("Gothic Fiction","Gothic_Fiction_(Bookshelf)");
	site2Link.put("Historical Fiction","Historical_Fiction_(Bookshelf)");
	site2Link.put("Horror","Horror_(Bookshelf)");
	site2Link.put("Humor","Humor_(Bookshelf)");
	site2Link.put("Movie Books","Movie_Books_(Bookshelf)");
	site2Link.put("Mystery Fiction","Mystery_Fiction_(Bookshelf)");
	site2Link.put("Precursors of Science Fiction","Precursors_of_Science_Fiction_(Bookshelf)");
	site2Link.put("Romantic Fiction","Romantic_Fiction_(Bookshelf)");
	site2Link.put("School Stories","School_Stories_(Bookshelf)");
	site2Link.put("Science Fiction","Science_Fiction_(Bookshelf)");
	site2Link.put("Western","Western_(Bookshelf)");
	
	site2Link.put("Architecture","Architecture_(Bookshelf)");
	site2Link.put("Art","Art_(Bookshelf)");
	
	site2Link.put("Children's Periodicals:Dew Drops","Dew_Drops_(Bookshelf)");
	site2Link.put("Children's Periodicals:The Girls Own Paper","The_Girls_Own_Paper_(Bookshelf)");
	site2Link.put("Children's Periodicals:Golden Days for Boys and Girls","Golden_Days_for_Boys_and_Girls_(Bookshelf)");
	site2Link.put("Children's Periodicals:The Great Round World And What Is Going On In It","The_Great_Round_World_And_What_Is_Going_On_In_It_(Bookshelf)");
	site2Link.put("Children's Periodicals:The Nursery","The_Nursery_(Bookshelf)");
	site2Link.put("Children's Periodicals:St. Nicholas Magazine for Boys and Girls","St._Nicholas_Magazine_for_Boys_and_Girls_(Bookshelf)");
	site2Link.put("Reference","Reference_(Bookshelf)");
	
	site2Link.put("Anthropology","Anthropology_(Bookshelf)");
	site2Link.put("CIA World Factbooks","CIA_World_Factbooks_(Bookshelf)");
	site2Link.put("Folklore","Folklore_(Bookshelf)");
	site2Link.put("Maps and Cartography","Maps_and_Cartography_(Bookshelf)");
	site2Link.put("Women's Travel Journals","Women%27s_Travel_Journals_(Bookshelf)");
	
	site2Link.put("Archaeology","Archaeology_(Bookshelf)");
	site2Link.put("Biographies","Biographies_(Bookshelf)");
	site2Link.put("Children's History","Children%27s_History_(Bookshelf)");
	site2Link.put("Classical Antiquity","Classical_Antiquity_(Bookshelf)");
	
	site2Link.put("Esperanto","Esperanto_(Bookshelf)");
	site2Link.put("German Language Books","German_Language_Books_(Bookshelf)");
	site2Link.put("Language Education","Language_Education_(Bookshelf)");
	site2Link.put("Plays","Plays_(Bookshelf)");
	
	site2Link.put("British Law","British_Law_(Bookshelf)");
	site2Link.put("Canon Law","Canon_Law");
	site2Link.put("Noteworthy Trials","Noteworthy_Trials(Bookshelf)");
	site2Link.put("United States Law","United_States_Law_(Bookshelf)");
	
	site2Link.put("Music","Music_(Bookshelf)");
	site2Link.put("Opera","Opera_(Bookshelf)");
	
	site2Link.put("Bibliomania","Bibliomania_(Bookshelf)");
	site2Link.put("Philosophy","Philosophy_(Bookshelf)");
	site2Link.put("Psychology","Psychology_(Bookshelf)");
	site2Link.put("Witchcraft","Witchcraft_(Bookshelf)");
	
	site2Link.put("Atheism","Atheism_(Bookshelf)");
	site2Link.put("Bahá'í Faith","Bahá%27í_Faith_(Bookshelf)");
	site2Link.put("Buddhism","Buddhism_(Bookshelf)");
	site2Link.put("Christianity","Christianity_(Bookshelf)");
	site2Link.put("Hinduism","Hinduism_(Bookshelf)");
	site2Link.put("Islam","Islam_(Bookshelf)");
	site2Link.put("Judaism","Judaism_(Bookshelf)");
	site2Link.put("Latter Day Saints","Latter_Day_Saints_(Bookshelf)");
	site2Link.put("Mythology","Mythology_(Bookshelf)");
	site2Link.put("Paganism","Paganism_(Bookshelf)");
	
	site2Link.put("Astronomy","Astronomy_(Bookshelf)");
	site2Link.put("Biology","Biology_(Bookshelf)");
	site2Link.put("Botany","Botany_(Bookshelf)");
	site2Link.put("Chemistry","Chemistry_(Bookshelf)");
	site2Link.put("Ecology","Ecology_(Bookshelf)");
	site2Link.put("Geology","Geology_(Bookshelf)");
	site2Link.put("Mathematics","Mathematics_(Bookshelf)");
	site2Link.put("Microbiology","Microbiology_(Bookshelf)");
	site2Link.put("Microscopy","Microscopy_(Bookshelf)");
	site2Link.put("Mycology","Mycology_(Bookshelf)");
	site2Link.put("Natural History","Natural_History_(Bookshelf)");
	site2Link.put("Physics","Physics_(Bookshelf)");
	site2Link.put("Physiology","Physiology_(Bookshelf)");
	site2Link.put("Science","Science");
	site2Link.put("Scientific American","Scientific_American_(Bookshelf)");
	site2Link.put("Zoology","Zoology_(Bookshelf)");
	
	site2Link.put("Anarchism","Anarchism_(Bookshelf)");
	site2Link.put("Racism","Racism_(Bookshelf)");
	site2Link.put("Slavery","Slavery_(Bookshelf)");
	site2Link.put("Sociology","Sociology_(Bookshelf)");
	site2Link.put("Suffrage","Suffrage");
	site2Link.put("Transportation","Transportation_(Bookshelf)");
	
	site2Link.put("Energy Research","fenrg");
	site2Link.put("ICT","fict");
	site2Link.put("Materials","fmats");
	site2Link.put("Mechanical Engineering","fmech");
	site2Link.put("Robotics and AI","frobt");
	site2Link.put("Communication","fcomm");
	site2Link.put("Digital Humanities","fdigh");
	site2Link.put("Sociology","fsoc");
	
	site2Link.put("Cookery","Cookery_(Bookshelf)");
	site2Link.put("Crafts","Crafts_(Bookshelf)");
	site2Link.put("Engineering","Engineering_(Bookshelf)");
	site2Link.put("Manufacturing","Manufacturing");
	site2Link.put("Technology","Technology_(Bookshelf)");
	site2Link.put("Woodwork","Woodwork");
	
	site2Link.put("American Revolutionary War","American_Revolutionary_War_(Bookshelf)");
	site2Link.put("Boer War","Boer_War_(Bookshelf)");
	site2Link.put("English Civil War","English_Civil_War_(Bookshelf)");
	site2Link.put("Spanish American War","Spanish_American_War_(Bookshelf)");
	site2Link.put("US Civil War","US_Civil_War_(Bookshelf)");
	site2Link.put("World War I","World_War_I_(Bookshelf)");
	site2Link.put("World War II","World_War_II_(Bookshelf)");
	
	site2Link.put("Ainslee's","Ainslee%27s_(Bookshelf)");
	site2Link.put("The Aldine","The_Aldine_(Bookshelf)");
	site2Link.put("The American Architect and Building News","The_American_Architect_and_Building_News_(Bookshelf)");
	site2Link.put("The American Journal of Archaeology","The_American_Journal_of_Archaeology_(Bookshelf)");
	site2Link.put("The American Missionary","The_American_Missionary_(Bookshelf)");
	site2Link.put("The American Quarterly Review","The_American_Quarterly_Review_(Bookshelf)");
	site2Link.put("The Arena","The_Arena_(Bookshelf)");
	site2Link.put("The Argosy","The_Argosy_(Bookshelf)");
	site2Link.put("Armour's Monthly Cook Book","Armour%27s_Monthly_Cook_Book_(Bookshelf)");
	site2Link.put("Astounding Stories","Astounding_Stories_(Bookshelf)");
	site2Link.put("The Atlantic Monthly","The_Atlantic_Monthly_(Bookshelf)");
	site2Link.put("The Baptist Magazine","The_Baptist_Magazine_(Bookshelf)");
	site2Link.put("Barnavännen","Barnavännen_(Bookshelf)");
	site2Link.put("The Bay State Monthly","The_Bay_State_Monthly_(Bookshelf)");
	site2Link.put("Bird-Lore","Bird-Lore_(Bookshelf)");
	site2Link.put("Birds, Illustrated by Color Photography","Birds,_Illustrated_by_Color_Photography_(Bookshelf)");
	site2Link.put("Blackwood's Edinburgh Magazine","Blackwood%27s_Edinburgh_Magazine_(Bookshelf)");
	site2Link.put("The Botanical Magazine","The_Botanical_Magazine_(Bookshelf)");
	site2Link.put("The Brochure Series of Architectural Illustration","The_Brochure_Series_of_Architectural_Illustration_(Bookshelf)");
	site2Link.put("Buchanan's Journal of Man","Buchanan%27s_Journal_of_Man_(Bookshelf)");
	site2Link.put("Bulletin of Lille","Bulletin_de_Lille_(Bookshelf)");
	site2Link.put("The Catholic World","The_Catholic_World_(Bookshelf)");
	site2Link.put("Celtic Magazine","Celtic_Magazine_(Bookshelf)");
	site2Link.put("Chambers's Edinburgh Journal","Chambers%27s_Edinburgh_Journal_(Bookshelf)");
	site2Link.put("The Christian Foundation","The_Christian_Foundation_(Bookshelf)");
	site2Link.put("The Church of England Magazine","The_Church_of_England_Magazine_(Bookshelf)");
	site2Link.put("The Contemporary Review","The_Contemporary_Review_(Bookshelf)");
	site2Link.put("Continental Monthly","Continental_Monthly_(Bookshelf)");
	site2Link.put("Current History","Current_History_(Bookshelf)");
	site2Link.put("De Aarde en haar Volken","De_Aarde_en_haar_Volken_(Bookshelf)");
	site2Link.put("Donahoe's Magazine","Donahoe%27s_Magazine_(Bookshelf)");
	site2Link.put("The Economist","The_Economist_(Bookshelf)");
	site2Link.put("The Esperantist","The_Esperantist_(Bookshelf)");
	site2Link.put("The Galaxy","The_Galaxy_(Bookshelf)");
	site2Link.put("Garden and Forest","Garden_and_Forest_(Bookshelf)");
	site2Link.put("Godey's Lady's Book","Godey%27s_Lady%27s_Book_(Bookshelf)");
	site2Link.put("Graham's Magazine","Graham%27s_Magazine_(Bookshelf)");
	site2Link.put("Harper's New Monthly Magazine","Harper%27s_New_Monthly_Magazine_(Bookshelf)");
	site2Link.put("Harper's Young People","Harper%27s_Young_People_(Bookshelf)");
	site2Link.put("The Idler","The_Idler_(Bookshelf)");
	site2Link.put("The Illustrated War News","The_Illustrated_War_News_(Bookshelf)");
	site2Link.put("The International Magazine of Literature, Art, and Science","The_International_Magazine_of_Literature,_Art,_and_Science_(Bookshelf)");
	site2Link.put("The Irish Ecclesiastical Record","The_Irish_Ecclesiastical_Record_(Bookshelf)");
	site2Link.put("The Irish Penny Journal","The_Irish_Penny_Journal_(Bookshelf)");
	site2Link.put("Journal of Entomology and Zoology","Journal_of_Entomology_and_Zoology_(Bookshelf)");
	site2Link.put("The Journal of Negro History","The_Journal_of_Negro_History_(Bookshelf)");
	site2Link.put("The Knickerbocker","The_Knickerbocker_(Bookshelf)");
	site2Link.put("L'Illustration","L%27Illustration_(Bookshelf)");
	site2Link.put("Lippincott's Magazine","Lippincott%27s_Magazine_(Bookshelf)");
	site2Link.put("Little Folks","Little_Folks_(Bookshelf)");
	site2Link.put("London Medical Gazette","London_Medical_Gazette");
	site2Link.put("The Mayflower","The_Mayflower_(Bookshelf)");
	site2Link.put("McClure's Magazine","McClure%27s_Magazine_(Bookshelf)");
	site2Link.put("The Menorah Journal","The_Menorah_Journal_(Bookshelf)");
	site2Link.put("The Mentor","The_Mentor_(Bookshelf)");
	site2Link.put("The Mirror of Literature, Amusement, and Instruction","The_Mirror_of_Literature,_Amusement,_and_Instruction_(Bookshelf)");
	site2Link.put("The Mirror of Taste, and Dramatic Censor","The_Mirror_of_Taste,_and_Dramatic_Censor_(Bookshelf)");
	site2Link.put("Mother Earth","Mother_Earth_(Bookshelf)");
	site2Link.put("Mrs Whittelsey's Magazine for Mothers and Daughters","Mrs_Whittelsey%27s_Magazine_for_Mothers_and_Daughters_(Bookshelf)");
	site2Link.put("The National Preacher","The_National_Preacher_(Bookshelf)");
	site2Link.put("The North American Medical and Surgical Journal","The_North_American_Medical_and_Surgical_Journal_(Bookshelf)");
	site2Link.put("Northern Nut Growers Association","Northern_Nut_Growers_Association_(Bookshelf)");
	site2Link.put("Notes and Queries","Notes_and_Queries_(Bookshelf)");
	site2Link.put("Our Young Folks","Our_Young_Folks_(Bookshelf)");
	site2Link.put("Poetry, A Magazine of Verse","Poetry,_A_Magazine_of_Verse_(Bookshelf)");
	site2Link.put("Popular Science Monthly","Popular_Science_Monthly_(Bookshelf)");
	site2Link.put("Prairie Farmer","Prairie_Farmer_(Bookshelf)");
	site2Link.put("Punch","Punch_(Bookshelf)");
	site2Link.put("Punchinello","Punchinello_(Bookshelf)");
	site2Link.put("Scientific American","Scientific_American_(Bookshelf)");
	site2Link.put("The Scrap Book","The_Scrap_Book_(Bookshelf)");
	site2Link.put("Scribner's Magazine","Scribner%27s_Magazine_(Bookshelf)");
	site2Link.put("The Speaker","The_Speaker_(Bookshelf)");
	site2Link.put("The Stars and Stripes","The_Stars_and_Stripes_(Bookshelf)");
	site2Link.put("The Strand Magazine","The_Strand_Magazine_(Bookshelf)");
	site2Link.put("The Haslemere Museum Gazette","The_Haslemere_Museum_Gazette_(Bookshelf)");
	site2Link.put("The Unpopular Review","The_Unpopular_Review_(Bookshelf)");
	site2Link.put("The Writer","The_Writer_(Bookshelf)");
	site2Link.put("The Yellow Book","The_Yellow_Book_(Bookshelf)");
	
	
	}
}
