package loedje.screenshot_organisation;

import net.fabricmc.api.ClientModInitializer;

public class ScreenshotOrganisation implements ClientModInitializer {
	/**
	 * Runs the mod initializer on the client environment.
	 */
	@Override
	public void onInitializeClient() {
		ScreenshotOrganisationConfig.readConfig(null);
	}
}
