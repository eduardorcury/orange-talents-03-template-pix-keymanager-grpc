package br.com.zup.pix.cadastro

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Constraint(validatedBy = [ChavePixValidator::class])
@Retention(AnnotationRetention.RUNTIME)
annotation class ChavePixValida(
    val message: String = "Tipo de chave\${ validatedValue.tipoDeChave} inválido",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

