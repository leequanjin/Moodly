package com.example.moodly

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class botpress{

    interface BotpressService {
        @Headers("Content-type: application/json")
        @POST("/v1/chat/users")
        suspend fun createNewUserAPI(
            @Header("Authorization") token: String,
            @Header("x-bot-id") botId: String,
            @Header("x-integration-id") integrationId: String,
            @Body request: CreateUserBody
        ): retrofit2.Response<Any>

        @Headers("Content-type: application/json")
        @POST("/v1/chat/conversations")
        suspend fun createConversationAPI(
            @Header("Authorization") token: String,
            @Header("x-bot-id") botId: String,
            @Header("x-integration-id") integrationId: String,
            @Body request: CreateConversationBody
        ): retrofit2.Response<Any>

        @Headers("Content-type: application/json")
        @POST("/v1/chat/messages")
        suspend fun createMessageAPI(
            @Header("Authorization") token: String,
            @Header("x-bot-id") botId: String,
            @Header("x-integration-id") integrationId: String,
            @Body request: CreateMessageBody
        ): retrofit2.Response<Any>

        @Headers("Content-type: application/json")
        @GET("/v1/chat/messages")
        suspend fun getMessageAPI(
            @Header("Authorization") token: String,
            @Header("x-bot-id") botId: String,
            @Header("x-integration-id") integrationId: String,
            @Query("conversationId") conversationId: String
        ): retrofit2.Response<Any>
    }

    //region Body content API

    data class CreateUserBody(
        val tags: Tags?=null,
        val name: String? = null,
        val pictureUrl: String? = null
    ){
        data class Tags(
            val id: String? = null
        )
    }

    data class CreateConversationBody(
        val tags: Tags?=null,
        val channel: String? = null
    ){
        data class Tags(
            val id: String? = null
        )
    }

    data class CreateMessageBody(
        val tags: Tags?=null,
        val userId: String? = null,
        val conversationId: String? = null,
        val payload: Payload? = null,
        val type: String? = null
    ){
        data class Tags(
            val id: String? = null
        )

        data class Payload(
            val text: String? = null
        )
    }

    //endregion

    object BotpressApiClient {
        private const val BASE_URL = "https://api.botpress.cloud/"

        fun create(): BotpressService {
            val client = OkHttpClient.Builder().build()
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(BotpressService::class.java)
        }
    }

    val token = "bp_pat_0Ww8rrRgxzMTQEqVqRr63h7y5LcJERA0UElY"
    val botId = "88b54088-7666-4570-8c5f-e548160ca9ad"
    val integrationId = "87b01760-ede8-49d5-afc6-6afc0d0d1bdb"

    var userID: Any? = null
    var conversationID: Any? = null

    var conversation: ArrayList<conversationData> = arrayListOf()
    var conversationTimeRecord: ArrayList<conversationTime> = arrayListOf()

    var username = ""

    data class conversationData(
        var msg: String,
        val role: String,
        val msgID: String
    )

    data class conversationTime(
        val msgID: String,
        val time: LocalDateTime
    )

    suspend fun createNewUser(){
        val botpressService = BotpressApiClient.create()

        try {
            val request = CreateUserBody(
                tags = CreateUserBody.Tags()
            )
            val response = botpressService.createNewUserAPI("Bearer $token", botId, integrationId, request)
            val responseBody = response.body()

            if (responseBody != null && responseBody is Map<*, *>) {
                val user = responseBody["user"] as? Map<*, *>
                if (user != null) {
                    userID = user["id"]

                    println("User ID: $userID")
                } else {
                    println("Error: User information not found in the response")
                }
            } else {
                println("Error: Invalid response body")
            }
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }

    suspend fun createConversation(){
        val botpressService = BotpressApiClient.create()

        try {
            val request = CreateConversationBody(
                tags = CreateConversationBody.Tags(),
                channel = "channel"
            )
            val response = botpressService.createConversationAPI("Bearer $token", botId, integrationId, request)
            val responseBody = response.body()

            if (responseBody != null && responseBody is Map<*, *>) {
                val conversation = responseBody["conversation"] as? Map<*, *>
                if (conversation != null) {
                    conversationID = conversation["id"]

                    println("Conversation ID: $conversationID")
                } else {
                    println("Error: User information not found in the response")
                }
            } else {
                println("Error: Invalid response body")
            }
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }

    suspend fun createMessage(_msg: String){
        val botpressService = BotpressApiClient.create()

        try {
            val request = CreateMessageBody(
                tags = CreateMessageBody.Tags(),
                userId = userID.toString(),
                conversationId = conversationID.toString(),
                payload = CreateMessageBody.Payload(
                    text = _msg
                ),
                type = "text"
            )
            val response = botpressService.createMessageAPI("Bearer $token", botId, integrationId, request)
            val responseBody = response.body()

            if (response.isSuccessful) {
                //println("Conversation started: ${response.body()}") // Use response.body() to get the response body

                if (responseBody != null && responseBody is Map<*, *>) {
                    val msg = responseBody["message"] as? Map<*, *>
                    if (msg != null && _msg != username) {
                        println("Message created: ${msg["id"]}")
                        conversation.add(conversationData(_msg, "user", msg["id"].toString()))
                        conversationTimeRecord.add(conversationTime(msg["id"].toString(), LocalDateTime.now()))
                    } else {
                        println("Error: User information not found in the response")
                    }
                } else {
                    println("Error: Invalid response body")
                }

            } else {
                println("Error: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }

    suspend fun getMessage(): Boolean{
        val botpressService = BotpressApiClient.create()

        try {
            val response = botpressService.getMessageAPI("Bearer $token", botId, integrationId, conversationID.toString())
            //val responseBody = response.body()

            if (response.isSuccessful) {

                extractMessageResponse(response.body())

                val lastestMsg = conversation.last().msg

                //if string contains waitinginput, return false
                return !(lastestMsg.contains("\n!!!WaitingInput!!!") || lastestMsg.contains("\n" +
                        "!!!SessionEnd!!!"))
            } else {
                println("Error: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }

        return false
    }

    //extract response and save the data thats with none exists id
    fun extractMessageResponse(_response: Any?){
        if (_response is Map<*, *>) {
            val messages = _response["messages"] as? List<*>

            val temp = arrayListOf<conversationData>()

            if (messages != null) {
                for (message in messages.reversed()){
                    val msgData = message as? Map<*, *>

                    val currMsgID = msgData?.get("id").toString()

                    val payload = msgData?.get("payload") as? Map<*, *>
                    val botReply = payload?.get("text").toString()

                    val currConData = conversationData(botReply, "bot", currMsgID)
                    val currConDataUser = conversationData(botReply, "user", currMsgID)

                    if(!conversation.contains(currConData) && !conversation.contains(currConDataUser) && botReply != username){
                        temp.add(currConData)
                        conversationTimeRecord.add(conversationTime(currMsgID, LocalDateTime.now()))

                        println("bot: $botReply")
                    }
                }

                conversation.addAll(temp)
            }
        }
    }

    fun displayConversationList(){
        println("\n-----Display Conversation-----")

        for (con in conversation){
            println("MessageID: "+ con.msgID)
            println("Message: " + con.msg)
            println("Role: " + con.role)

            for(timeRecord in conversationTimeRecord){
                if(timeRecord.msgID == con.msgID){
                    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
                    println("Time: ${timeRecord.time.format(formatter)}")
                }
            }

            println()
        }

        println("-----End-----")
    }
}