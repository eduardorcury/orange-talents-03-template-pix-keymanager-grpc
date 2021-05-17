package br.com.zup.compartilhado.handlers

import com.google.rpc.Status

interface ExceptionHandler<T: Throwable> {

    fun handle(exception: T): Status

    fun supports(exception: Exception): Boolean

}