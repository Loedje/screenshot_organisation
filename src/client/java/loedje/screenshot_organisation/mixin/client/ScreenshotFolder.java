package loedje.screenshot_organisation.mixin.client;

import loedje.screenshot_organisation.ScreenshotOrganisation;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import nl.enjarai.shared_resources.api.GameResourceHelper;
import nl.enjarai.shared_resources.api.GameResourceRegistry;
import nl.enjarai.shared_resources.api.ResourceDirectory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

@Mixin(value = ScreenshotRecorder.class, priority = 1200)
public class ScreenshotFolder {

	@ModifyVariable(method = "saveScreenshotInner", at = @At("STORE"), ordinal = 1)
	private static File injected(File file) {
		MinecraftClient client = MinecraftClient.getInstance();
		Map<String, String> rules = ScreenshotOrganisation.CONFIG.getRules();
		// Shared resources mod

		File screenshotsDir = new File(client.runDirectory,
				ScreenshotRecorder.SCREENSHOTS_DIRECTORY);

		// IDK if this affects performance when you take a screenie
		if (FabricLoader.getInstance().isModLoaded("shared-resources")) {

			ResourceDirectory resourceDirectory = ((ResourceDirectory) GameResourceRegistry.REGISTRY.get(new Identifier("shared-resources:screenshots")));
			Path path = GameResourceHelper.getPathFor(resourceDirectory);
			if (path != null) {
				screenshotsDir = path.toFile();
			}
		}

		if (client.getServer() != null) { // Single player
			String location = client.getServer().getSavePath(WorldSavePath.ROOT).toString();
			location = location.substring(0, location.length() - 2);
			if (rules.containsKey(location)) { // From config
				return new File(rules.get(location));
			} else { // No config
				String levelName = client.getServer().getSaveProperties().getLevelName();
				return new File(screenshotsDir, levelName.replaceAll("[\\\\/:*?\"<>|]", "_"));
			}
		} else if (client.getNetworkHandler() != null) { // Multiplayer
			ServerInfo serverInfo = client.getNetworkHandler().getServerInfo();
			String location = serverInfo.address;
			if (rules.containsKey(location)) { // From config
				return new File(rules.get(location));
			} else { // No config
				return new File(screenshotsDir, (location + " - " + serverInfo.name)
						.replaceAll("[\\\\/:*?\"<>|]", "_"));
			}
		} else { // Screenshot outside a world.
			return file;
		}
	}
}