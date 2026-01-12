package org.acme

import io.quarkus.elytron.security.common.BcryptUtil
import io.smallrye.jwt.build.Jwt
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.jwt.Claims

import io.quarkus.security.Authenticated
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import org.eclipse.microprofile.jwt.JsonWebToken

@Path("/auth")
class AuthResource {

    @Inject
    lateinit var jwt: JsonWebToken

    @GET
    @Path("/check")
    @Authenticated
    @Produces(MediaType.TEXT_PLAIN)
    fun check(): String {
        return "Token is valid for user: ${jwt.name}"
    }

    @POST
    @Path("/login")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    fun login(request: LoginRequest): Response {
        val user = User.findByEmail(request.email)
        if (user == null || !BcryptUtil.matches(request.password, user.password)) {
            return Response.status(Response.Status.UNAUTHORIZED).build()
        }

        val token = Jwt.issuer("https://example.com/issuer")
            .upn(user.email)
            .groups(setOf("User"))
            .claim(Claims.email.name, user.email)
            .sign()

        return Response.ok(token).build()
    }
}

data class LoginRequest(
    val email: String = "",
    val password: String = ""
)
