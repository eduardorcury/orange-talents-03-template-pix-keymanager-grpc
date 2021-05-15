package br.com.zup.pix.enums

import br.com.caelum.stella.validation.CPFValidator
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import io.micronaut.validation.validator.constraints.EmailValidator
import javax.validation.constraints.Email

enum class TipoDeChave {

    CPF {
        override fun chaveValida(valor: String?, context: ConstraintValidatorContext): Boolean {
            if (valor == null) {
                return false
            }
            return CPFValidator().invalidMessagesFor(valor).isEmpty()
        }

    },
    TELEFONE {
        val regex = Regex("^\\+[1-9][0-9]\\d{1,14}\$")
        override fun chaveValida(valor: String?, context: ConstraintValidatorContext): Boolean {
            if (valor == null) {
                return false
            }
            return valor.matches(regex)
        }

    },
    EMAIL {
        override fun chaveValida(valor: String?, context: ConstraintValidatorContext): Boolean {
            if (valor == null) {
                return false
            }
            return EmailValidator().isValid(valor, AnnotationValue("email"),context)
        }

    },
    ALEATORIA {
        override fun chaveValida(valor: String?, context: ConstraintValidatorContext): Boolean = true
    };

    abstract fun chaveValida(valor: String?, context: ConstraintValidatorContext): Boolean

}