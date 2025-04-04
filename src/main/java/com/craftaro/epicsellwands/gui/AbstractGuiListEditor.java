package com.craftaro.epicsellwands.gui;

import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.epicsellwands.wand.Wand;
import com.songoda.core.gui.AnvilGui;
import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.utils.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractGuiListEditor extends Gui {

    protected final Wand wand;
    private final Gui returnGui;

    public AbstractGuiListEditor(Wand wand, Gui returnGui) {
        super(1, returnGui);
        this.returnGui = returnGui;
        this.wand = wand;
        paint();
    }

    public void paint() {
        List<String> lore = getData() == null ? new ArrayList<>() : getData();
        setButton(2, GuiUtils.createButtonItem(XMaterial.OAK_FENCE_GATE,
                TextUtils.formatText("&cBack")),
                (event) -> {
                    guiManager.showGUI(event.player, returnGui);
                    ((GuiEditWand) returnGui).paint();
                });
        setButton(6, GuiUtils.createButtonItem(XMaterial.OAK_FENCE_GATE,
                TextUtils.formatText("&cBack")),
                (event) -> {
                    guiManager.showGUI(event.player, returnGui);
                    ((GuiEditWand) returnGui).paint();
                });
        setButton(3, GuiUtils.createButtonItem(XMaterial.ARROW,
                TextUtils.formatText("&aAdd new line")),
                (event -> {
                    AnvilGui gui = new AnvilGui(event.player, this);
                    gui.setAction((e -> {
                        String validated = validate(gui.getInputText());
                        if (validated != null) {
                            lore.add(validated);
                            updateData(lore.isEmpty() ? null : lore);
                            e.player.closeInventory();
                            paint();
                        }
                    }));
                    gui.setTitle("Enter a new line");
                    guiManager.showGUI(event.player, gui);
                }));

        setItem(4, GuiUtils.createButtonItem(XMaterial.WRITABLE_BOOK,
                TextUtils.formatText("&9Lore Override:"),
                lore.isEmpty()
                        ? TextUtils.formatText(Collections.singletonList("&cNo lore set..."))
                        : TextUtils.formatText(lore)));

        setButton(5, GuiUtils.createButtonItem(XMaterial.ARROW,
                TextUtils.formatText("&cRemove the last line")),
                (event -> {
                    lore.remove(lore.size() - 1);
                    updateData(lore);
                    paint();
                }));
    }

    protected abstract List<String> getData();

    protected abstract void updateData(List<String> list);

    protected abstract String validate(String line);
}
