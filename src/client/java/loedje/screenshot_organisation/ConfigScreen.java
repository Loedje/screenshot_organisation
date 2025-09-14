package loedje.screenshot_organisation;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.nio.file.Path;
import java.util.List;

/**
 * TODO it's broken
 */
@Environment(value= EnvType.CLIENT)
public class ConfigScreen extends Screen {

	private static final String WORLD_PATH_OR_IP = "World path or IP";
	public static final int TEXTBOX_WIDTH = 200;
	private String sourceBoxContent = "";
	private String destinationBoxContent = "";
	private static final String SCREENSHOT_DESTINATION = "Screenshot destination";
	private final Screen parent;
	private final List<Drawable> drawables = Lists.newArrayList();
	protected ConfigScreen(Screen parent) {
		super(Text.literal("Screenshot Organisation configuration"));
		this.parent = parent;
	}

	/**
	 * Initialise widgets of config screen.
	 */
	@Override
	protected void init() {
		drawables.clear();
		TextFieldWidget sourceBox = new TextFieldWidget(textRenderer,
				width / 2 - TEXTBOX_WIDTH - 4,
				22,
				TEXTBOX_WIDTH,
				20,
				Text.literal(WORLD_PATH_OR_IP));
		sourceBox.setMaxLength(256);
		sourceBox.setText(sourceBoxContent);
		sourceBox.setChangedListener(s -> sourceBoxContent = sourceBox.getText()
				.trim()
				.replace("/","\\"));
		addDrawableChild(sourceBox);

		TextFieldWidget destinationBox = new TextFieldWidget(textRenderer,
				width / 2 + 4,
				22,
				TEXTBOX_WIDTH,
				20,
				Text.literal(SCREENSHOT_DESTINATION));
		destinationBox.setMaxLength(256);
		destinationBox.setText(destinationBoxContent);
		destinationBox.setChangedListener(s -> destinationBoxContent = destinationBox.getText()
				.trim());
		addDrawableChild(destinationBox);

		addDrawableChild(ButtonWidget.builder(Text.literal("Open Minecraft directory"),
				button -> Util.getOperatingSystem().open(client.runDirectory))
				.position(width / 2 - 150 / 2, 22 + 20 + 22)
				.size(150, 20)
				.build());

		ScreenshotFolderListWidget listWidget = new ScreenshotFolderListWidget(
				client,
				width,
				height - 36 -( 22 + 20 + 8 + 20 + 22),
				22 + 20 + 8 + 20 + 22,
				50);
		addDrawableChild(listWidget);

		addDrawableChild(ButtonWidget.builder(Text.literal("Add"), button -> {
			if (sourceBoxContent != null && destinationBoxContent != null) {
				ScreenshotOrganisation.CONFIG.addRuleToConfig(
						sourceBoxContent, destinationBoxContent);
				listWidget.addRule(sourceBoxContent, destinationBoxContent);
			}})
				.position(width / 2 - 150 - 4, height - 28)
				.size(71, 20)
				.build());

		addDrawableChild(ButtonWidget.builder(Text.literal("Remove"), button -> {
			String removedSource = listWidget.removeRule();
			if (removedSource != null) ScreenshotOrganisation.CONFIG.removeRule(removedSource);})
				.position(width / 2 - 71 - 4, height - 28)
				.size(71, 20)
				.build());

		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> client.setScreen(parent))
				.position(width / 2 + 4, height - 28)
				.size(150, 20)
				.build());

		ScreenshotOrganisation.CONFIG.getRules().forEach(listWidget::addRule);
	}

	@Override
	public void onFilesDropped(List<Path> paths) {
		paths.forEach(path -> {
			if (sourceBoxContent == null || sourceBoxContent.isEmpty()) {
				sourceBoxContent = path.toString();
			} else {
				destinationBoxContent = path.toString();
			}
		});
		clearAndInit();
	}

	@Override
	public void close() {
		client.setScreen(parent);
	}

	@Override
	protected <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement) {
		drawables.add(drawableElement);
		return super.addDrawableChild(drawableElement);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.drawCenteredTextWithShadow(
				textRenderer,
				Text.literal(WORLD_PATH_OR_IP),
				width / 2 - 100 - 4,
				8,
				0xFFFFFF);
		context.drawCenteredTextWithShadow(
				textRenderer,
				Text.literal(SCREENSHOT_DESTINATION),
				width / 2 + 4 + 100,
				8,
				0xFFFFFF);
		context.drawCenteredTextWithShadow(
				this.textRenderer,
				Text.literal("Drag and drop folders into this window").formatted(Formatting.GRAY),
				this.width / 2,
				22 + 20 + 8,
				0xFFFFFF);
		renderBackgroundTexture(context, Screen.MENU_BACKGROUND_TEXTURE, 0,0,0,0,width,height);
		for (Drawable drawable : drawables) {
			drawable.render(context, mouseX, mouseY, delta);
		}

	}

	@Environment(value=EnvType.CLIENT)
	protected class ScreenshotFolderListWidget extends AlwaysSelectedEntryListWidget<Entry> {

		private static final int ROW_WIDTH = 500;


		private ScreenshotFolderListWidget(MinecraftClient minecraftClient, int width, int height, int top, int itemHeight) {
			super(minecraftClient, width, height, top, itemHeight);
		}
		protected void addRule(String source, String destination) {
			addEntry(new ConfigScreen.Entry(Text.literal(source), Text.literal(destination)));
		}

		/**
		 * Removes selected rule from list widget and gives the removed rule or null
		 * @return null if nothing is removed, else the removes source.
		 */
		private String removeRule() {
			ConfigScreen.Entry entry = getSelectedOrNull();
			setSelected(getNeighboringEntry(NavigationDirection.DOWN));
			String removed = null;
			if (entry != null) {
				removed = entry.source.getString();
				removeEntryWithoutScrolling(entry);
			}
			return removed;
		}

		@Override
		public int getRowWidth() {
			return ROW_WIDTH;
		}
	}

	@Environment(value = EnvType.CLIENT)
	private class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> {

		private final Text source;
		private static final Text ARROW = Text.literal("➡");
		private final Text destination;

		public Entry(Text source, Text destination) {
			this.source = source;
			this.destination = destination;
		}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			context.drawCenteredTextWithShadow(textRenderer, source, x + entryWidth/2, y + 10, 0xFFFFFF);
			context.drawCenteredTextWithShadow(textRenderer, ARROW, x + entryWidth/2, y + 20, 0x909090);
			context.drawCenteredTextWithShadow(textRenderer, destination, x + entryWidth/2, y + 30, 0xFFFFFF);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			return true;
		}

		@Override
		public Text getNarration() {
			// narrates: "Selected *rule*, ..."
			return Text.translatable("narrator.select", Text.empty().append(source).append(Text.literal(" to ").append(destination)));
		}
	}
}
