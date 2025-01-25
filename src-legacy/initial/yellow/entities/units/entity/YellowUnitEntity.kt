package yellow.entities.units.entity

import arc.Events
import arc.math.Mathf
import arc.math.geom.Vec2
import arc.struct.Seq
import arc.util.Time
import arc.util.io.*
import kotmindy.mindustry.MUnit
import mindustry.Vars
import mindustry.content.UnitTypes
import mindustry.entities.Units
import mindustry.entities.units.*
import mindustry.gen.*
import mindustry.type.*
import yellow.YellowPermVars
import yellow.content.*
import yellow.entities.units.*
import yellow.game.YEventType.DeathInvalidationEvent
import yellow.util.YellowUtils.internalLog
import yellow.util.ins

@Suppress("MemberVisibilityCanBePrivate", "unused")
open class YellowUnitEntity: UnitEntity(){
    
    private var inited = false
    private var firstDeath = false
    private var franticTeleportTime = 60f
    private val everywhere = Vec2()

    //turn into private field?
    var lives = 0
    var iframes = 0f

    var allowsHealing = false
    var panicMode = false
    var panicModeTypeTwo = false
    var forceIdle = false
    var enableAutoIdle = false
    var idleTime = 0f

    var spells: Array<SpellBind?> = arrayOfNulls(0)

    private fun initVars(){
        inited = true
        lives = type().maxLives
        allowsHealing = Mathf.chance(0.346)
        panicMode = Mathf.chance(0.221)
        panicModeTypeTwo = Mathf.chance(0.124)

        spells = arrayOfNulls(type().spells.size)
        for(i in 0 ..< type().spells.size){
            spells[i] = type().spells[i].spellType[type().spells[i]]
        }

        entities.add(this)
    }
    
    private fun invalidateVars(){
        lives = 0
        allowsHealing = false
        panicMode = false
        panicModeTypeTwo = false
        dead = true
        health = 0f
        shield = 0f
    }
    
    private fun invalidateDeath(){

        //huh???
        try {
            type().afterDeath[-lives + type().maxLives]?.get(this)
        }catch(_: ArrayIndexOutOfBoundsException){
            //edge case for when yellow has m o r e lives than max lives
        }

        lives -= 1
        health = type.health
        dead = false
        elevation = 1f

        if((lives == type().maxLives - 1) && !firstDeath) {
            shield = Mathf.random(1000f, 8000f)
            firstDeath = true
        }
        
        if(isPlayer){
            Vars.ui.showInfoFade("$lives left!")
        }

        if(outOfWorldBounds()){
            if(team.data().cores.isEmpty){
                x = Mathf.random(Vars.world.width()) * 8f
                y = Mathf.random(Vars.world.height()) * 8f
            }else{
                val core = team.data().cores[0]
                x = core.x
                y = core.y
            }
        }else{
            x += Mathf.range(25f * 8f)
            y += Mathf.range(25f * 8f)
        }

        Events.fire(DeathInvalidationEvent(this))
    }
    
    private fun destroyFull(){
        invalidateVars()
        super.destroy()
    }
    
    private fun removeFull(){
        invalidateVars()
        super.remove()
    }

    fun spells() = spells

    fun outOfWorldBounds(): Boolean{
        return x > Vars.world.width() * 8f || x < 0f || y > Vars.world.height() * 8f || y < 0f
    }

    fun forceKill(){
        destroyFull()
    }

    fun despawn(){
        YellowFx.despawn.at(x, y)
        invalidateVars()
        super.remove()
    }

    fun isIdle() = elevation <= 0.1f

    fun lives() = lives
    
    fun lives(new: Int) {
        lives = new
    }

    fun livesf() = (lives / type().maxLives).toFloat()
    
    inline fun <reified T : WeaponMount> eachMountAs(max: Int, cons: (T) -> Unit){
        var index = 0
        mounts().forEach{
            if(index >= max) return
            if(it is T) cons(it)
            index++
        }
    }

    inline fun eachSpellAs(max: Int, cons: (SpellBind) -> Unit){
        var index = 0
        spells().forEach{
            if(index >= max) return
            cons(it!!)
            index++
        }
    }

    fun isInvulnerable() = iframes > 0

    fun findMount(weapon: Weapon) = mounts().find { it.weapon == weapon }

    override fun wobble(){}

    override fun type() = type as YellowUnitType
    
    override fun kill(){
        destroy() //just call destroy(), no point in waiting
    }

    override fun destroy(){
        if(isInvulnerable()) return
        
        if(lives > 1){
            invalidateDeath()
            return
        }
        
        destroyFull()
    }

    override fun remove(){
    
        if(!YellowPermVars.removeAllowed && lives > 1){
            return
        }

        removeFull()
    }

    override fun rawDamage(amount: Float){
        var finalDamage = amount
    
    	if(!isInvulnerable()){
    		iframes = Mathf.random(30f, 80f)
    		return
    	}

    	//cap damage to remaining health
    	//TODO do i?
    	//finalDamage = if(amount > health) health + 10f else amount

    	super.rawDamage(amount)
    }

    override fun damage(amount: Float){
        super.damage(amount)
    }
    
