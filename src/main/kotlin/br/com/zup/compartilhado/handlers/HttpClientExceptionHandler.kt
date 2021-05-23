package br.com.zup.compartilhado.handlers

import br.com.zup.ValidationErrors
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.google.protobuf.Any
import com.google.rpc.Code
import com.google.rpc.Status
import io.micronaut.http.client.exceptions.HttpClientException
import javax.inject.Singleton

@Singleton
class HttpClientExceptionHandler : ExceptionHandler<HttpClientException> {

    override fun handle(exception: HttpClientException): Status {

        val mapper = XmlMapper()

        try {
            val problem: Problem = mapper.readValue(exception.message, Problem::class.java)
            with(problem) {
                if (violations == null) {
                    return Status.newBuilder()
                        .setCode(Code.ALREADY_EXISTS_VALUE)
                        .setMessage("Erro de tipo $type, $status. $title: $detail.")
                        .build()
                }
                return Status.newBuilder()
                    .setCode(Code.INTERNAL_VALUE)
                    .setMessage("Erro de tipo $type, $status. $title: $detail. " +
                          "${violations.map { "${it.field}: ${it.message}" }}")
                    .addDetails(Any.pack(ValidationErrors
                        .newBuilder()
                        .addAllMessage(violations.map { "${it.field}: ${it.message}" })
                        .build()))
                    .build()
            }

        } catch (e: JsonProcessingException) {
            return Status
                .newBuilder()
                .setMessage(exception.message)
                .setCode(Code.INTERNAL_VALUE)
                .build()
        }
    }


    override fun supports(exception: Exception) = exception is HttpClientException

}

data class Problem(
    @JsonProperty("type")
    val type: String,
    @JsonProperty("status")
    val status: String,
    @JsonProperty("title")
    val title: String,
    @JsonProperty("detail")
    val detail: String,
    @JsonProperty("violations")
    val violations: List<Violation>?
)

data class Violation(
    @JsonProperty("field")
    val field: String,
    @JsonProperty("message")
    val message: String
)