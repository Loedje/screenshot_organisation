package loedje.screenshot_organisation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

@Environment(value= EnvType.CLIENT)
public class ConfigScreen extends Screen {

	private static final String WORLD_PATH_OR_IP = "World path or IP";
	private final ScreenshotOrganisationConfig config = new ScreenshotOrganisationConfig();
	private String worldOrIPBoxContent;
	private String destinationBoxContent;
	private static final String SCREENSHOT_DESTINATION = "Screenshot destination";
	private final Screen parent;
	protected ConfigScreen(Screen parent) {
		super(Text.literal("Screenshot Organisation configuration"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		TextFieldWidget worldOrIPBox = new TextFieldWidget(textRenderer,
				width / 2 - 200 - 4,
				22,
				200,
				20,
				Text.literal(WORLD_PATH_OR_IP));
		worldOrIPBox.setMaxLength(256);
		worldOrIPBox.setText(worldOrIPBoxContent);
		worldOrIPBox.setChangedListener(s -> worldOrIPBoxContent = worldOrIPBox.getText());
		addDrawableChild(worldOrIPBox);

		TextFieldWidget destinationBox = new TextFieldWidget(textRenderer,
				width / 2 + 4,
				22,
				200,
				20,
				Text.literal(SCREENSHOT_DESTINATION));
		destinationBox.setMaxLength(256);
		destinationBox.setText(destinationBoxContent);
		destinationBox.setChangedListener(s -> destinationBoxContent = destinationBox.getText());
		addDrawableChild(destinationBox);

		addDrawableChild(ButtonWidget.builder(Text.literal("Browse"), button -> System.out.println(1))
				.position(width / 2 - 200 - 4, 22 + 20 + 8)
				.size(71, 20)
				.build());

		addDrawableChild(ButtonWidget.builder(Text.literal("Browse"), button -> System.out.println(2))
				.position(width / 2 + 4, 22 + 20 + 8)
				.size(71, 20)
				.build());

		ScreenshotFolderListWidget listWidget = new ScreenshotFolderListWidget(
				client,
				width,
				height,
				22 + 20 + 8 + 20 + 8,
				ConfigScreen.this.height - 36,
				50);
		addDrawableChild(listWidget);

		addDrawableChild(ButtonWidget.builder(Text.literal("Add"), button -> {
			if (worldOrIPBoxContent != null && destinationBoxContent != null) {
				worldOrIPBoxContent = worldOrIPBoxContent.trim();
				destinationBoxContent = destinationBoxContent.trim();
				ScreenshotOrganisationConfig.rules.put(worldOrIPBoxContent,destinationBoxContent);
				listWidget.addRule(worldOrIPBoxContent, destinationBoxContent);
				config.addRuleToConfig(worldOrIPBoxContent + "=" + destinationBoxContent);
			}})
				.position(width / 2 - 150 - 4, height - 28)
				.size(71, 20)
				.build());

		addDrawableChild(ButtonWidget.builder(Text.literal("Remove"), button -> {
			String removed = listWidget.removeRule(config);
			if (removed != null) config.removeRuleInConfig(removed);})
				.position(width / 2 - 71 - 4, height - 28)
				.size(71, 20)
				.build());

		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> client.setScreen(parent))
				.position(width / 2 + 4, height - 28)
				.size(150, 20)
				.build());

		config.readConfig(listWidget);
		ScreenshotOrganisationConfig.rules.keySet().forEach(System.out::println);
	}

	@Override
	public void close() {
		client.setScreen(parent);
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
		super.render(context, mouseX, mouseY, delta);
	}

	@Environment(value=EnvType.CLIENT)
	protected class ScreenshotFolderListWidget extends AlwaysSelectedEntryListWidget<Entry> {

		private static final int ROW_WIDTH = 400;


		private ScreenshotFolderListWidget(MinecraftClient minecraftClient, int width, int height, int top, int bottom, int itemHeight) {
			super(minecraftClient, width, height, top, bottom, itemHeight);
		}
		protected void addRule(String source, String destination) {
			addEntry(new ConfigScreen.Entry(Text.literal(source), Text.literal(destination)));
		}

		private String removeRule(ScreenshotOrganisationConfig config) {
			ConfigScreen.Entry entry = getSelectedOrNull();
			setSelected(getNeighboringEntry(NavigationDirection.DOWN));
			String removed = null;
			if (entry != null) {
				ScreenshotOrganisationConfig.rules.remove(entry.source.getString());
				removed = entry.source.getString() + "=" + entry.destination.getString();
				removeEntryWithoutScrolling(entry);
			}
			return removed;
		}

		@Override
		protected int getScrollbarPositionX() {
			return width/2 + ROW_WIDTH/2 + 14;
		}

		@Override
		public int getRowWidth() {
			return ROW_WIDTH;
		}

		@Override
		protected boolean removeEntryWithoutScrolling(ConfigScreen.Entry entry) {
			return super.removeEntryWithoutScrolling(entry);
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
