package yellow.maps.planet

import arc.graphics.*
import arc.math.*
import arc.math.geom.*
import arc.util.*
import arc.util.noise.*
import mindustry.content.Blocks.*
import mindustry.maps.generators.*
import mindustry.world.*

open class AzenoPlanetGenerator : PlanetGenerator(){

    var scl = 4.2f
    var waterOffset = 0.13f
    private var v34 = Vec3()
    private var v35 = Vec3()
    private var csus = Color()

    var arr = arrayOf(
        arrayOf(stone, dirt, sand, water, sand, dirt, stone)
    )

    var watera: Float = 2f / arr.get(0).size

    override fun getHeight(position: Vec3): Float{
        return rawHeight(position).coerceAtLeast(watera)
    }

    override fun getColor(position: Vec3): Color{
        val block = getBlock(position)
        @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
        return if (block === salt) sand.mapColor else csus.set(block!!.mapColor).a(1f - block!!.albedo)
    }

    fun getBlock(position: Vec3): Block? {
        var tposition = position
        var height = rawHeight(tposition)
        Tmp.v31.set(tposition)
        tposition = v35.set(position).scl(scl)
        val rad = scl
        var temp = Mathf.clamp(Math.abs(tposition.y * 2f) / rad)
        val tnoise = Simplex.noise3d(
            seed,
            6.65,
            0.58,
            (1f / 3f).toDouble(),
            tposition.x.toDouble(),
            (tposition.y + 999f).toDouble(),
            tposition.z.toDouble()
        )
        temp = Mathf.lerp(temp, tnoise, 0.5f)
        height *= 1.3f
        height = Mathf.clamp(height)
        return arr[Mathf.clamp((temp * arr.size).toInt(), 0, arr[0].size - 1)][Mathf.clamp(
            (height * arr[0].size).toInt(),
            0,
            arr[0].size - 1
        )]
    }
    
    fun rawHeight(position: Vec3): Float {
        val tposition = v34.set(position).scl(scl)
        return (Mathf.pow(
            Simplex.noise3d(
                seed,
                6.65,
                0.5,
                (1f / 3f).toDouble(),
                tposition.x.toDouble(),
                tposition.y.toDouble(),
                tposition.z.toDouble()
            ), 2.3f
        ) + waterOffset) / (1f + waterOffset)
    }
}
