package yellow.entities.bullet

import arc.math.Mathf
import arc.util.Time
import mindustry.entities.bullet.*
import mindustry.gen.Bullet

open class AirstrikeFlare(var missileBullet: BulletType): ArtilleryBulletType() {
    init {
        shrinkX = 0f
        shrinkY = 0f
        lifetime = 20f
        speed = 15f
        collides = true
        collidesAir = true
        collidesGround = true
    }

    var missileCount = 8
    var minMissileCount = 4
    var arrivalDelay = 240f
    var minArrivalDelay = 60f
    var missileLifetimeRandomization = 3f
    var posRandomization = 120f
    var randomizeMissileCount = true

    private fun misRng(): Int {
        val yes: Int

        if(randomizeMissileCount) {
            yes = Mathf.random(minMissileCount, missileCount)
        } else {
            return missileCount
        }

        return yes
    }

    override fun despawned(b: Bullet) {
        super.despawned(b)

        val x = b.x
        val y = b.y

        for(i in 0..misRng()) {
            Time.run(Mathf.random(minArrivalDelay, arrivalDelay)) {
                BulletType.createBullet(
                    missileBullet,
                    b.team,
                    x + Mathf.range(posRandomization),
                    y + Mathf.range(posRandomization),
                    0f,
                    350f,
                    0f,
                    1f + Mathf.random(missileLifetimeRandomization)
                )
            }
        }
    }
}
