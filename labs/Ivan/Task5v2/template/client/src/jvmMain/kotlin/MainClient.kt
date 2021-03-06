import com.soywiz.korge.Korge
import com.soywiz.korge.view.Circle
import com.soywiz.korge.view.addUpdater
import com.soywiz.korge.view.solidRect
import com.soywiz.korge.view.xy
import com.soywiz.korim.color.Colors
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.collections.set

@KtorExperimentalAPI
suspend fun main() = Korge(virtualWidth = 640, virtualHeight = 480, bgcolor = Colors.BLACK) {
    solidRect(640, 480, Colors.WHITE)

    val futureUser = CompletableDeferred<Client>()
    launch {
        val user = Client()
        user.start()
        futureUser.complete(user)
    }
    val user = futureUser.await()

    val idToView = hashMapOf<Long, Circle>()

    fun drawPlayer(player: Player) {
        if (!idToView.containsKey(player.id)) {
            idToView[player.id] = Circle(10.0, Colors.DARKBLUE)
        }
        val c = idToView[player.id]!!
        c.xy(player.pos.x, player.pos.y)
        addChild(c)
    }

    launch {
        while (true) {
            delay(16)
            user.changeDirection(Dot(mouseX, mouseY))
        }
    }


    launch {
        while (true) {
            delay(16)
            val ps = user.getPlayers()
            for (p in ps) {
                drawPlayer(p.component2())
            }
        }
    }
}