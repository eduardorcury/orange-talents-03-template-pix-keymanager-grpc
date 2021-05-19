package br.com.zup.pix.enums

import br.com.caelum.stella.validation.CPFValidator
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import io.micronaut.validation.validator.constraints.EmailValidator
import javax.validation.constraints.Email

enum class TipoDeChave {

    CPF {
        override fun chaveValida(valor: String?, context: ConstraintValidatorContext?): Boolean {
            if (valor == null) {
                return false
            }
            return CPFValidator().invalidMessagesFor(valor).isEmpty()
        }
        override val nomeBCB: String = "CPF"

    },
    TELEFONE {
        val regex = Regex("^\\+[1-9][0-9]\\d{1,14}\$")
        override fun chaveValida(valor: String?, context: ConstraintValidatorContext?): Boolean {
            if (valor == null) {
                return false
            }
            return valor.matches(regex)
        }
        override val nomeBCB: String = "PHONE"

    },
    EMAIL {
        override fun chaveValida(valor: String?, context: ConstraintValidatorContext?): Boolean {
            if (valor == null) {
                return false
            }
            return EmailValidator().isValid(valor, AnnotationValue("email"), context)
        }
        override val nomeBCB: String = "EMAIL"

    },
    ALEATORIA {
        override fun chaveValida(valor: String?, context: ConstraintValidatorContext?): Boolean = true
        override val nomeBCB: String = "RANDOM"
    };

    abstract fun chaveValida(valor: String?, context: ConstraintValidatorContext?): Boolean

    abstract val nomeBCB: String

    companion object {
        fun fromBcb(valor: String): TipoDeChave {
            return when(valor) {
                "CPF" -> CPF
                "PHONE" -> TELEFONE
                "EMAIL" -> EMAIL
                "RANDOM" -> ALEATORIA
                else -> throw IllegalStateException("Chave de tipo $valor não suportada")
            }
        }
    }

}