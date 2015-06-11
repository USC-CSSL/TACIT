/**
 * @author Aswin Rajkumar <aswin.rajkumar@usc.edu>
 */
package edu.uc.cssl.nlputils.wordcount.weighted.services;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import edu.usc.cssl.nlputils.common.ui.views.ConsoleView;

public class Trie {

	private class Node {
		Map<Character, Node> children;
		boolean isWord;
		List<Integer> categories;
		char character;
		
		public Node(char c, List<Integer> categories, boolean isWord){
			this.character = c;
			this.isWord = isWord;
			this.categories = categories;
			this.children = new HashMap<Character, Node>();
		}
		
		public String toString(){
			return Character.toString(character);		
		}
	}
	
	Node root;
	
	public Trie(){
		root = new Node(' ', null, false); /* Initialize root node with space character and no categories*/
	}
	
	public void insert(String word, List<Integer> categories){
		Node next = root.children.get(word.charAt(0)), prev = root;
		int i=0;
		
		while(next != null && i != word.length()-1){
			//ConsoleView.writeInConsole(i+"/"+word.length() +" "+ word.charAt(i));
			prev = next;
			next = next.children.get(word.charAt(i+1));
			//ConsoleView.writeInConsole("Prev - " + prev.character);
			//if (next!=null)
			//ConsoleView.writeInConsole("Next - " + next.character);
			i++;
		}
		
		/* If I reached end of the trie without reaching the end of the string, add the new nodes corresponding to this new string */
		//if((i!=word.length()) || i==0){
		if(next==null){
			Node n;
			while( i != word.length()){
				n = new Node(word.charAt(i), null, false);
				prev.children.put(word.charAt(i), n);
				//ConsoleView.writeInConsole("Inserted - " + n.character);
				prev = n;
				i++;
			}
		} else {
			// The string is in the trie. mark the current node's isWord to true to mark it as a valid word.
			//ConsoleView.writeInConsole("Marking "+next.character+" as valid.");
			//next.isWord = true; - instead, just set prev = next as the remaining instructions outside the condition do the rest.
			prev = next;
		}
		
		prev.categories = categories;
		prev.isWord = true;
	}
	
	public void printTrie(){
		
		Stack<Node> s = new Stack<Node>();
		Node n;
		Iterator it;
		Map.Entry pair;
		
		s.push(root);
		/* Do a DFS on the tree */
		while(!s.empty()){
			n = s.pop();
			System.out.print(n.character);
			it = n.children.entrySet().iterator();
			while(it.hasNext()){
				pair = (Map.Entry)it.next();
				s.push((Node)pair.getValue());
			}
		}
	}
	
	public List<Integer> query(String word){
		
		Node prev = root, next = root.children.get(word.charAt(0));
		
		int i=1;
		while( next != null && i != word.length()){
			prev = next;
			next = next.children.get(word.charAt(i));
			i++;
		}
		
		if(i == word.length() && next != null){
			prev = next;
		}
	
		if(prev.children.get('*') != null){
			return prev.children.get('*').categories;
		}
		
		if (i == word.length() && next == null & prev.children.get("*")==null){
			return null;
		}
		
		if(i == word.length()){
			if(prev.isWord){
				return prev.categories;
			}
		}		
		
		/* If I came out alive, it means I didn't find the word - so return a null */
		return null;
		
	}
	
public String root(String word){
		StringBuilder sb = new StringBuilder();
		Node prev = root, next = root.children.get(word.charAt(0));
		
		int i=1;
		while( next != null && i != word.length()){
			prev = next;
			next = next.children.get(word.charAt(i));
			sb.append(prev.character);
			i++;
		}
		
		if(i == word.length() && next != null){
			prev = next;
			sb.append(prev.character);
		}
		
//		if ((i == word.length()) && (prev.children.get('*') != null)){
//			sb.append(prev.character);
//			return sb.toString();
//		}
		
		if(prev.children.get('*') != null){
			sb.append("*");
			return sb.toString();
		}
		
		if (i == word.length() && next == null & prev.children.get("*")==null){
			return null;
		}
		
		if(i == word.length()){
			if(prev.isWord){
				return sb.toString();
			}
		}		
		
		/* If I came out alive, it means I didn't find the word - so return a null */
		return null;
		
	}
	
public boolean checkHyphen(String word){
		boolean hyphen = false;
		Node prev = root, next = root.children.get(word.charAt(0));
		
		int i=1;
		while( next != null && i != word.length()){
			prev = next;
			next = next.children.get(word.charAt(i));
			if (next!=null)
				if (next.character == '-')
				hyphen = true;
			i++;
		}
		
		if(i == word.length() && next != null){
			prev = next;
		}
	
		if(prev.children.get('*') != null){
			// return prev.children.get('*').categories;
			if (hyphen)
				return true;
			else 
				return false;
		}
		
		if (i == word.length() && next == null & prev.children.get("*")==null){
			// return null;
			return false;
		}
		
		if(i == word.length()){
			if(prev.isWord){
				//return prev.categories;
				if (hyphen)
					return true;
				else 
					return false;
			}
		}		
		
		/* If I came out alive, it means I didn't find the word - so return a null */
		return false;
		
	}
	
}