    override fun damage(amount: Float, withEffect: Boolean){
        damage(amount)
    }
    
    override fun damagePierce(amount: Float){
        damage(amount)
    }
    
    override fun damagePierce(amount: Float, withEffect: Boolean){
        damage(amount)
    }

    override fun damageContinuous(amount: Float) {
        if(!isInvulnerable()) super.damageContinuous(amount)
    }

    override fun damageContinuousPierce(amount: Float) {
        if(!isInvulnerable()) super.damageContinuousPierce(amount)
    }

    override fun apply(effect: StatusEffect, time: Float) {
        if(!isInvulnerable()) super.apply(effect, time)
    }

    override fun speed(): Float{
        return if(forceIdle){
            vel.set(0f, 0f)
            0f
        }else{
            super.speed()
        }
    }

    override fun update() {
        super.update()

        if(!inited) {
            initVars()
        }

        if(isInvulnerable()) iframes -= Time.delta

        //mfw meep testing utils
        spawnedByCore = false

        if(team.data().countType(type) > 1) {
            YellowPermVars.removeAllowed = true
            removeFull()
        } else {
            YellowPermVars.removeAllowed = false
        }

        spells().forEach{
            it?.update()
            //cuh
            it?.spell?.castListener?.update(this)
        }
        
        //heal surrounding units; normal units gain 70 health, player units gain either no health or a third of their current health
        if(allowsHealing){
            Units.nearby(x, y, 15f*8f, 15f*8f){a: MUnit ->
                if(a.team == team){
                    if(!a.isPlayer){
                        if(Mathf.chanceDelta(0.09)){
                            a.heal(70f)
                        }
                    }else{
                        if(Mathf.chanceDelta(0.14)){
                            a.heal(Mathf.random() * a.health.div(3f))
                        }
                    }
                }
            }
        }

        if(panicMode && lives == 1 && franticTeleportTime > 0f){
            everywhere.set(Mathf.random(Vars.world.width()) * 8f, Mathf.random(Vars.world.height()) * 8f)
            x = everywhere.x
            y = everywhere.y
            
            //if type two panic mode is enabled, start dropping quad bombs
            if(panicModeTypeTwo){
                UnitTypes.quad.weapons[0].bullet.create(this, x, y, 0f)
            }
            
            franticTeleportTime--
        }

        if((vel.len() == 0f && enableAutoIdle) || forceIdle) idleTime++ else idleTime = 0f
        if(idleTime > 600f || forceIdle){
            if(elevation > 0f) elevation -= 0.01f
            if(idleTime > 4200f){
                if(idleTime ins 60f) heal(Mathf.random(10f, 30f))
            }
        }else{
            if(elevation < 1f) elevation += 0.02f
        }
    }

    override fun draw(){
        super.draw()

        YellowDrawEffects.activeEffect.drawCode(this)
    }
    
    override fun toString() = if(isValid) "YellowUnitEntity#$id:${type.name}" else "(invalid) YellowUnitEntity#$id:${type.name}"

    override fun write(write: Writes){
        super.write(write)
        write.s(0)

        val mnt = mounts().size
        val spl = spells().size

        write.bool(inited)
        write.bool(firstDeath)
        write.bool(allowsHealing)
        write.bool(panicMode)
        write.bool(panicModeTypeTwo)
        write.i(lives)
        write.f(franticTeleportTime)
        write.f(idleTime)
        write.bool(enableAutoIdle)
        write.bool(forceIdle)
        write.i(mnt)
        write.i(spl)
        
        eachMountAs<DisableableWeaponMount>(mnt){
            it.write(write)
        }

        eachSpellAs(spl){
            it.write(write)
        }
    }

    @Suppress("ReplaceRangeToWithRangeUntil")
    override fun read(read: Reads){
        super.read(read)

        //i love init times
        spells = arrayOfNulls(type().spells.size)
        for(i in 0..type().spells.size - 1){
            spells[i] = type().spells[i].spellType[type().spells[i]]
        }

        val revision = read.s().toInt()
        internalLog("REVISION: $revision")

        when(revision){
            0 -> {
                inited = read.bool()
                firstDeath = read.bool()
                allowsHealing = read.bool()
                panicMode = read.bool()
                panicModeTypeTwo = read.bool()
                lives = read.i()
                franticTeleportTime = read.f()
                idleTime = read.f()
                enableAutoIdle = read.bool()
                forceIdle = read.bool()

                val mnt = read.i()
                val spl = read.i()

                eachMountAs<DisableableWeaponMount>(mnt) {
                    it.read(read)
                }

                eachSpellAs(spl) {
                    it.read(read)
                }
            }
        }
    }
    
    override fun classId() = mappingId

    companion object{
        val mappingId = EntityMapping.register("yellow-unit", ::YellowUnitEntity)

        private val blankAI = object: UnitController {
            override fun unit(unit: mindustry.gen.Unit?) {
            }

            override fun unit(): mindustry.gen.Unit {
                return Nulls.unit
            }
        }
        
        @JvmStatic
        val entities = Seq<YellowUnitEntity>()
    }
}
