package org.acme

import com.fasterxml.jackson.annotation.JsonIgnore
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "sections")
class Section : PanacheEntity() {
    companion object : PanacheCompanion<Section>

    lateinit var title: String

    @Enumerated(EnumType.STRING)
    lateinit var type: SectionType

    var isRequired: Boolean = false

    @ElementCollection(fetch = FetchType.EAGER)
    var options: MutableList<String> = mutableListOf()

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id")
    lateinit var form: Form
}
