package org.acme

import jakarta.transaction.Transactional
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/public")
class PublicFormResource {

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun get(@PathParam("id") id: Long): Form {
        return Form.findById(id) ?: throw WebApplicationException("Form not found", Response.Status.NOT_FOUND)
    }

    @POST
    @Path("/{id}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    fun submit(@PathParam("id") id: Long, submission: Submission): Response {
        val form = Form.findById(id) ?: throw WebApplicationException("Form not found", Response.Status.NOT_FOUND)

        submission.answers.forEach { answer ->
            val section = Section.findById(answer.sectionId)
            if (section != null && section.form.id == form.id) {
                answer.values.forEach { value ->
                    val existingStat: Stat? = Stat.find("section = ?1 and optionValue = ?2", section, value).firstResult()
                    
                    val stat = if (existingStat == null) {
                        val newStat = Stat()
                        newStat.form = form
                        newStat.section = section
                        newStat.optionValue = value
                        newStat.count = 0
                        newStat
                    } else {
                        existingStat
                    }
                    
                    stat.count++
                    stat.persist()
                }
            }
        }
        return Response.ok().build()
    }
}

data class Submission(val answers: List<Answer> = listOf())
data class Answer(val sectionId: Long = 0, val values: List<String> = listOf())
