package com.sollace.muzic;

import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public interface MuzicDisc {
    Identifier getId();

    Text getDescription();

    Identifier getSong();

    SoundEvent getSound();

    int getComparatorOutput();

    MuzicDiscPattern getPattern();

    int getColor(int layer);
}
