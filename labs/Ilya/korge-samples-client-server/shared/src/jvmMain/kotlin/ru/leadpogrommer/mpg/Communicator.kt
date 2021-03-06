package ru.leadpogrommer.mpg

import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import java.io.*

class Communicator(s: Socket) {
    private val inputChannel = Channel<Request>(Channel.Factory.UNLIMITED)
    private val outputChannel = Channel<ByteArray>(Channel.Factory.UNLIMITED)

    private val inStream = s.openReadChannel()
    private val outStream = s.openWriteChannel()

    private var gson: Gson

    private var connected = true
    init {
        val shit = RuntimeTypeAdapterFactory.of(Request::class.java, "__type__")
        for(c in Request::class.sealedSubclasses){
            println("Registered class ${c.simpleName}")
            shit.registerSubtype(c.java, c.simpleName)
        }
        gson = GsonBuilder().registerTypeAdapterFactory(shit).create()
    }

    fun run(){
        GlobalScope.launch(Dispatchers.IO) {
            try {
                while (connected){
                    val ba = outputChannel.receive()
                    outStream.writeInt(ba.size)
                    outStream.writeFully(ba, 0, ba.size)
                    outStream.flush()
                }
            }catch (e: Throwable){
                connected = false
                inputChannel.close()
            }

        }

        GlobalScope.launch(Dispatchers.IO){
            while (connected){
                try{

                    val size = inStream.readInt()
                    val ser = ByteArray(size)
                    inStream.readFully(ser, 0, size)
                    val req: Request = gson.fromJson(ser.toString(charset = Charsets.UTF_8), Request::class.java)
                    println(ser.toString(Charsets.UTF_8))
//                    val ois = ObjectInputStream(ser.inputStream())
//                    val req = ois.readObject() as Request
                    inputChannel.send(req)
                }catch (e: Throwable){
                    connected = false
                    inputChannel.close()
                }
            }
        }
    }


    suspend fun sendRequest(req: Request){
        req.__type__ = req::class.simpleName!!
        val sered = gson.toJson(req)
        val ba = sered.toString().toByteArray()
        outputChannel.send(ba)
//        val buf = ByteArrayOutputStream()
//        val ois = ObjectOutputStream(buf)
//        ois.writeObject(req)
//        outputChannel.send(buf.toByteArray())

    }

    fun getRequests():ReceiveChannel<Request>{
        return inputChannel
    }


}