    package me.earth.earthhack.impl.modules.combat.autopot;

    import me.earth.earthhack.api.event.events.Stage;
    import me.earth.earthhack.impl.event.events.network.MotionUpdateEvent;
    import me.earth.earthhack.impl.event.listeners.ModuleListener;
    import me.earth.earthhack.impl.modules.combat.autopot.modes.*;
    import me.earth.earthhack.impl.util.math.StopWatch;
    import me.earth.earthhack.impl.util.minecraft.InventoryUtil;
    import me.earth.earthhack.impl.util.thread.Locks;
    import net.minecraft.block.Block;
    import net.minecraft.client.gui.GuiChat;
    import net.minecraft.client.gui.inventory.GuiInventory;
    import net.minecraft.init.Blocks;
    import net.minecraft.init.MobEffects;
    import net.minecraft.inventory.ClickType;
    import net.minecraft.item.ItemFood;
    import net.minecraft.item.ItemStack;
    import net.minecraft.network.play.client.CPacketPlayer;
    import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
    import net.minecraft.potion.PotionEffect;
    import net.minecraft.potion.PotionUtils;
    import net.minecraft.util.EnumHand;
    import net.minecraft.util.ResourceLocation;
    import net.minecraft.util.math.BlockPos;

    import java.util.List;


    class ListenerMotion extends ModuleListener<AutoPot, MotionUpdateEvent> {


        private final StopWatch speedUseTimer = new StopWatch();
        private final StopWatch healUseTimer = new StopWatch();
        protected int healSlot = -1;
        protected int speedSlot = -1;


        public ListenerMotion(AutoPot module) {
            super(module, MotionUpdateEvent.class, 1000);
        }


        @Override
        public void invoke(MotionUpdateEvent event) {
            healSlot = module.healSlot.getValue();
            speedSlot = module.speedSlot.getValue();
            if (event.getStage() == Stage.PRE) {
                module.calcGround();
                module.calcThrows();
                if (module.groundedEnough()) {
                    if (module.needHeal() && module.heal.getValue()) {
                        if (mc.currentScreen == null || mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiChat) {
                            healToHotbar();
                        }
                        ItemStack iq = mc.player.openContainer.getSlot(healSlot + 36).getStack();
                        if (isHealingPotion(iq)) {
                            if (module.throwMode.getValue() == throwType.INSTANT && module.throwCounter < module.maxInstantThrows.getValue() || healUseTimer.passed(module.healDelay.getValue())) {
                                rotate(event);
                                if (module.stageForThrow.getValue() == stageThrow.PRE) {
                                    useHealPotion();
                                }
                            }
                        }
                    } else if (module.needSpeed() && module.speed.getValue()) {
                            if (speedUseTimer.passed(module.speedDelay.getValue())) {
                                rotate(event);
                                if (module.stageForThrow.getValue() == stageThrow.PRE) {
                                    useSpeedPotion();
                                }
                            }
                        }
                }
            } else if (event.getStage() == Stage.POST && module.stageForThrow.getValue() == stageThrow.POST) {
                if (module.groundedEnough()) {
                    if (module.whileEating.getValue() || !(mc.player.getActiveItemStack().getItem() instanceof ItemFood)) {
                        if (module.needHeal() && module.heal.getValue()) {
                            if (module.throwMode.getValue() == throwType.INSTANT && module.throwCounter < module.maxInstantThrows.getValue() || healUseTimer.passed(module.healDelay.getValue())) {
                                ItemStack iq = mc.player.openContainer.getSlot(healSlot + 36).getStack();
                                if (isHealingPotion(iq)) {
                                    useHealPotion();
                                }
                            }
                        } else if (module.needSpeed() && module.speed.getValue()) {
                            if (module.whileEating.getValue() || !(mc.player.getActiveItemStack().getItem() instanceof ItemFood)) {
                                if (speedUseTimer.passed(module.speedDelay.getValue())) {
                                        useSpeedPotion();
                                }
                            }
                        }
                    }
                }
            }
        }

        private void useHealPotion() {
            Locks.acquire(Locks.PLACE_SWITCH_LOCK, () -> {
                int lastSlot = mc.player.inventory.currentItem;
                boolean silent = module.silentSwitch.getValue() || mc.gameSettings.keyBindUseItem.isKeyDown();
                InventoryUtil.switchTo(healSlot);
                mc.playerController.processRightClick(mc.player, mc.world, InventoryUtil.getHand(healSlot));
                if (silent) {
                    InventoryUtil.switchTo(lastSlot);
                    module.needSpeed = false;
                } else if (lastSlot != healSlot) {
                    module.lastSlot = lastSlot;
                }
                module.throwCounter++;
                healUseTimer.reset();
            });
        }


        private void useSpeedPotion() {

            int k = findSpeed();
            if (k != -1) {
                Locks.acquire(Locks.PLACE_SWITCH_LOCK, () -> {
                    int lastSlot = mc.player.inventory.currentItem + 36;
                    if (!isSpeedPotion(mc.player.inventory.getItemStack())) {
                        InventoryUtil.click(k);
                        InventoryUtil.click(lastSlot);
                        mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                        InventoryUtil.click(lastSlot);
                        InventoryUtil.click(k);
                    } else {
                        InventoryUtil.click(lastSlot);
                        InventoryUtil.click(lastSlot);
                    }
                    speedUseTimer.reset();
                });
            }
        }

        private void rotate(MotionUpdateEvent event) {
            float pitch = mc.player.rotationPitch;
            float yaw = mc.player.rotationYaw;
            rotationIQ iq = module.rotIQ.getValue();
            if (iq == rotationIQ.noIQ) {
                pitch = 90.0f;
            }
            if (iq == rotationIQ.lowIQ) {
                if (isBlockAboveHead()) {
                    pitch = -90.0f;
                } else {
                    pitch = 90.0f;
                }
            }

            if (iq == rotationIQ.AverageIQ ) {
                if (!isMoving()) {
                    if (isBlockAboveHead()) {
                        pitch = -90.0f;
                    } else {
                        pitch = 90.0f;
                    }
                } else {
                    yaw = mc.player.rotationYaw;
                    pitch = 70;
                }
            }
            sendRotation(event, pitch, yaw);
        }

        private void sendRotation(MotionUpdateEvent event, float pitch, float yaw) {

            if (module.rotMode == null || mc.player == null) return;
            rotationMode mode = module.rotMode.getValue();
            switch (mode) {
                case Both:
                    if (!mc.player.onGround) {
                        event.setPitch(pitch);
                    } else {
                        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, pitch, true));
                    }
                    break;

                case Event:
                    event.setPitch(pitch);
                    break;

                case Packet:
                    if (mc.player.onGround) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, pitch, true));
                    }
            }
        }


        private void healToHotbar() {
            int hotbarSlot = 36 + healSlot;

            ItemStack currentStack = mc.player.openContainer.getSlot(hotbarSlot).getStack();
            if (!currentStack.isEmpty() && isHealingPotion(currentStack)) {
                return;
            }

            refill mode = module.refillType.getValue();

            for (int i = 0; i < 45; i++) {
                ItemStack stack = mc.player.openContainer.getSlot(i).getStack();
                if (!stack.isEmpty() && isHealingPotion(stack)) {
                    switch (mode) {
                        case PickUp:
                            int inS = i;
                            Locks.acquire(Locks.WINDOW_CLICK_LOCK, () -> {
                                if (!isHealingPotion(mc.player.inventory.getItemStack())) {
                                    InventoryUtil.click(inS);
                                    InventoryUtil.click(hotbarSlot);
                                } else {
                                    InventoryUtil.click(hotbarSlot);
                                }
                            });
                            break;


                        case Swap:
                            mc.playerController.windowClick(0, i, healSlot, ClickType.SWAP, mc.player);
                            break;

                        case QuickMove:
                            mc.playerController.windowClick(0, i, 0, ClickType.QUICK_MOVE, mc.player);
                            break;

                        default:
                            break;
                    }
                    break;
                }
            }
        }

        private int findSpeed() {
            for (int i = 0; i < 45; i++) {
                ItemStack stack = mc.player.openContainer.getSlot(i).getStack();
                if (!stack.isEmpty() && isSpeedPotion(stack)) {
                   return i;
                }
            }
            return -1;
        }


        private boolean isHealingPotion(ItemStack potionStack) {
            if (potionStack == null || potionStack.isEmpty()) {
                return false;
            }
            potionStack.getItem();
            List<PotionEffect> effects = PotionUtils.getEffectsFromStack(potionStack);
            for (PotionEffect effect : effects) {
                if (effect.getPotion() == MobEffects.INSTANT_HEALTH) {
                    return true;
                }
            }
            return false;
        }


        private boolean isSpeedPotion(ItemStack potionStack) {

            if (potionStack == null || potionStack.isEmpty()) {
                return false;
            }
            potionStack.getItem();
            List<PotionEffect> effects = PotionUtils.getEffectsFromStack(potionStack);
            for (PotionEffect effect : effects) {
                if (effect.getPotion() == MobEffects.SPEED) {
                    return true;
                }
            }
            return false;
        }


        private boolean isStrengthPotion(ItemStack potionStack) {
            if (!potionStack.hasTagCompound()) return false;

            ResourceLocation registryName = PotionUtils.getPotionFromItem(potionStack).getRegistryName();
            if (registryName == null) return false;

            return registryName.getPath().contains("strength");
        }

        private boolean isBlockAboveHead() {
            BlockPos blockPos = new BlockPos(mc.player.posX, mc.player.posY + 2, mc.player.posZ);
            Block block = mc.world.getBlockState(blockPos).getBlock();
            return block != Blocks.AIR;
        }

        private boolean isMoving() {
            return mc.player.motionX == 0 && mc.player.motionY == 0 && mc.player.motionZ == 0;
        }
    }