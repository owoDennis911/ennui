package me.earth.earthhack.impl.gui.click.component.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.earth.earthhack.api.setting.settings.EnumSetting;
import me.earth.earthhack.api.util.EnumHelper;
import me.earth.earthhack.impl.gui.click.component.SettingComponent;
import me.earth.earthhack.impl.managers.Managers;
import me.earth.earthhack.impl.util.render.RenderUtil;

public class EnumComponent<E extends Enum<E>> extends SettingComponent<E, EnumSetting<E>> {
    private final EnumSetting<E> enumSetting;
    private final float baseHeight;
    private boolean dropdownOpen = false;
    private float dropdownAnimationProgress = 0f;

    public EnumComponent(EnumSetting<E> enumSetting, float posX, float posY, float offsetX, float offsetY, float width, float height) {
        super(enumSetting.getName(), posX, posY, offsetX, offsetY, width, height, enumSetting);
        this.enumSetting = enumSetting;
        this.baseHeight = height;
    }

    @Override
    public float getHeight() {
        E[] values = enumSetting.getValue().getDeclaringClass().getEnumConstants();
        return baseHeight + (baseHeight * values.length * dropdownAnimationProgress);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float ANIMATION_SPEED = 0.1f;
        if (dropdownOpen && dropdownAnimationProgress < 1f) {
            dropdownAnimationProgress = Math.min(1f, dropdownAnimationProgress + ANIMATION_SPEED);
        } else if (!dropdownOpen && dropdownAnimationProgress > 0f) {
            dropdownAnimationProgress = Math.max(0f, dropdownAnimationProgress - ANIMATION_SPEED);
        }

        Managers.TEXT.drawStringWithShadow(getLabel() + ": " + ChatFormatting.GRAY + getEnumSetting().getValue().name(),
                getFinishedX() + 5,
                getFinishedY() + baseHeight / 2 - (Managers.TEXT.getStringHeightI() >> 1),
                0xFFFFFFFF);

        if (dropdownAnimationProgress > 0) {
            E[] values = enumSetting.getValue().getDeclaringClass().getEnumConstants();
            for (int i = 0; i < values.length; i++) {
                float optionY = getFinishedY() + baseHeight + (i * baseHeight);
                float slideOffset = baseHeight * (1 - dropdownAnimationProgress);
                optionY -= slideOffset;

                boolean isSelected = values[i].equals(getEnumSetting().getValue());
                int alpha = (int) (Math.pow(dropdownAnimationProgress, 2) * 255);
                int SELECTED_COLOR = 0xFF00FF00 ;
                int DEFAULT_COLOR = 0xFFFFFFFF;
                int baseColor = isSelected ? SELECTED_COLOR : DEFAULT_COLOR;
                int color = (alpha << 24) | (baseColor & 0x00FFFFFF);

                Managers.TEXT.drawStringWithShadow(values[i].name(),
                        getFinishedX() + 5,
                        optionY + baseHeight / 2 - (Managers.TEXT.getStringHeightI() >> 1),
                        color);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        boolean headerHovered = RenderUtil.mouseWithinBounds(mouseX, mouseY,
                getFinishedX() + 5,
                getFinishedY() + 1,
                getWidth() - 10,
                baseHeight - 2);

        if (headerHovered) {
            if (mouseButton == 0) {
                getEnumSetting().setValue((E) EnumHelper.next(getEnumSetting().getValue()));
            } else if (mouseButton == 1) {
                dropdownOpen = !dropdownOpen;
            }
        } else if (dropdownOpen && dropdownAnimationProgress > 0) {
            E[] values = enumSetting.getValue().getDeclaringClass().getEnumConstants();
            for (int i = 0; i < values.length; i++) {
                float optionY = getFinishedY() + baseHeight + (i * baseHeight);
                float slideOffset = baseHeight * (1 - dropdownAnimationProgress);
                optionY -= slideOffset;

                if (RenderUtil.mouseWithinBounds(mouseX, mouseY, getFinishedX() + 5, (int) optionY, getWidth() - 10, (int) baseHeight)) {
                    getEnumSetting().setValue(values[i]);
                    dropdownOpen = false;
                    break;
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    public EnumSetting<E> getEnumSetting() {
        return enumSetting;
    }
}