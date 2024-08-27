package com.streamefy.network

sealed class MyResource<T>(var data: T? = null, var error: String = "") {
    class isLoading<T> : MyResource<T>()
    class isSuccess<T>( mData: T? = null) : MyResource<T>(data = mData)
    class isError<T>( mError: String) : MyResource<T>(error = mError)
}