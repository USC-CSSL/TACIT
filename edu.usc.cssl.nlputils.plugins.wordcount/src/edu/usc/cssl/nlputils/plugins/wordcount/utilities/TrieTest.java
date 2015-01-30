package edu.usc.cssl.nlputils.plugins.wordcount.utilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.List;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class TrieTest {

	public Trie testTrie;
	public ArrayList<Integer> categories;
	
	@Before
	public void setUp(){
		testTrie = new Trie();
		categories = new ArrayList<Integer>();
		categories.add(1);
	}
	
	@Test
	public void testReverseOrderInsert(){
	       testTrie.insert("arena", categories);
	       testTrie.insert("are", categories);
	       testTrie.insert("a", categories);
	       assertNotNull(testTrie.query("arena"));
	       assertNotNull(testTrie.query("are"));
	       assertNotNull(testTrie.query("a"));
	       assertNull(testTrie.query("ar"));
	}
	
	@Test
	public void testRoot(){
			testTrie.insert("crazy", categories);
			assertNull(testTrie.root("crazyness"));
			assertNull(testTrie.root("kkrazy"));
			assertNull(testTrie.root("craz"));
			assertEquals("crazy",testTrie.root("crazy"));
	       
	       testTrie.insert("neighbor*",categories);
	       assertNotNull(testTrie.query("neighbor-the"));
	       assertEquals("neighbor*",testTrie.root("neighbor"));
	       assertEquals("neighbor*",testTrie.root("neighbor-the"));
	       
	       testTrie.insert("self-conscious*", categories);
	       assertEquals("self-conscious*",testTrie.root("self-conscious"));
	       assertEquals("self-conscious*",testTrie.root("self-conscious*"));
	}
	
	@Test
	public void testCheckHyphen(){
		testTrie.insert("neighbor*", categories);
		assertFalse(testTrie.checkHyphen("neighbor-the"));
	       testTrie.insert("neighbor-the*",categories);
	       assertTrue(testTrie.checkHyphen("neighbor-the"));
	       assertTrue(testTrie.checkHyphen("neighbor-the*"));
	       testTrie.insert("self*", categories);
	       testTrie.insert("self-conscious*", categories);
	       assertTrue(testTrie.checkHyphen("self-conscious"));
	       assertTrue(testTrie.checkHyphen("self-conscious*"));
	}
	
	public static void main(String[] args) {
		/*
       Trie newTrie = new Trie();
       ArrayList<Integer> cats = new ArrayList<Integer>();
       cats.add(1);
       newTrie.insert("arena", cats);
       newTrie.insert("are", cats);
       newTrie.insert("a", cats);
       newTrie.printTrie();
       System.out.println(newTrie.query("a"));
       System.out.println(newTrie.query("ar"));
       System.out.println(newTrie.query("are"));
       System.out.println(newTrie.query("arena"));
       newTrie.insert("crazy", cats);
       System.out.println(newTrie.root("crazyness"));
       System.out.println(newTrie.root("kkrazy"));
       System.out.println(newTrie.root("crazy"));
       System.out.println(newTrie.root("craz"));
       newTrie.insert("neighbor*",cats);
       System.out.println(newTrie.query("neighbor-the"));
       System.out.println(newTrie.root("neighbor"));
       System.out.println(newTrie.root("neighbor-the"));
       System.out.println(newTrie.checkHyphen("neighbor-the"));
       newTrie.insert("neighbor-the*",cats);
       System.out.println(newTrie.checkHyphen("neighbor-the"));
       System.out.println(newTrie.checkHyphen("neighbor-the*"));
       newTrie.insert("self*", cats);
       newTrie.insert("self-conscious*", cats);
       System.out.println(newTrie.checkHyphen("self-conscious"));
       System.out.println(newTrie.root("self-conscious"));
       System.out.println(newTrie.checkHyphen("self-conscious*"));
       System.out.println(newTrie.root("self-conscious*"));
       */
       
	}

}
