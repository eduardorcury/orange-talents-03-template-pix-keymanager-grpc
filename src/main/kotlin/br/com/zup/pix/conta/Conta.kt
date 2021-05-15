package br.com.zup.pix.conta

import br.com.zup.pix.enums.TipoDeConta
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Embeddable
class Conta(

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tipo: TipoDeConta,

    @Column(nullable = false)
    val instituicao: String,

    @Column(nullable = false)
    val agencia: String,

    @Column(nullable = false)
    val numero: String,

    @Column(nullable = false)
    val titular: String

) {

}
