package yellow.entities.units.entity

import arc.math.Mathf
import arc.util.Time
import arc.util.io.*
import mindustry.content.Fx
import mindustry.entities.Effect
import mindustry.gen.*
import yellow.entities.units.GhostUnitType

@Suppress("MemberVisibilityCanBePrivate", "unused")
open class GhostUnitEntity: UnitEntity() {
    var ghostLifetime = 0f
    var despawnEffect: Effect = Fx.none

    private var inited = false

    fun lifetimef(): Float {
        return ghostLifetime / (type as GhostUnitType).ghostLifetime
    }

    fun clampLifetime() {
        ghostLifetime = Mathf.clamp(ghostLifetime, 0f, (type as GhostUnitType).ghostLifetime)
    }

    fun ghostLifetime(time: Float){
        ghostLifetime = time
    }

    private fun initVars(){
        inited = true
        ghostLifetime = (type as GhostUnitType).ghostLifetime + Mathf.random(60f)
        despawnEffect = (type as GhostUnitType).despawnEffect
    }

    override fun type() = type as GhostUnitType
    
    override fun kill(){
        Fx.unitDespawn.at(x, y, 0f, this)
        remove()
    }
    
    override fun destroy(){
        Fx.unitDespawn.at(x, y, 0f, this)
        remove()
    }

    override fun update() {
        super.update()

        if(!inited) {
            initVars()
        }

        ghostLifetime -= Time.delta
        clampLifetime()

        if(ghostLifetime <= 0f) {
            val ty = (type as GhostUnitType)
            remove()
            ty.despawnEffect.at(x + ty.despawnEffectOffset.x, y + ty.despawnEffectOffset.y)
        }
    }

    override fun write(write: Writes) {
        super.write(write)
        write.f(ghostLifetime)
        write.bool(inited)
    }

    override fun read(read: Reads) {
        super.read(read)
        ghostLifetime = read.f()
        inited = read.bool()
    }

    override fun classId() = mappingId

    companion object {
        val mappingId = EntityMapping.register("ghost-unit", ::GhostUnitEntity)
    }
}
