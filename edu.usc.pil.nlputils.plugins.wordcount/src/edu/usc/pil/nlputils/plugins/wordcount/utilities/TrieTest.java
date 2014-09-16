package edu.usc.pil.nlputils.plugins.wordcount.utilities;

import java.awt.List;
import java.util.ArrayList;

public class TrieTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
       Trie newTrie = new Trie();
       ArrayList<Integer> cats = new ArrayList<Integer>();
       cats.add(1);
       newTrie.insert("neighbor*",cats);
       System.out.println(newTrie.query("neighbor-the"));
       System.out.println(newTrie.checkHyphen("neighbor-the"));
       newTrie.insert("neighbor-the*",cats);
       System.out.println(newTrie.checkHyphen("neighbor-the"));
       System.out.println(newTrie.checkHyphen("neighbor-the*"));
       newTrie.insert("self*", cats);
       newTrie.insert("self-conscious*", cats);
       System.out.println(newTrie.checkHyphen("self-conscious"));
       System.out.println(newTrie.checkHyphen("self-conscious*"));
       
       
	}

}
