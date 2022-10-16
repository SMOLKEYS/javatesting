package yellow;

import arc.Events;
import arc.util.Log;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.mod.Mod;
import mindustry.type.Weapon;
import yellow.content.*;
import yellow.ctype.FallbackContentList;
import yellow.internal.YellowClassGateway;
import yellow.internal.util.YellowUtils;
import yellow.ui.YellowSettings;
import yellow.ui.buttons.YellowWeaponSwitch;
import yellow.weapons.YellowWeapons;

import static arc.Core.app;
import static mindustry.Vars.headless;
import static mindustry.Vars.ui;
import static yellow.weapons.YellowWeapons.*;

public class Yellow extends Mod{
    
    public static YellowWeaponSwitch weaponSwitch = new YellowWeaponSwitch();
    
    public Yellow(){
        String yellow = "yellow suse ";
        for(int i = 0; i < 5; i++) yellow += yellow;
        Log.info(yellow);
        
        Events.run(ClientLoadEvent.class, () -> {
            weaponSwitch.build(ui.hudGroup);
            
            YellowClassGateway ycg = new YellowClassGateway();
            ycg.load();

            YellowSettings.INSTANCE.load();
            YellowUtils.startRequestLimitHandler();
        });
    }

    public final FallbackContentList[] yellowContent = {
        new YellowUnitTypes(),
        new YellowStatusEffects(),
        new YellowPlanets(),
        new YellowBlocks()
    };
    
    public final FallbackContentList bullets = new YellowBullets();
    
    @Override
    public void loadContent(){
        bullets.load();
        YellowWeapons.init();
        
        for(FallbackContentList list : yellowContent){
            list.load();
        }

        YellowWeapons.afterInit();

        YellowUtils.mirror(new Weapon[]{meltdownBurstAttack, antiMothSpray, decimation, airstrikeFlareLauncher, ghostCall}, true, true, YellowUnitTypes.yellow);
    }
    
}
