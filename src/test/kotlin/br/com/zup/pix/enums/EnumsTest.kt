package br.com.zup.pix.enums

import br.com.zup.pix.enums.TipoDeChave.*
import br.com.zup.pix.enums.TipoDeConta.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource
import java.lang.IllegalStateException
import java.util.stream.Stream

internal class EnumsTest {

    @Nested
    inner class nestedCPF {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings =  ["12345", "040.449.800-04"] )
        internal fun `testa valores de cpf invalidos`(cpf: String?) {
            val resultado = CPF.chaveValida(cpf, null)
            assertFalse(resultado)
        }

        @Test
        internal fun `deve retornar valido para CPF valido`() {
            val resultado = CPF.chaveValida("04044980004", null)
            assertTrue(resultado)
        }

    }

    @Nested
    inner class nestedTELEFONE {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings =  ["12345"] )
        internal fun `testa valores de telefone invalidos`(telefone: String?) {
            val resultado = TELEFONE.chaveValida(telefone, null)
            assertFalse(resultado)
        }

        @Test
        internal fun `deve retornar valido para telefone valido`() {
            val resultado = TELEFONE.chaveValida("+5585988714077", null)
            assertTrue(resultado)
        }

    }

    @Nested
    inner class nestedEMAIL {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings =  ["email"] )
        internal fun `testa valores de email invalidos`(email: String?) {
            val resultado = EMAIL.chaveValida(email, null)
            assertFalse(resultado)
        }

        @ParameterizedTest
        @ValueSource(strings =  ["email@gmail.com", "email@outlook.com", "email@usp.com.br"] )
        internal fun `deve retornar valido para email valido`(email: String?) {
            val resultado = EMAIL.chaveValida(email, null)
            assertTrue(resultado)
        }
    }

    @ParameterizedTest
    @MethodSource("geraTiposDeChaveBCB")
    internal fun `deve converter valor de chave BCB para enum`(valor: String, resultado: TipoDeChave) {
        val tipoDeChave = TipoDeChave.fromBcb(valor)
        assertEquals(resultado, tipoDeChave)
    }

    @ParameterizedTest
    @MethodSource("geraTiposDeContaBCB")
    internal fun `deve converter valor de conta BCB para enum`(valor: String, resultado: TipoDeConta) {
        val tipoDeConta = TipoDeConta.fromBcb(valor)
        assertEquals(resultado, tipoDeConta)
    }

    @Test
    internal fun `deve lancar excecao se BCB retornar chave desconhecida`() {

        assertThrows<IllegalStateException> {
            TipoDeChave.fromBcb("DESCONHECIDO")
        }.let { illegalStateException ->
            assertEquals("Chave de tipo DESCONHECIDO não suportada",
                illegalStateException.message)
        }

    }

    @Test
    internal fun `deve lancar excecao se BCB retornar conta desconhecida`() {

        assertThrows<IllegalStateException> {
            TipoDeConta.fromBcb("DESCONHECIDO")
        }.let { illegalStateException ->
            assertEquals("Tipo de conta DESCONHECIDO não suportado",
                illegalStateException.message)
        }

    }

    companion object {
        @JvmStatic
        fun geraTiposDeChaveBCB(): Stream<Arguments> = Stream.of(
            Arguments.of("CPF", CPF),
            Arguments.of("PHONE", TELEFONE),
            Arguments.of("EMAIL", EMAIL),
            Arguments.of("RANDOM", ALEATORIA),
        )
        @JvmStatic
        fun geraTiposDeContaBCB(): Stream<Arguments> = Stream.of(
            Arguments.of("CACC", CONTA_CORRENTE),
            Arguments.of("SVGS", CONTA_POUPANCA)
        )
    }

}