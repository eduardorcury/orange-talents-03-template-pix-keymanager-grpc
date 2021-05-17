package br.com.zup.compartilhado.handlers

import br.com.zup.compartilhado.exceptions.RecursoNaoEncontradoException
import com.google.rpc.Code
import com.google.rpc.Status
import javax.inject.Singleton

@Singleton
class RecursoNaoEncontradoExceptionHandler : ExceptionHandler<RecursoNaoEncontradoException> {

    override fun handle(exception: RecursoNaoEncontradoException) =
        Status
            .newBuilder()
            .setCode(Code.NOT_FOUND_VALUE)
            .setMessage(exception.message)
            .build()

    override fun supports(exception: Exception) = exception is RecursoNaoEncontradoException
}