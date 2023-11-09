package loedje.screenshot_organisation.mixin.client;

import loedje.screenshot_organisation.ScreenshotOrganisationConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.util.WorldSavePath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.io.File;

@Mixin(ScreenshotRecorder.class)
public class ScreenshotFolder {

	@ModifyVariable(method = "saveScreenshotInner", at = @At("STORE"), ordinal = 1)
	private static File injected(File file) {

		MinecraftClient minecraftClient = MinecraftClient.getInstance();
		String location;
		File destinationFile;
		if (minecraftClient.getServer() != null) {
			location = minecraftClient.getServer().getSavePath(WorldSavePath.ROOT).toString();
			location = location.substring(0, location.length() - 2);
			if (ScreenshotOrganisationConfig.rules.containsKey(location)) {
				destinationFile = new File(ScreenshotOrganisationConfig.rules.get(location));
			} else {
				destinationFile = new File(new File(MinecraftClient.getInstance().runDirectory,
								ScreenshotRecorder.SCREENSHOTS_DIRECTORY),
						(minecraftClient.getServer().getSaveProperties().getLevelName()));
			}

		} else {
			ServerInfo serverInfo = minecraftClient.getNetworkHandler().getServerInfo();
			location = serverInfo.address;
			if (ScreenshotOrganisationConfig.rules.containsKey(location)) {
				destinationFile = new File(ScreenshotOrganisationConfig.rules.get(location));
			} else {
				destinationFile = new File(new File(MinecraftClient.getInstance().runDirectory,
						ScreenshotRecorder.SCREENSHOTS_DIRECTORY),
						location + " - " + serverInfo.name);
			}
		}
		return destinationFile;
	}
}