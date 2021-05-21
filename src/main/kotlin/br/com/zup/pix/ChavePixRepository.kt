package br.com.zup.pix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, String> {

    fun existsByValor(valor: String): Boolean

    fun findByValor(valor: String): Optional<ChavePix>

    fun findByIdTitular(clienteId: String): List<ChavePix>

}