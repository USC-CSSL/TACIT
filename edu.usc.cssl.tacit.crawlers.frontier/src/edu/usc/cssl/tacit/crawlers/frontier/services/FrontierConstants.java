package edu.usc.cssl.tacit.crawlers.frontier.services;

import java.util.HashMap;

public class FrontierConstants {
	public static HashMap<String, String> site2Link = new HashMap<String, String>();
	public static HashMap<Integer, String[]> sites = new HashMap<Integer, String[]>();
	static String science[] = new String[]{"Applied Mathematics and Statistics",
			"Astronomy and Space Sciences",
			"Cell and Developmental Biology",
			"Chemistry",
			"Earth Science",
			"Ecology and Evolution",
			"Environmental Science",
			"Genetics",
			"Marine Science",
			"Microbiology",
			"Molecular Biosciences",
			"Neuroscience",
			"Pharmacology",
			"Physics",
			"Physiology",
			"Plant Science",
			"Psychology"};
	static String health[] = new String[]{"Cardiovascular Medicine",
			"Endocrinology",
			"Immunology",
			"Medicine",
			"Neurology",
			"Nutrition",
			"Oncology",
			"Pediatric",
			"Psychiatry",
			"Public Health",
			"Surgery",
			"Veterinary Science"
	};
	static String eng[] =new String[]{
			"Bioengineering and Biotechnology",
			"Built Environment",
			"Energy Research",
			"ICT",
			"Materials",
			"Mechanical Engineering",
			"Robotics and AI"
	};
	static String hsc[] = new String[]{
			"Communication",
			"Digital Humanities",
			"Sociology"
	};
	
	static{
	sites.put(0, science);
	sites.put(1, health);
	sites.put(2, eng);
	sites.put(3, hsc);
	site2Link.put("Applied Mathematics and Statistics", "fams");
	site2Link.put("Astronomy and Space Sciences", "fspas");
	site2Link.put("Cell and Developmental Biology", "fcell");
	site2Link.put("Chemistry","fchem");
	site2Link.put("Earth Science","feart");
	site2Link.put("Ecology and Evolution","fevo");
	site2Link.put("Environmental Science","fenvs");
	site2Link.put("Genetics","fgene");
	site2Link.put("Marine Science","fmars");
	site2Link.put("Microbiology","fmicb");
	site2Link.put("Molecular Biosciences","fmolb");
	site2Link.put("Neuroscience","fnins");
	site2Link.put("Pharmacology","fphar");
	site2Link.put("Physics","fphy");
	site2Link.put("Physiology","fphys");
	site2Link.put("Plant Science","fpls");
	site2Link.put("Psychology","fpsyg");
	site2Link.put("Cardiovascular Medicine","fcvm");
	site2Link.put("Endocrinology","fendo");
	site2Link.put("Immunology","fimmu");
	site2Link.put("Medicine","fmed");
	site2Link.put("Neurology","fneur");
	site2Link.put("Nutrition","fnut");
	site2Link.put("Oncology","fonc");
	site2Link.put("Pediatrics","fped");
	site2Link.put("Psychiatry","fpsyt");
	site2Link.put("Public Health","fpubh");
	site2Link.put("Surgery","fsurg");
	site2Link.put("Veterinary Science","fvets");
	site2Link.put("Bioengineering and Biotechnology","fbioe");
	site2Link.put("Built Environment","fbuil");
	site2Link.put("Energy Research","fenrg");
	site2Link.put("ICT","fict");
	site2Link.put("Materials","fmats");
	site2Link.put("Mechanical Engineering","fmech");
	site2Link.put("Robotics and AI","frobt");
	site2Link.put("Communication","fcomm");
	site2Link.put("Digital Humanities","fdigh");
	site2Link.put("Sociology","fsoc");
	}
}
