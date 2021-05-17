package br.com.zup.compartilhado.handlers

import br.com.zup.ValidationErrors
import com.google.protobuf.Any
import com.google.rpc.Code
import com.google.rpc.Status
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class ValidationExceptionHandler : ExceptionHandler<ConstraintViolationException> {

    override fun handle(exception: ConstraintViolationException) =
        Status.newBuilder()
            .setCode(Code.INVALID_ARGUMENT_VALUE)
            .setMessage("Dados inválidos: ${exception.constraintViolations.map{ it.message }}")
            .addDetails(Any.pack(ValidationErrors
                .newBuilder()
                .addAllMessage(exception.constraintViolations.map{ it.message })
                .build()))
            .build()

    override fun supports(exception: Exception): Boolean {
        return exception is ConstraintViolationException
    }

}