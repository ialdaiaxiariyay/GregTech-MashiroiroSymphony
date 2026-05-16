package top.ialdaiaxiariyay.gtms.api.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ButtonEntry {

    public String translationKey;
    public Button.OnPress onPress;

    public ButtonEntry(String key, Button.OnPress press) {
        translationKey = key;
        onPress = press;
    }
}
