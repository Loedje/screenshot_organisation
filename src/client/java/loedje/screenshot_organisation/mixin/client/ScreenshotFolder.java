package loedje.screenshot_organisation.mixin.client;

import loedje.screenshot_organisation.ScreenshotOrganisation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.util.WorldSavePath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.io.File;
import java.util.Map;

@Mixin(ScreenshotRecorder.class)
public class ScreenshotFolder {

	@Shadow private File file;

	@ModifyVariable(method = "saveScreenshotInner", at = @At("STORE"), ordinal = 1)
	private static File injected(File file) {
		MinecraftClient client = MinecraftClient.getInstance();
		Map<String, String> rules = ScreenshotOrganisation.CONFIG.getRules();
		File screenshotsDir = new File(client.runDirectory, ScreenshotRecorder.SCREENSHOTS_DIRECTORY);

		if (client.getServer() != null) {
			String location = client.getServer().getSavePath(WorldSavePath.ROOT).toString();
			location = location.substring(0, location.length() - 2);
			if (rules.containsKey(location)) {
				return new File(rules.get(location));
			} else {
				String levelName = client.getServer().getSaveProperties().getLevelName();
				return new File(screenshotsDir, levelName);
			}
		} else if (client.getNetworkHandler() != null) {
			ServerInfo serverInfo = client.getNetworkHandler().getServerInfo();
			String location = serverInfo.address;
			if (rules.containsKey(location)) {
				return new File(rules.get(location));
			} else {
				return new File(screenshotsDir, location + " - " + serverInfo.name);
			}
		} else {
			return file;
		}
	}
}