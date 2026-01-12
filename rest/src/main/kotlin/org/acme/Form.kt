package org.acme

import com.fasterxml.jackson.annotation.JsonIgnore
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "forms")
class Form : PanacheEntity() {
    companion object : PanacheCompanion<Form>

    lateinit var title: String
    lateinit var description: String

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    lateinit var user: User

    @OneToMany(mappedBy = "form", cascade = [CascadeType.ALL], orphanRemoval = true)
    var sections: MutableList<Section> = mutableListOf()
}
