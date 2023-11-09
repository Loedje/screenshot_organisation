package loedje.screenshot_organisation;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ScreenshotOrganisationConfig {
	private static File config;
	public static final Map<String, String> rules = new HashMap<>();

	public static void readConfig(ConfigScreen.ScreenshotFolderListWidget listWidget) {
		rules.clear();
		config = (FabricLoader.getInstance().getConfigDir().resolve("screenshot_organisation.txt").toFile());
		try {
			if (config.createNewFile())
				return; //If a new file is created the file is not read
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		try (BufferedReader br = new BufferedReader(new FileReader(config))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] rule = line.split("=");
				if (rule.length == 2) {
					rules.put(rule[0].trim(), rule[1].trim());
					if (listWidget != null)	listWidget.addRule(rule[0].trim(), rule[1].trim());
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected void removeRuleInConfig(String removed) {
		try (BufferedReader br = new BufferedReader(new FileReader(config))) {
			StringBuilder StringBuilder = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				String[] rule = line.split("=");
				if (rule.length == 2 && !(rule[0].trim() + "=" + rule[1].trim()).equals(removed)) {
					StringBuilder.append(line);
					StringBuilder.append('\n');
				}
			}
			FileOutputStream fileOut = new FileOutputStream(config);
			fileOut.write(StringBuilder.toString().getBytes());
			fileOut.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected void addRuleToConfig(String addition) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(config, true))) {
			bw.newLine();
			bw.write(addition);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}