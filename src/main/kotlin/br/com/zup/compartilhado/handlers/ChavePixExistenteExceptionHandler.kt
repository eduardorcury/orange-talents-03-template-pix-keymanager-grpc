package br.com.zup.compartilhado.handlers

import br.com.zup.compartilhado.exceptions.ChavePixExistenteException
import com.google.rpc.Code
import com.google.rpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixExistenteExceptionHandler : ExceptionHandler<ChavePixExistenteException> {

    override fun handle(exception: ChavePixExistenteException) =
        Status.newBuilder()
            .setCode(Code.ALREADY_EXISTS_VALUE)
            .setMessage(exception.message)
            .build()

    override fun supports(exception: Exception) = exception is ChavePixExistenteException

}