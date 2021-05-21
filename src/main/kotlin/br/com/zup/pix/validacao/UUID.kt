package br.com.zup.pix.validacao

import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.constraints.Pattern
import kotlin.reflect.KClass

@Constraint(validatedBy = [])
@MustBeDocumented
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Pattern(regexp = "^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$",
        message = "ID informado não está no formato UUID")
annotation class UUID(
    val message: String = "ID informado não está no formato UUID",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)
