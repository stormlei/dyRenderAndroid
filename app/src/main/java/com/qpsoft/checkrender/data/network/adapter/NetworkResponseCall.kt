package com.qpsoft.checkrender.data.network.adapter

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.qpsoft.checkrender.data.model.SimpleError
import com.qpsoft.checkrender.utils.Convert
import okhttp3.Request
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import java.io.IOException

/**
 * [Call] interface implementation in order to make Retrofit return [NetworkResponse] when API call is triggered.
 */
internal class NetworkResponseCall<S : Any, E : Any>(
    private val delegate: Call<S>,
    private val errorConverter: Converter<ResponseBody, E>
) : Call<NetworkResponse<S, E>> {

    override fun enqueue(callback: Callback<NetworkResponse<S, E>>) {
        return delegate.enqueue(object : Callback<S> {
            override fun onResponse(call: Call<S>, response: Response<S>) {
                val body = response.body()
                val code = response.code()
                val error = response.errorBody()

                if (response.isSuccessful) {
                    if (body != null) {
                        // Response is successful
                        callback.onResponse(
                            this@NetworkResponseCall,
                            Response.success(NetworkResponse.Success(body))
                        )
                    } else {
                        // Response is successful but the body is null
                        callback.onResponse(
                            this@NetworkResponseCall,
                            Response.success(NetworkResponse.UnknownError(null))
                        )
                    }
                } else {
                    val errorBody = when {
                        error == null -> null
                        error.contentLength() == 0L -> null
                        else -> try {
                            errorConverter.convert(error)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (errorBody != null) {
                        // Response is not successful
                        callback.onResponse(
                            this@NetworkResponseCall,
                            Response.success(NetworkResponse.ApiError(errorBody, code))
                        )
                        LogUtils.e("------${errorBody}")
                        if (errorBody is SimpleError) ToastUtils.showShort("错误代码：" + errorBody.code + "，错误信息：" + errorBody.message)
                    } else {
                        // Response is not successful but the error body is null
                        val simpleError = SimpleError()
                        when(code) {
                            404-> {
                                simpleError.code = 404
                                simpleError.message = "page not found"
                            }
                            401-> {
                                simpleError.code = 401
                                simpleError.message = "token invalid, refresh again"
                            }
                        }
                        val eBody = errorConverter.convert(Convert.toJson(simpleError).toResponseBody())!!
                        callback.onResponse(
                            this@NetworkResponseCall,
                            Response.success(NetworkResponse.ApiError(eBody, code))
                        )
                        if (eBody is SimpleError) ToastUtils.showShort("错误代码：" + eBody.code + "，错误信息：" + eBody.message)
//                        callback.onResponse(
//                            this@NetworkResponseCall,
//                            Response.success(NetworkResponse.UnknownError(null))
//                        )
                    }
                }
            }

            override fun onFailure(call: Call<S>, throwable: Throwable) {
                val networkResponse = when (throwable) {
                    is IOException -> {
                        ToastUtils.showShort("请检查网络连接")
                        NetworkResponse.NetworkError(throwable)
                    }
                    else -> {
                        ToastUtils.showShort("服务器错误，请联系管理员")
                        NetworkResponse.UnknownError(throwable)
                    }
                }
                callback.onResponse(this@NetworkResponseCall, Response.success(networkResponse))
            }
        })
    }

    override fun isExecuted() = delegate.isExecuted

    override fun timeout(): Timeout = delegate.timeout()

    override fun clone() = NetworkResponseCall(delegate.clone(), errorConverter)

    override fun isCanceled() = delegate.isCanceled

    override fun cancel() = delegate.cancel()

    override fun execute(): Response<NetworkResponse<S, E>> {
        throw UnsupportedOperationException("NetworkResponseCall doesn't support execute")
    }

    override fun request(): Request = delegate.request()
}