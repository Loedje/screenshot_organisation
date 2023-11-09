package loedje.screenshot_organisation;

import net.fabricmc.api.ClientModInitializer;

public class ScreenshotOrganisation implements ClientModInitializer {
	public static final Config CONFIG = new Config();
	/**
	 * Runs the mod initializer on the client environment.
	 */
	@Override
	public void onInitializeClient() {
		CONFIG.readConfig();
	}
}
