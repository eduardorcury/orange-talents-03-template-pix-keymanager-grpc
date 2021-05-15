package br.com.zup.pix.cadastro

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton

@Singleton
class ChavePixValidator : ConstraintValidator<ChavePixValida, NovaChavePix> {

    override fun isValid(
        novaChave: NovaChavePix,
        annotationMetadata: AnnotationValue<ChavePixValida>,
        context: ConstraintValidatorContext,
    ): Boolean {

        if (novaChave.tipoDeChave == null) {
            return false
        }
        return novaChave.tipoDeChave.chaveValida(novaChave.valor, context)

    }

}