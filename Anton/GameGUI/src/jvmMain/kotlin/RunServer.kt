//import Client
import Server
import Engine
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import java.lang.Math.random
import java.util.concurrent.ThreadLocalRandom

@ExperimentalCoroutinesApi
suspend fun serverStart(scope : CoroutineScope) : SendChannel<ServerMsg>
{
    val serverActor = scope.serverActor()
    scope.launch {
        InfLoop@ while (true)
        {
            when
            {
                serverActor.isClosedForSend -> break@InfLoop
                else -> serverActor.send(Update)
            }
            delay(Config.updateTime)
        }
    }
    return serverActor
}

@ExperimentalCoroutinesApi
fun main()
{
    runBlocking {
        val serverActor = serverStart(this)
        withContext(Dispatchers.Default) {
            coroutineScope {
                launch {
                    val gui = GUI(serverActor)
                    gui.start()
                }
                repeat(3) {
                    launch {
                        delay((random() * 10000).toLong())
                        val client = Client(serverActor)
                        client.start()
                    }
                }
            }
        }
        serverActor.close()
    }
}