package edu.usc.cssl.tacit.common.queryprocess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

class Attribute {
	String key = null;
	QueryDataType dataType = null;

	public Attribute(String key, QueryDataType dataType) {
		this.key = key;
		this.dataType = dataType;
	}

	public Attribute() {
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public QueryDataType getDataType() {
		return dataType;
	}

	public void setDataType(QueryDataType d) {
		this.dataType = d;
	}
}

public class JsonParser {
	final int keyLevel = 3;

	public JsonParser(String[] keyList) {
		for (int i = 0; i < keyList.length; i++) {
			keyList[i] = keyList[i].trim();
		}
		keyList.clone();
	}

	public JsonParser() {
	}

	public Set<Attribute> findJsonStructure(String filePath)
			throws FileNotFoundException {
		Set<Attribute> resultAttr = new HashSet<Attribute>();
		if (!new File(filePath).isDirectory()) {
			throw new FileNotFoundException(filePath
					+ " is not Directory and cannot be parsed");
		}
		String[] files = new File(filePath).list();
		for (String jsonFileName : files) {
			if (!jsonFileName.endsWith(".json")) {
				continue;
			}
			try {
				getKeysFromJson(filePath + File.separatorChar + jsonFileName,
						resultAttr);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
			} catch (JsonIOException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return resultAttr;
	}

	private void getKeysFromJson(String fileName, Set<Attribute> resultAttr)
			throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		Object things = new Gson().fromJson(new FileReader(fileName),
				Object.class);
		collectAllTheKeys(things, resultAttr, null, keyLevel);
	}

	public static List<String> getParentKeys(String jsonFileName)
			throws JsonSyntaxException, JsonIOException, FileNotFoundException {

		List<String> parentKeys = new ArrayList<String>();

		Object jsonData = new Gson().fromJson(new FileReader(jsonFileName),
				Object.class);
		if (jsonData instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) jsonData;
			for (Object key : map.keySet()) {
				parentKeys.add(key.toString());
			}
		} else if (jsonData instanceof Collection) {
			for (Object key : ((Collection<?>) jsonData)) {
				parentKeys.add(key.toString());
			}
		} else {
			// TODO: ??
		}

		return parentKeys;
	}

	private void collectAllTheKeys(Object o, Set<Attribute> resultAttr,
			String parent, int keyLevel) {
		if (keyLevel < 0)
			return; // only accept keys within key level

		if (o instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) o;
			for (Object key : map.keySet()) {
				if (!(map.get(key) instanceof Map)
						&& !(map.get(key) instanceof Collection)) {
					Attribute attr = new Attribute();
					setDataType(attr, map.get(key));

					if (null != parent)
						attr.setKey(parent + "." + key.toString());
					else
						attr.setKey(key.toString());
					resultAttr.add(attr);
				} else if (map.get(key) instanceof Map) {
					if (null == parent)
						collectAllTheKeys(map.get(key), resultAttr,
								key.toString(), keyLevel - 1);
					else
						collectAllTheKeys(map.get(key), resultAttr, parent
								+ "." + key.toString(), keyLevel - 1);
				} else if (map.get(key) instanceof Collection) {
					if (null == parent)
						collectAllTheKeys(map.get(key), resultAttr,
								key.toString(), keyLevel - 1);
					else
						collectAllTheKeys(map.get(key), resultAttr, parent
								+ "." + key.toString(), keyLevel - 1);
				}
			}
		} else if (o instanceof Collection) {
			for (Object key : (Collection<?>) o) {
				if (!(key instanceof Map) && !(key instanceof Collection)) {
					Attribute attr = new Attribute();
					setDataType(attr, key);
					if (null != parent)
						attr.setKey(parent + "." + key.toString());
					else
						attr.setKey(key.toString());
					resultAttr.add(attr);
				} else if (key instanceof Map) {
					collectAllTheKeys(key, resultAttr, parent, keyLevel - 1);
				} else if (key instanceof Collection) {
					if (null == parent)
						collectAllTheKeys(key, resultAttr, key.toString(),
								keyLevel - 1);
					else
						collectAllTheKeys(key, resultAttr,
								parent + "." + key.toString(), keyLevel - 1);
				}
				break;
			}
		} else {
			// TODO: ??
		}
	}

	private void setDataType(Attribute attr, Object key) {
		if (key instanceof Double)
			attr.setDataType(QueryDataType.DOUBLE);
		else if (key instanceof String)
			attr.setDataType(QueryDataType.STRING);
		else if (key instanceof Integer)
			attr.setDataType(QueryDataType.INTEGER);
		else
			attr.setDataType(QueryDataType.STRING); // TODO : as of now, for
													// default cases
	}

	public static void main(String[] args) {
//		JsonParser jh = new JsonParser();
//		// HashMap<String, String> jsonKeys =
//		// jh.findJsonStructure("C:\\Program Files (x86)\\eclipse\\json_corpuses\\reddit\\REDDIT_1443138695389\\Dummy\\test.json");
//		Set<Attribute> jsonKeys = jh
//				.findJsonStructure("C:\\Program Files (x86)\\eclipse\\json_corpuses\\reddit\\REDDIT_1443138695389\\Dummy\\long.json");
//		for (Attribute attr : jsonKeys) {
//			System.out.println(attr.key + "->" + attr.dataType);
//		}
	}
}
