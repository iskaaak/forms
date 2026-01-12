package org.acme

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "stats")
class Stat : PanacheEntity() {
    companion object : PanacheCompanion<Stat> {
        fun findBySectionAndOption(section: Section, option: String): Stat? {
            return find("section = ?1 and optionValue = ?2", section, option).firstResult()
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    lateinit var section: Section

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id")
    lateinit var form: Form

    lateinit var optionValue: String
    
    var count: Long = 0
}
