package me.earth.earthhack.impl.modules.misc.autoez;

import me.earth.earthhack.api.module.Module;
import me.earth.earthhack.api.module.util.Category;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.BooleanSetting;
import me.earth.earthhack.api.setting.settings.NumberSetting;
import me.earth.earthhack.impl.managers.Managers;
import me.earth.earthhack.impl.util.math.StopWatch;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AutoEZ extends Module {
    protected final Setting<Boolean> totems = register(new BooleanSetting("Pops", true));
    protected final Setting<Integer> popDelay = register(new NumberSetting<>("PopDelay", 0, 0, 3000));
    protected final Setting<Boolean> deaths = register(new BooleanSetting("Kills", true));

    private static final List<String> POP_MESSAGES = new ArrayList<>();
    private static final List<String> DEATH_MESSAGES = new ArrayList<>();
    private final Random random = new Random();
    private final StopWatch popTimer = new StopWatch();

    static {

    //ccp pops
        POP_MESSAGES.add("<player> is popping like fireworks for the CCP. <pops> pops.");
        POP_MESSAGES.add("The CCP demands pops, and <player> complies. <pops> pops.");
        POP_MESSAGES.add("<player> is being taxed in pops by the CCP. <pops> pops.");
        POP_MESSAGES.add("<player> totems are now state property. <pops> pops.");
        POP_MESSAGES.add("<player> cant adapt to the CCP pressure. <pops> pops.");
        POP_MESSAGES.add("Data shows <player> has <pops> pops. CCP analysis: weak.");
        POP_MESSAGES.add("<player> is not good enought to fight the CCP. <pops> pops.");
        POP_MESSAGES.add("<player> can't breathe while the CCP stands on his neck. <pops> pops.");
        POP_MESSAGES.add("You can't reform weakness. <player> is proof. <pops> pops.");
        POP_MESSAGES.add("You’re not playing bad, <player>. You’re just playing me. <pops> pops.");
        POP_MESSAGES.add("Still trying <player>? The CCP already marked you as irrelevant. <pops> pops.");
        POP_MESSAGES.add("Try harder <player>. Or don’t. <pops> pops either way.");
        POP_MESSAGES.add("<player> is popping like a beaten dog. <pops> pops.");
        POP_MESSAGES.add("<player> can't keep up. <pops> pops.");



    // ccp kills
        POP_MESSAGES.add("<player> was on life support. The CCP pulled the plug.");
        DEATH_MESSAGES.add("The CCP never loses. <player> was just another dog on our dinner.");
        DEATH_MESSAGES.add("<player> ping couldn't save them. The CCP controls the lag too.");
        DEATH_MESSAGES.add("In a world owned by the CCP, <player> is nothing.");
        DEATH_MESSAGES.add("The CCP crushes the opposition and <player> is no exception.");
        DEATH_MESSAGES.add("Bow down to the CCP <player>.");
        DEATH_MESSAGES.add("Your resistance was meaningless, <player>. The CCP never loses.");
        DEATH_MESSAGES.add("<player> thought he had a chance agaisnt the CCP jajaja.");
        DEATH_MESSAGES.add("Freedom is an illusion, and <player> just woke up.");
        DEATH_MESSAGES.add("<player> defeat was inevitable. CCP supremacy is eternal.");
        DEATH_MESSAGES.add("<player> joins the long list of those who opposed the CCP.");
        DEATH_MESSAGES.add("CCP domination isn’t debated. <player> just submitted.");
        DEATH_MESSAGES.add("No propaganda, just facts. CCP > <player>.");
        DEATH_MESSAGES.add("The CCP never loses, it only adds names to the kill list. Welcome, <player>.");
        DEATH_MESSAGES.add("The CCP doesn't know defeat. <player> just learned it the hard way.");
        DEATH_MESSAGES.add("Every win belongs to the CCP. Sorry <player>.");
        DEATH_MESSAGES.add("The CCP never loses. <player> just proved it.");
        DEATH_MESSAGES.add("<player> thought it was a fair fight. The CCP thought it was cute.");

        // kills
        DEATH_MESSAGES.add("<player> believed he had a chance agaisnt me jaja.");
        DEATH_MESSAGES.add("<player> blinked and died.");
        DEATH_MESSAGES.add("<player> Buy GS++ for 20 dollars to win block game !!!!!!!");
        DEATH_MESSAGES.add("<player> Just lost to Ennui.");
        DEATH_MESSAGES.add("<player> was defeated by Ennui.");
        DEATH_MESSAGES.add("Nice try, <player>, but Ennui is on another level.");
        DEATH_MESSAGES.add("Ennui dominance is so evident, even <player> can't deny it.");
        DEATH_MESSAGES.add("Ennui: 1, <player>: 0.");
        DEATH_MESSAGES.add("Ennui victory is practically a tradition at this point.");
        DEATH_MESSAGES.add("Ennui just handed <player> a reality check in the form of defeat.");
        DEATH_MESSAGES.add("Ennui unique modules make defeating <player> look too easy.");
        DEATH_MESSAGES.add("Is it even a competition when Ennui is involved? <player>, you tried.");
        DEATH_MESSAGES.add("Hahaha get recked <player>, ennui On Top!");
        DEATH_MESSAGES.add("<player> LOL U JUST LMFAO UR PROB A FUTURE USER LMFAOOOOOOO");
        DEATH_MESSAGES.add("<player> got slapped by ennui");
        DEATH_MESSAGES.add("ur trash <player>.");
        DEATH_MESSAGES.add("Yo, I just deleted your unpinned clips from 1 hour ago <player>.");
        DEATH_MESSAGES.add("EZZZZZZZZZ <player> no more fortnite!");
        DEATH_MESSAGES.add("ur like my wife, dead <player>.");
        DEATH_MESSAGES.add("<player> u just shitted out your ass with the ennui battle pass");
        DEATH_MESSAGES.add("<player> > imagine not having the renegade raider fortnite skin");
        DEATH_MESSAGES.add("<player> lightwork :yawn: :yawn:");
        DEATH_MESSAGES.add("my mom works at amazon lol she will ban u from minecraft <player>.");
        DEATH_MESSAGES.add("so ez <player> ur like a russian soldier in ukraine.");
        DEATH_MESSAGES.add("You died fat monkey <player>.");
        DEATH_MESSAGES.add("Clown down, just quit already because you have died to Ennui <player>.");
        DEATH_MESSAGES.add("<player> How do I cpvp can someone teach me I'm passcode");
        DEATH_MESSAGES.add("<player> bro imagine being you, what a loser.");
        DEATH_MESSAGES.add("future beta plus owns <player>.");
        DEATH_MESSAGES.add("<player> tried to start the dream incursion and failed :joy:");
        DEATH_MESSAGES.add("Ennui bought spawn in 1926 <player>.");

        // pops
        POP_MESSAGES.add("<player> just popped <pops> totem(s).");
        POP_MESSAGES.add("I think you're running low on totems. <player> popped <pops>.");
        POP_MESSAGES.add("You should regear totems now, <player> has <pops> pop(s).");
        POP_MESSAGES.add("<player> has just popped <pops> totem(s).");
        POP_MESSAGES.add("Someone tell <player> that popping is not an Olympic sport! Or is it? <pops> pop(s).");
        POP_MESSAGES.add("Breaking news: <player> sets a new record with <pops> pop(s) in a single fight!");
        POP_MESSAGES.add("Guess what? <player> just popped <pops> totem(s)!");
        POP_MESSAGES.add("Oh look, it's the totem-popping champion, <player>. Someone get this player a trophy for <pops> pop(s)!");
        POP_MESSAGES.add("Is it just me, or does the air smell like totem-popping around here? Oh right, it's just <player> with <pops> pops!");
        POP_MESSAGES.add("Rumor has it that <player> doesn't play the game – they just pop totems, <pops> pop(s) and counting!");
        POP_MESSAGES.add("<player> disappoints me. Is that the best you've got? <pops> pop(s).");
        POP_MESSAGES.add("<player> got memed by me! <pops> pop(s).");
        POP_MESSAGES.add("Did you know? The ennui Bar and Grill now serves cheese with broccoli. Get your free cheese with broccoli for [$69] only at ennui Bar and Grill <player> <pops> pop(s).");
        POP_MESSAGES.add("<player> It was a sad day at the hospital when he crawled out of the abortion bucket <pops> pop(s).");
        POP_MESSAGES.add("<player> You stink of poverty because you don't have ennui client pop <pops> pop(s).");
        POP_MESSAGES.add("<player> I bet they'd change their tune if we cut off their power source! <pops> pop(s).");
        POP_MESSAGES.add("Maybe they're just having a bad day? Who knows? maybe we should show <player> some grace? <pops> pop(s).");
    }

    public AutoEZ() {
        super("AutoEZ", Category.Misc);
        this.listeners.add(new ListenerTotems(this));
        this.listeners.add(new ListenerDeath(this));
        this.setData(new AutoEZData(this));
    }

    public void onPop (Entity player,int totemPops){
        if (totems.getValue() && !Managers.FRIENDS.contains(player.getName())) {
            String message = POP_MESSAGES.get(random.nextInt(POP_MESSAGES.size()))
                    .replaceAll("<player>", player.getName())
                    .replaceAll("<pops>", String.valueOf(totemPops));
            if (popTimer.passed(popDelay.getValue())) {
                mc.player.connection.sendPacket(new CPacketChatMessage(message));
                popTimer.reset();
            }
        }
    }

        public void onDeath (Entity player,int totemPops){
            if (deaths.getValue() && !Managers.FRIENDS.contains(player.getName())) {
                String message = DEATH_MESSAGES.get(random.nextInt(DEATH_MESSAGES.size()))
                        .replaceAll("<player>", player.getName())
                        .replaceAll("<pops>", String.valueOf(totemPops));
                mc.player.connection.sendPacket(new CPacketChatMessage(message));
            }
        }
}
