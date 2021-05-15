package br.com.zup.pix

import br.com.zup.pix.conta.Conta
import br.com.zup.pix.enums.TipoDeChave
import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import javax.persistence.*

@Entity
class ChavePix(

    @Column(nullable = false)
    val idTitular: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tipoDeChave: TipoDeChave,

    @Column(unique = true, nullable = false)
    val valor: String,

    @Embedded
    val conta: Conta

) {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    var id: String? = null;

    val criadaEm: LocalDateTime = LocalDateTime.now()

}