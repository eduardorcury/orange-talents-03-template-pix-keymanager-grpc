package br.com.zup.compartilhado.handlers

import br.com.zup.compartilhado.exceptions.PermissaoNegadaException
import com.google.rpc.Code
import com.google.rpc.Status
import javax.inject.Singleton

@Singleton
class PermissaoNegadaExceptionHandler : ExceptionHandler<PermissaoNegadaException> {

    override fun handle(exception: PermissaoNegadaException): Status =
        Status
            .newBuilder()
            .setCode(Code.PERMISSION_DENIED_VALUE)
            .setMessage(exception.message)
            .build()

    override fun supports(exception: Exception): Boolean =
        exception is PermissaoNegadaException

}