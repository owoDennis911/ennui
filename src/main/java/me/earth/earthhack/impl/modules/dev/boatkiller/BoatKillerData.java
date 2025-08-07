package me.earth.earthhack.impl.modules.dev.boatkiller;


import me.earth.earthhack.api.module.data.DefaultData;

final class BoatKillerData extends DefaultData<BoatKiller> {
    public BoatKillerData(BoatKiller module) {
        super(module);
        register(module.loop, "How many time the code will loop");
        register(module.ytp, "Will tp using y axis");
        register(module.y, "The first y , need to be lower than Y2 and required since you can't phase with only 1Y");
        register(module.y2, "The second y , set it to higer value than the the Y1 one.");
        register(module.xtp, "Will tp using x axis");
        register(module.x, "Set the range from x+ if above than 0 and x- if below than 0");
        register(module.ztp, "Will tp using z axis");
        register(module.z, "Set the range from z+ if above than 0 and z- if below than 0");
    }

    @Override
    public int getColor() {
        return 0xff34A1FF;
    }

    @Override
    public String getDescription() {
        return "Let you do some hacker thing while in boat:\n" +
                "Loop 2, Y 5.05,Y2 180 for boat tp (above nether roof)\n" +
                "Loop 4, Y 0.05, Y2 3 for boat pop (Try to instakill who is in the boat)\n" +
                "Loop 2, Y -5.05, Y2 -120(-180) for passenger tp (tp passenger under bedrock)\n" +
                "x and z tp work withing 6 to 60 range but fails if there is a block between you and your destination";
    }

}