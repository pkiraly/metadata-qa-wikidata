package de.gwdg.metadataqa.wikidata.json;

import com.jayway.jsonpath.JsonPath;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Reader {
	private static final Pattern ENTITY_ID_PATTERN = Pattern.compile("^Q\\d+$");
	private static final String WD_CMD_PATTERN = "wd data --props labels '%s'";
	private static final String API_URL_PATTERN =
		"https://www.wikidata.org/wiki/Special:EntityData/%s.json";
	public static final Charset CHARSET = Charset.forName("UTF-8");

	private enum TYPE {
		PROPERTIES,
		ENTITIES;
	}

	private static int recordCounter = 0;
	private static Map<String, Integer> container = new HashMap<>();
	private static JSONParser parser = new JSONParser();
	private Map<String, WikidataProperty> properties = new HashMap<>();
	private Map<String, String> entities = new HashMap<>();
	private static List<String> printableTypes = Arrays.asList("String", "Long", "Double");
	private SparqlClientJena sparqlClient = new SparqlClientJena();

	public Reader() {}

	public Reader(String propertiesFile, String entitiesFile) {
		readCsv(propertiesFile, TYPE.PROPERTIES);
		System.err.println("properties: " + properties.size());
		readCsv(entitiesFile, TYPE.ENTITIES);
		System.err.println("entities: " + entities.size());
	}

	public void read(String jsonString) {
		recordCounter++;

		if (recordCounter % 50000 == 0)
			System.err.println(recordCounter);
		try {
			Object obj = parser.parse(jsonString);
			JSONObject jsonObject = (JSONObject) obj;
			process(new ArrayList<>(), jsonObject);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private void process(List<String> path, JSONObject jsonObject) {
		for (Object keyObj : jsonObject.keySet()) {
			String key = (String) keyObj;
			Object value = jsonObject.get(keyObj);
			String type = value.getClass().getSimpleName();
			if (path.contains("claims") && printableTypes.contains(type)) {
				System.err.printf("O: %s/%s: %s\n", resolvePath(path), key, value.toString());
			}

			if (type.equals("String")) {
				addContainer(path, key, "String");
			} else if (type.equals("Long")) {
				addContainer(path, key, "Long");
			} else if (type.equals("Double")) {
				addContainer(path, key, "Double");
			} else if (type.equals("JSONArray")) {
				process(buildPath(path, key), (JSONArray)value);
			} else if (type.equals("JSONObject")) {
				process(buildPath(path, key), (JSONObject)value);
			} else {
				System.err.println("unknown type [in object]: " + type);
			}
		}
	}

	private String resolveProperty(String propertyId) {
		return propertyId;
	}

	private void process(List<String> path, JSONArray jsonArray) {
		Iterator it = jsonArray.iterator();
		int i = 0;
		if (it.hasNext()) {
			Object item = it.next();
			String type = item.getClass().getSimpleName();
			if (path.contains("claims") && printableTypes.contains(type)) {
				System.err.printf("A: %s/%s: %s\n", resolvePath(path), i, resolveValue(item));
			}

			if (type.equals("String")) {
				addContainer(path, "0", "String");
			} else if (type.equals("Long")) {
				addContainer(path, "0", "Long");
			} else if (type.equals("Double")) {
				addContainer(path, "0", "Double");
			} else if (type.equals("JSONArray")) {
				process(buildPath(path, String.valueOf(i)), (JSONArray)item);
			} else if (type.equals("JSONObject")) {
				process(buildPath(path, String.valueOf(i)), (JSONObject)item);
			} else {
				System.err.println("unknown type [array]: " + type);
			}
			i++;
		}
	}

	private String resolveValue(Object value) {
		if (value instanceof String) {
			String entityId = (String)value;
			if (ENTITY_ID_PATTERN.matcher(entityId).matches()) {
				if (!entities.containsKey(entityId)) {
					String label = sparqlClient.getLabel(entityId);
					// String label = readWd(entityId);
					entities.put(entityId, label);
				}
				return entities.get(entityId);
			}
		}
		return value.toString();
	}

	private static List<String> buildPath(List<String> path, String key) {
		List<String> path2 = new ArrayList<>(path);
		path2.add(key);
		return path2;
		// return path.equals("/") ? "/" + key : String.format("%s/%s", path, key);
	}

	private static void addContainer(List<String> path, String key, String type) {
		key = StringUtils.join("/", path) + "/" + key;
		String typedKey = String.format("%s (%s)", key, type);
		if (!container.containsKey(typedKey)) {
			container.put(typedKey, 0);
		}
		container.put(typedKey, container.get(typedKey) + 1);
	}

	public static int getRecordCounter() {
		return recordCounter;
	}

	public static Map<String, Integer> getContainer() {
		return container;
	}

	private void readCsv(String csvFile, TYPE type) {
		CSVReader reader = null;
		int i = 0;
		try {
			reader = new CSVReader(new FileReader(csvFile));
			String[] line;
			while ((line = reader.readNext()) != null) {
				if (type.equals(TYPE.PROPERTIES)) {
					WikidataProperty property = new WikidataProperty(line[0], line[1], line[3]);
					properties.put(property.getId(), property);
				} else if (type.equals(TYPE.ENTITIES)) {
					System.err.println(line[0]);
					entities.put(line[0], line[1]);
				}
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println(type.name() + " " + i);
	}

	private String resolvePath(List<String> path) {
		String propertyId = path.get(path.size() - 1);
		String label;
		if (propertyId.startsWith("P")) {
			if (properties.containsKey(propertyId)) {
				label = properties.get(propertyId).getLabel();
			} else {
				// resolve property
				label = resolveProperty(propertyId);
			}
			return label;
		}
		return propertyId;
	}

	// alternative way: using Wikidata API
	// https://www.wikidata.org/wiki/Special:EntityData/Q133704.json
	// https://www.mediawiki.org/wiki/Wikibase/EntityData
	private String readApi(String entityId) {
		String label = entityId;
		String url = String.format(API_URL_PATTERN, entityId);
		try {
			String json = readJsonFromUrl(url);
			label = extractEntityLabel(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return label;
	}

	private String readWd(String entityId) {
		String label = entityId;
		try {
			Runtime runtime = Runtime.getRuntime();
			String cmd = String.format(WD_CMD_PATTERN, entityId);
			Process process = runtime.exec(cmd);
			process.waitFor();
			String json = extractJsonFromWd(process.getInputStream());
			label = extractEntityLabel(json);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return label;
	}

	public static String readJsonFromUrl(String url) throws IOException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(
				new InputStreamReader(is, CHARSET));
			return readAll(rd);
		} finally {
			is.close();
		}
	}

	private static String readAll(java.io.Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	private String extractJsonFromWd(InputStream inputStream) {
		BufferedReader bufferedReader = new BufferedReader(
			new InputStreamReader(inputStream));
		StringBuffer json = new StringBuffer();

		while (true) {
			String line = null;
			try {
				if (!((line = bufferedReader.readLine()) != null)) break;
			} catch (IOException e) {
				e.printStackTrace();
			}
			json.append(line);
		}
		try {
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return json.toString();
	}

	private String extractEntityLabel(String json) {
		return JsonPath.read(json, "$.labels.en.value");
		//return labels.get(0);
	}

	public void saveEntities(String entitiesFile) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(entitiesFile);
			//using custom delimiter and quote character
			CSVWriter csvWriter = new CSVWriter(writer);

			List<String[]> data = toStringArray();

			csvWriter.writeAll(data);
			csvWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<String[]> toStringArray() {
		List<String[]> records = new ArrayList<String[]>();

		// adding header record
		records.add(new String[]{"id", "label"});

		for (Map.Entry<String, String> entry : entities.entrySet()) {
			records.add(new String[]{entry.getKey(), entry.getValue()});
		}

		return records;
	}
}
