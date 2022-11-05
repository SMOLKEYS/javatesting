package yellow.ui.buttons.dialogs

import arc.*
import arc.scene.ui.*
import com.github.mnemotechnician.mkui.extensions.dsl.*
import mindustry.game.*
import mindustry.gen.*
import mindustry.graphics.*
import mindustry.ui.dialogs.*
import yellow.internal.util.*
import yellow.internal.util.YellowUtils.*
import yellow.type.*

open class FoodDialog : BaseDialog("Food"){

    init{
        this.addCloseButton()
    }


    fun show(team: Team): Dialog{
        cont.clear()

        cont.scrollPane{ scr: ScrollPane ->
            YellowUtilsKt.seperator(this, Core.graphics.width.toFloat(), 4f, Pal.accent)
            this.row()
            Groups.unit.each{ unit ->
                if(unit.team == team){
                    foodOpts(this, unit, {}){ itt ->
                        table(itt, {
                            it.grow()
                        }, { table ->
                            table.pane{ pan ->
                                FoodItem.instances.each{
                                    pan.button("${it.nameShort}\n${typiis(it)}"){
                                        if(it.hasThis(team)) it.consume(unit, team)
                                    }.get().label.setWrap(false)
                                    pan.row()
                                }
                            }.grow()
                        })
                    }
                    this.row()
                    YellowUtilsKt.seperator(this, Core.graphics.width.toFloat(), 4f, Pal.accent)
                    this.row()
                }
            }
            scr.width = Core.graphics.width.toFloat()
        }.size(Core.graphics.width.toFloat(), Core.graphics.height.toFloat() / 1.15f)


        return super.show()
    }
}
