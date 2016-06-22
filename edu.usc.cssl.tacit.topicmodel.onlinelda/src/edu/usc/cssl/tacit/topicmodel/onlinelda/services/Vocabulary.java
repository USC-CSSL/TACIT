package edu.usc.cssl.tacit.topicmodel.onlinelda.services;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

//Vocabulary class gives Dictionary mapping from words to integer ids.

public class Vocabulary {
	
	final List<String> words = new ArrayList<String>();
	
	public Vocabulary(String path) throws IOException {
        Scanner scanner = new Scanner(new File(path));
        while (scanner.hasNextLine()){
            words.add(scanner.nextLine().trim().toLowerCase());
        }
        scanner.close();
    }
	
	public String getToken(int i){
		return words.get(i);
	}
	
	public int getId(String s){
		int id = -100;
		for(int i=0; i< words.size();++i){
            if(words.get(i).equals(s)){
                id = i;
                break;
            }
        }
		return id;
	}
}
