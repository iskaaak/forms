package org.acme

import io.quarkus.security.Authenticated
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.jwt.JsonWebToken

@Path("/forms")
@Authenticated
class FormResource {

    @Inject
    lateinit var jwt: JsonWebToken

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun list(): List<FormSummary> {
        val email = jwt.name ?: throw WebApplicationException(Response.Status.UNAUTHORIZED)
        val user = User.findByEmail(email) ?: throw WebApplicationException(Response.Status.UNAUTHORIZED)
        
        return Form.find("user", user).list().map { 
            FormSummary(it.id!!, it.title) 
        }
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun create(form: Form): Response {
        val email = jwt.name ?: throw WebApplicationException(Response.Status.UNAUTHORIZED)
        val user = User.findByEmail(email) ?: throw WebApplicationException(Response.Status.UNAUTHORIZED)

        form.user = user
        form.sections.forEach { it.form = form }
        
        form.persist()

        return Response.created(java.net.URI.create("/forms/${form.id}")).entity(form).build()
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun get(@PathParam("id") id: Long): Form {
        val form = Form.findById(id) ?: throw WebApplicationException("Form not found", Response.Status.NOT_FOUND)
        val email = jwt.name ?: throw WebApplicationException(Response.Status.UNAUTHORIZED)
        
        // Security check: ensure form belongs to authenticated user
        if (form.user.email != email) {
             throw WebApplicationException(Response.Status.FORBIDDEN)
        }
        
        return form
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun update(@PathParam("id") id: Long, updateDto: Form): Form {
        val existingForm = Form.findById(id) ?: throw WebApplicationException("Form not found", Response.Status.NOT_FOUND)
        val email = jwt.name ?: throw WebApplicationException(Response.Status.UNAUTHORIZED)

        // Security check: ensure form belongs to authenticated user
        if (existingForm.user.email != email) {
            throw WebApplicationException(Response.Status.FORBIDDEN)
        }

        existingForm.title = updateDto.title
        existingForm.description = updateDto.description

        // Update sections
        existingForm.sections.clear()
        existingForm.sections.addAll(updateDto.sections)
        existingForm.sections.forEach { it.form = existingForm }

        return existingForm
    }
}

data class FormSummary(
    val id: Long, 
    val title: String
)
