package yellow.internal

import mindustry.Vars
import rhino.*
import yellow.util.YellowUtils.internalLog

object YellowConsoleBind{

    private val classes = arrayOf("yellow", "yellow.internal", "yellow.util", "yellow.ui", "yellow.ui.fragments", "yellow.content", "yellow.ai", "yellow.game", "yellow.input", "yellow.goodies.vn")

    @JvmStatic
    fun load(){
        internalLog("YELLOW: STARTING CONSOLE BIND")

        val consoleScope = Vars.mods.scripts.scope as ImporterTopLevel
        var current: NativeJavaPackage?

        classes.forEach {
            internalLog("cycling $it")
            current = NativeJavaPackage(it, Vars.mods.mainLoader())
            internalLog("$current, $consoleScope")

            current!!.parentScope = consoleScope

            consoleScope.importPackage(current)
            internalLog("cycled $it")
        }

        internalLog("YELLOW: CONSOLE BIND STARTED")
    }
}
