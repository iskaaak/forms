package org.acme

import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.WebApplicationException
import io.quarkus.elytron.security.common.BcryptUtil
import java.net.URI

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UserResource {

    @GET
    fun list(): List<User> {
        return User.listAll()
    }

    @GET
    @Path("/{id}")
    fun get(@PathParam("id") id: Long): User {
        return User.findById(id) ?: throw WebApplicationException("User not found", Response.Status.NOT_FOUND)
    }

    @POST
    @Transactional
    fun create(@Valid user: User): Response {
        user.password = BcryptUtil.bcryptHash(user.password)
        user.persist()
        return Response.created(URI.create("/users/${user.id}")).entity(user).build()
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    fun delete(@PathParam("id") id: Long): Response {
        val deleted = User.deleteById(id)
        return if (deleted) {
            Response.noContent().build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }
    }
}
