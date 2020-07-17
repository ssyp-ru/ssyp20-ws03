import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap

class BorovServer() {
    private val clients = HashMap<String, String>()
    private val engine = BorovEngine(512.0, 512.0)
    val channel = Channel<BorovMessage>()
    init {
        GlobalScope.launch {
            while(true) {
                engine.tick()
//                engine.debug()
                delay(100)
            }
        }
        GlobalScope.launch {
            channel.consumeEach {
                when (it) {
                    is BorovMessageRegister -> {
                        val id = UUID.randomUUID().toString()
                        val player = engine.registerPlayer()
                        clients[id] = player.uuid
                        it.callback.complete(id)
                    }
                    is BorovMessageMap -> {
                        val map = engine.getMap()
                        it.callback.complete(map)
                    }
                    is BorovMessageDirection -> {
                        if(!clients.containsKey(it.id)) it.callback.complete(false)
                        val success = engine.setPlayerDirection(clients[it.id]!!, it.angle)
                        it.callback.complete(success)
                    }
                    is BorovMessageInfo -> {
                        if(!clients.containsKey(it.id)) it.callback.complete(null)
                        else it.callback.complete(engine.getPlayer(clients[it.id]!!))
                    }
                }
            }
        }
    }
}