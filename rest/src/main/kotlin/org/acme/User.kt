package org.acme

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

@Entity
@Table(name = "users")
class User : PanacheEntity() {
    companion object : PanacheCompanion<User> {
        fun findByEmail(email: String) = find("email", email).firstResult()
    }

    @field:NotBlank(message = "Email may not be blank")
    @field:Email(message = "Invalid email format")
    lateinit var email: String

    @field:NotBlank(message = "Password may not be blank")
    @field:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$",
        message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number"
    )
    @JsonIgnore
    lateinit var password: String

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var forms: MutableList<Form> = mutableListOf()
}
