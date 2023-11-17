import io.ktor.client.engine.cio.*
import io.ktor.client.statement.*
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.get
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

public var finalJoke: String? = "Nebyl žádný řečen D:"

suspend fun main() {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

    }

    val url = "https://v2.jokeapi.dev/joke/Any?lang=en"
    val response: HttpResponse = client.get(url) {
        method = HttpMethod.Get
    }

    @Serializable
    data class Joke(
        val error: Boolean,
        val category: String,
        val type: String,
        val joke: String? = null,
        val setup: String? = null,
        val delivery: String? = null,
        val id: Int,
        val safe: Boolean,
        val lang: String,
    )

    val token = "DISCORD_TOKEN"

    JDABuilder.createDefault(token)
        .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
        .addEventListeners(object : ListenerAdapter() {
            override fun onMessageReceived(event: MessageReceivedEvent) {
                if (event.author.isBot) return // Ignore messages from other bots

                if (event.message.contentRaw.equals("!joke", ignoreCase = true)) {


                    for (i in 1..5)
                    {
                        var joke = runBlocking { client.get(url).body<Joke>() }

                        if (joke.safe == true)
                        {
                            val jokeMessage = joke.joke ?: run {
                                if (joke.setup != null && joke.delivery != null) {
                                    "${joke.setup} \n \n ${joke.delivery}"
                                }
                                else
                                { " "}
                            }

                            event.channel.sendMessage(jokeMessage).queue()

                            return
                        }
                    }

                    event.channel.sendMessage("Je to v háji, nenašel jsem žádnej vtip..").queue()
                }
            }
        })
        .build()
        .awaitReady()


}