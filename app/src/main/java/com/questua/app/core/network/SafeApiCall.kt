package com.questua.app.core.network

import com.questua.app.core.common.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

abstract class SafeApiCall {
    suspend fun <T> safeApiCall(apiCall: suspend () -> retrofit2.Response<T>): Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiCall()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) return@withContext Resource.Success(body)
                    // Caso o corpo seja nulo mas o status seja sucesso (ex: 204 No Content)
                    return@withContext Resource.Success(Unit as T)
                }

                val errorBody = response.errorBody()?.string()
                val parsedMessage = try {
                    if (errorBody != null) {
                        val jsonObject = JSONObject(errorBody)
                        if (jsonObject.has("message")) {
                            jsonObject.getString("message")
                        } else if (jsonObject.has("error")) {
                            jsonObject.getString("error")
                        } else {
                            errorBody
                        }
                    } else {
                        "Erro desconhecido: ${response.code()}"
                    }
                } catch (e: Exception) {
                    errorBody ?: "Erro desconhecido: ${response.code()}"
                }

                Resource.Error(parsedMessage)
            } catch (e: Exception) {
                when (e) {
                    is IOException -> Resource.Error("Sem conexão com a internet. Verifique sua rede.")
                    is HttpException -> Resource.Error("Erro no servidor: ${e.message}")
                    else -> Resource.Error(e.localizedMessage ?: "Ocorreu um erro inesperado.")
                }
            }
        }
    }
}