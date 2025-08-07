package me.earth.earthhack.impl.gui.click.component.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.earth.earthhack.api.setting.settings.NumberSetting;
import me.earth.earthhack.impl.gui.click.component.SettingComponent;
import me.earth.earthhack.impl.managers.Managers;
import me.earth.earthhack.impl.util.math.MathUtil;
import me.earth.earthhack.impl.util.render.Render2DUtil;
import me.earth.earthhack.impl.util.render.RenderUtil;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Keyboard;

public class NumberComponent extends SettingComponent<Number, NumberSetting<Number>> {
    private final NumberSetting<Number> numberSetting;
    private boolean sliding;
    private boolean editing = false;
    private String currentInput = "";

    public NumberComponent(NumberSetting<Number> numberSetting, float posX, float posY, float offsetX, float offsetY, float width, float height) {
        super(numberSetting.getName(), posX, posY, offsetX, offsetY, width, height, numberSetting);
        this.numberSetting = numberSetting;
    }

    @Override
    public void moved(float posX, float posY) {
        super.moved(posX, posY);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        final boolean hovered = RenderUtil.mouseWithinBounds(mouseX, mouseY, getFinishedX(), getFinishedY(), getWidth(), getHeight());

        String displayValue = editing ? currentInput : ChatFormatting.GRAY.toString() + getNumberSetting().getValue();
        Managers.TEXT.drawStringWithShadow(getLabel() + ": " + displayValue,
                getFinishedX() + 5,
                getFinishedY() + getHeight() / 2 - (Managers.TEXT.getStringHeightI() >> 1),
                0xFFFFFFFF);

        if (!editing) {
            float length = MathHelper.floor(((getNumberSetting().getValue()).floatValue() - getNumberSetting().getMin().floatValue())
                    / (getNumberSetting().getMax().floatValue() - getNumberSetting().getMin().floatValue()) * (getWidth() - 10));
            Render2DUtil.drawBorderedRect(getFinishedX() + 5, getFinishedY() + getHeight() - 2.5f,
                    getFinishedX() + 5 + length, getFinishedY() + getHeight() - 0.5f,
                    0.5f, hovered ? getClickGui().get().color.getValue().brighter().getRGB()
                            : getClickGui().get().color.getValue().getRGB(), 0xff000000);
        }

        if (sliding && !editing) {
            double val = ((mouseX - (getFinishedX() + 5)) * (getNumberSetting().getMax().doubleValue() - getNumberSetting().getMin().doubleValue())
                    / (getWidth() - 10) + getNumberSetting().getMin().doubleValue());
            getNumberSetting().setValue(getNumberSetting().numberToValue(MathUtil.round(val, 2)));
        }
    }

    @Override
    public void keyTyped(char character, int keyCode) {
        if (editing) {
            if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
                //We prevent overflow this way (unlike like in meteor where we can set range and slider Range differently)
                try {
                    double inputVal = Double.parseDouble(currentInput);
                    if (inputVal < numberSetting.getMin().doubleValue()) {
                        inputVal = numberSetting.getMin().doubleValue();
                    }
                    if (inputVal > numberSetting.getMax().doubleValue()) {
                        inputVal = numberSetting.getMax().doubleValue();
                    }
                    numberSetting.setValue(numberSetting.numberToValue(inputVal));
                } catch (NumberFormatException ignored) {
                }
                editing = false;
                currentInput = "";
            }
            else if (keyCode == Keyboard.KEY_ESCAPE) {
                editing = false;
                currentInput = "";
            }
            else if (keyCode == Keyboard.KEY_BACK) {
                if (!currentInput.isEmpty()) {
                    currentInput = currentInput.substring(0, currentInput.length() - 1);
                }
            }
            else {
                if ((character >= '0' && character <= '9') || character == '.' || character == '-') {
                    currentInput += character;
                }
            }
        } else {
            super.keyTyped(character, keyCode);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        final boolean hovered = RenderUtil.mouseWithinBounds(mouseX, mouseY, getFinishedX(), getFinishedY(), getWidth(), getHeight());
        if (hovered && mouseButton == 0) {
            if (mouseY < getFinishedY() + getHeight() - 5) {
                editing = true;
                currentInput = numberSetting.getValue().toString();
            } else {
                setSliding(true);
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        if (isSliding())
            setSliding(false);
    }

    public NumberSetting<Number> getNumberSetting() {
        return numberSetting;
    }

    public boolean isSliding() {
        return sliding;
    }

    public void setSliding(boolean sliding) {
        this.sliding = sliding;
    }
}
