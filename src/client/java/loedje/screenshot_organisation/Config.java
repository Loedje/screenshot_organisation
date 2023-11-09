package loedje.screenshot_organisation;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class Config {
	private File configFile;
	private final Map<String, String> rules = new LinkedHashMap<>();

	public Map<String, String> getRules() {
		return rules;
	}

	protected void readConfig() {
		rules.clear();
		configFile = (FabricLoader.getInstance().getConfigDir().resolve("screenshot_organisation.txt").toFile());
		try {
			if (configFile.createNewFile())
				return; //If a new file is created the file is not read
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] rule = line.split("=");
				if (rule.length == 2) {
					rules.put(rule[0].trim(), rule[1].trim());
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected void removeRule(String removedSource) {
		rules.remove(removedSource);
		try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
			StringBuilder stringBuilder = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				if (!line.trim().startsWith(removedSource)) {
					stringBuilder.append(line);
					stringBuilder.append('\n');
				}
			}
			FileOutputStream fileOut = new FileOutputStream(configFile);
			fileOut.write(stringBuilder.toString().getBytes());
			fileOut.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected void addRuleToConfig(String source, String destination) {
		rules.put(source, destination);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(configFile, true))) {
			bw.newLine();
			bw.write(source + "=" + destination);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}