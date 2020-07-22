package engine

import engine.managers.DamageManager
import engine.managers.PositionsManager
import engine.managers.TimersManager
import shared.BOOM
import shared.Bullet
import shared.Entity
import shared.Player


class Engine {
    private val positionsManager = PositionsManager()
    private val timersManager = TimersManager()
    private val damageManager = DamageManager()
    private val listOfPlayers = mutableMapOf<Int, Player>()

    //TODO: WRITE TEAM CHOOSER
    private var teamCounter = 0

    fun registerPlayer(nick: String): Player {
        val player = Player(nick, Configuration.healthOfPlayer)
        player.team = teamCounter++
        println("id: ${player.id}")
        listOfPlayers[player.id] = player
        positionsManager.register(player)
        timersManager.register(player)
        damageManager.register(player, player.team)
        return player
    }

    fun removePlayer(player: Player) {
        val boom = BOOM()
        boom.x = player.x
        boom.y = player.y
        positionsManager.register(boom)
        timersManager.register(boom)

        println("BOOM")

        listOfPlayers.remove(player.id)
        positionsManager.removeEntity(player)
        timersManager.remove(player)
    }

    fun tick(){
        val deads = damageManager.processCollisions(positionsManager.moveAll()?.toTypedArray())
        if (deads != null){
            for (ent in deads){
                timersManager.haveDead(ent)
                positionsManager.removeEntity(ent)
            }
        }
        for(ent in positionsManager.getEntities()){
            if((ent is BOOM) && (timersManager.checkBoomTimer(ent))){
                positionsManager.removeEntity(ent)
                timersManager.remove(ent)
            }
        }
        timersManager.tick()
    }

    fun getEntities(player: Entity): Array<Entity> {
        // All visible entities (based on VisibilityManager)
        return positionsManager.getEntities()
    }

    fun setAngle(entity: Entity, angle: Double) {
        listOfPlayers[entity.id]!!.angle = angle
    }

    fun shot(player: Player) {
        // Creates bullet (based on cooldown)
        if (timersManager.checkCooldownTimer(player)){
            println("Shot in engine")
            val bullet = Bullet(player.team)
            bullet.x = player.x
            bullet.y = player.y
            bullet.angle = player.angle
            positionsManager.register(bullet)
            timersManager.haveShooted(player)
            damageManager.register(bullet, bullet.team)
            println("Registered in dmg manager")
        }
    }

    fun setFriendlyFire(ff: Boolean?){
        damageManager.friendlyFire = ff
    }
}