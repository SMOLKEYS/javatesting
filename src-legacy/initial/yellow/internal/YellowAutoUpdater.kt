package yellow.internal

import arc.Core
import arc.util.*
import arc.util.serialization.JsonReader
import kotmindy.mindustry.ui.*
import mindustry.Vars
import yellow.*
import yellow.util.YellowUtils.getAndWrite
import yellow.util.YellowUtils.internalLog
import yellow.ui.YellowSettings

@Suppress("MemberVisibilityCanBePrivate")
object YellowAutoUpdater{
    val vtype = if(YellowVars.getSelf().meta.version.contains(".")) "release" else "nightly"
    val jsr = JsonReader()

    @JvmStatic
    fun start(){
        when(vtype){
            "release" -> {
                Http.get("https://api.github.com/repos/SMOLKEYS/yellow-java/releases", { response ->
                    val res = response.resultAsString

                    try{
                        val versionNet = jsr.parse(res)[0]["tag_name"].asString().replace("v", "")
                        val parsedNet = if(versionNet.toFloatOrNull() != null) versionNet.toFloat() else 0f
                        val versionLocal = YellowVars.getSelf().meta.version.replace("v", "")
                        val parsedLocal = if(versionLocal.toFloatOrNull() != null) versionLocal.toFloat() else 99999.99f

                        internalLog(versionNet)
                        internalLog("$parsedNet")

                        if(parsedNet > parsedLocal){
                            showTitledConfirm("@yellow.newrelease", Core.bundle.format("updatenow", parsedLocal, parsedNet)){
                                getAndWrite(YellowPermVars.sourceReleaseRepo, YellowSettings.tmpDir, true){ file ->
                                    Vars.mods.importMod(file)
                                    file.delete()
                                    showInfo("@modimported"){ Core.app.exit() }
                                }
                            }
                        }
                    }catch(e: Exception){
                        Log.err(e)
                    }
                }, {
                    Core.app.post{
                        it.printStackTrace()
                        Log.err(it)
                    }
                })
            }
            "nightly" -> internalLog("Using nightly Yellow build.")
            else -> {}
        }
    }
}
