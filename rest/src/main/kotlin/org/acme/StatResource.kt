package org.acme

import io.quarkus.security.Authenticated
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.jwt.JsonWebToken

@Path("/stats")
@Authenticated
class StatResource {

    @Inject
    lateinit var jwt: JsonWebToken

    @GET
    @Path("/{formId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun get(@PathParam("formId") formId: Long): FormStats {
        val email = jwt.name ?: throw WebApplicationException(Response.Status.UNAUTHORIZED)
        val form = Form.findById(formId) ?: throw WebApplicationException("Form not found", Response.Status.NOT_FOUND)

        // Security check: ensure form belongs to authenticated user
        if (form.user.email != email) {
            throw WebApplicationException(Response.Status.FORBIDDEN)
        }

        val stats = Stat.find("form", form).list()
        
        val sectionStats = form.sections.map { section ->
            val options = section.options.map { optionValue ->
                val count = stats.find { it.section.id == section.id && it.optionValue == optionValue }?.count ?: 0
                OptionStat(optionValue, count)
            }
            SectionStats(section.title, options)
        }

        return FormStats(form.title, form.description, sectionStats)
    }
}

data class FormStats(
    val title: String,
    val description: String,
    val sections: List<SectionStats>
)

data class SectionStats(
    val title: String,
    val options: List<OptionStat>
)

data class OptionStat(
    val value: String,
    val count: Long
)
