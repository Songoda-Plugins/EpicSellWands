package com.craftaro.epicsellwands.gui;

import com.craftaro.epicsellwands.wand.Wand;
import com.songoda.core.gui.Gui;

import java.util.ArrayList;
import java.util.List;

public class GuiLoreEditor extends AbstractGuiListEditor {

    public GuiLoreEditor(Wand wand, Gui returnGui) {
        super(wand, returnGui);
    }

    @Override
    protected List<String> getData() {
        return new ArrayList<>(wand.getLore());
    }

    @Override
    protected void updateData(List<String> list) {
        wand.setLore(list);
    }

    @Override
    protected String validate(String line) {
        return line.trim();
    }
}
