package me.xmrvizzy.skyblocker.skyblock.shortcut;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class ShortcutsConfigScreen extends Screen {

    private ShortcutsConfigListWidget shortcutsConfigListWidget;
    private ButtonWidget buttonDelete;
    private ButtonWidget buttonNew;
    private ButtonWidget buttonDone;
    private double scrollAmount;

    public ShortcutsConfigScreen() {
        super(Text.translatable("skyblocker.shortcuts.config"));
    }

    @Override
    public void setTooltip(Text tooltip) {
        super.setTooltip(tooltip);
    }

    @Override
    protected void init() {
        super.init();
        shortcutsConfigListWidget = new ShortcutsConfigListWidget(client, this, width, height, 32, height - 64, 25);
        addDrawableChild(shortcutsConfigListWidget);
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(5).marginY(2);
        GridWidget.Adder adder = gridWidget.createAdder(2);
        buttonDelete = ButtonWidget.builder(Text.translatable("selectServer.delete"), button -> {
            if (client != null && shortcutsConfigListWidget.getSelectedOrNull() instanceof ShortcutsConfigListWidget.ShortcutEntry shortcutEntry) {
                scrollAmount = shortcutsConfigListWidget.getScrollAmount();
                client.setScreen(new ConfirmScreen(this::deleteEntry, Text.translatable("skyblocker.shortcuts.deleteQuestion"), Text.translatable("skyblocker.shortcuts.deleteWarning", shortcutEntry.target.getText() + " → " + shortcutEntry.replacement.getText()), Text.translatable("selectServer.deleteButton"), ScreenTexts.CANCEL));
            }
        }).build();
        adder.add(buttonDelete);
        buttonNew = ButtonWidget.builder(Text.translatable("skyblocker.shortcuts.new"), buttonNew -> shortcutsConfigListWidget.addShortcutAfterSelected()).build();
        adder.add(buttonNew);
        adder.add(ButtonWidget.builder(ScreenTexts.CANCEL, button -> {
            if (client != null) {
                client.setScreen(null);
            }
            Shortcuts.loadShortcuts(); // Cancel changes by reloading shortcuts from disk
        }).build());
        buttonDone = ButtonWidget.builder(ScreenTexts.DONE, button -> {
            if (client != null) {
                client.setScreen(null);
                Shortcuts.saveShortcuts(client); // Save shortcuts to disk
            }
        }).tooltip(Tooltip.of(Text.translatable("skyblocker.shortcuts.commandSuggestionTooltip"))).build();
        adder.add(buttonDone);
        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, this.height - 64, this.width, 64);
        gridWidget.forEachChild(this::addDrawableChild);
        updateButtons();
    }

    private void deleteEntry(boolean confirmedAction) {
        if (client != null) {
            if (confirmedAction && shortcutsConfigListWidget.getSelectedOrNull() instanceof ShortcutsConfigListWidget.ShortcutEntry shortcutEntry) {
                shortcutsConfigListWidget.getShortcutsMap(shortcutEntry.category).remove(shortcutEntry.target.getText());
            }
            client.setScreen(this); // Re-inits the screen and creates a new instance of ShortcutsConfigListWidget
            shortcutsConfigListWidget.setScrollAmount(scrollAmount);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 0xFFFFFF);
    }

    protected void updateButtons() {
        buttonDelete.active = Shortcuts.isShortcutsLoaded() && shortcutsConfigListWidget.getSelectedOrNull() instanceof ShortcutsConfigListWidget.ShortcutEntry;
        buttonNew.active = Shortcuts.isShortcutsLoaded() && shortcutsConfigListWidget.getCategory().isPresent();
        buttonDone.active = Shortcuts.isShortcutsLoaded();
    }
}
