package loedje.screenshot_organisation.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.ScreenshotRecorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.io.File;

@Mixin(ScreenshotRecorder.class)
public class ScreenshotFolder {
	@ModifyVariable(method = "saveScreenshotInner", at = @At("STORE"), ordinal = 1)
	private static File injected(File file) {
		MinecraftClient minecraftClient = MinecraftClient.getInstance();
		String s;
		if (minecraftClient.getServer() != null) {
			s = minecraftClient.getServer().getServerMotd()
					.replaceFirst(".*?- ", "");
		} else {
			ServerInfo serverInfo = minecraftClient.getNetworkHandler().getServerInfo();
			s = serverInfo.address + " - " + serverInfo.name;
		}
		return new File(new File(MinecraftClient.getInstance().runDirectory, ScreenshotRecorder.SCREENSHOTS_DIRECTORY), s);
	}
}