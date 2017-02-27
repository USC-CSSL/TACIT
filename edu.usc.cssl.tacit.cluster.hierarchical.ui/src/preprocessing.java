import java.io.*;
import java.util.*;
import java.util.regex.*;

class Spelling {

	private final HashMap<String, Integer> nWords = new HashMap<String, Integer>();

	public Spelling(String file) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(new File(file)));
		Pattern p = Pattern.compile("\\w+");
		for(String temp = ""; temp != null; temp = in.readLine()){
			Matcher m = p.matcher(temp.toLowerCase());
			while(m.find()) nWords.put((temp = m.group()), nWords.containsKey(temp) ? nWords.get(temp) + 1 : 1);
		}
		in.close();
	}

	private final ArrayList<String> edits(String word) {
		ArrayList<String> result = new ArrayList<String>();
		for(int i=0; i < word.length(); ++i) result.add(word.substring(0, i) + word.substring(i+1));
		for(int i=0; i < word.length()-1; ++i) result.add(word.substring(0, i) + word.substring(i+1, i+2) + word.substring(i, i+1) + word.substring(i+2));
		for(int i=0; i < word.length(); ++i) for(char c='a'; c <= 'z'; ++c) result.add(word.substring(0, i) + String.valueOf(c) + word.substring(i+1));
		for(int i=0; i <= word.length(); ++i) for(char c='a'; c <= 'z'; ++c) result.add(word.substring(0, i) + String.valueOf(c) + word.substring(i));
		return result;
	}

	public final String correct(String word) {
		if(nWords.containsKey(word)) return word;
		ArrayList<String> list = edits(word);
		HashMap<Integer, String> candidates = new HashMap<Integer, String>();
		for(String s : list) if(nWords.containsKey(s)) candidates.put(nWords.get(s),s);
		if(candidates.size() > 0) return candidates.get(Collections.max(candidates.keySet()));
		for(String s : list) for(String w : edits(s)) if(nWords.containsKey(w)) candidates.put(nWords.get(w),w);
		return candidates.size() > 0 ? candidates.get(Collections.max(candidates.keySet())) : word;
	}

	public static void main(String args[]) throws IOException {
		String a = "homs to do are yoe";
		
		String[] arr = a.split("[^a-zA-Z]+");
		
		
		for(int i = 0; i < arr.length; i += 1)
		{
		     //System.out.println(arr[i]);
		     String b = arr[i];
		     
		     
		System.out.println((new Spelling("C:\\Users\\Reshma\\workspace\\Preprocessing\\src\\big1.txt")).correct(b));
		String c = new Spelling("C:\\Users\\Reshma\\workspace\\Preprocessing\\src\\big1.txt").correct(b);
		c=Arrays.toString(arr).replaceAll(",", "");
		//System.out.println(c);
		}
		
	}

}