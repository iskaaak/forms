package org.acme

import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.transaction.Transactional

@ApplicationScoped
class DataSeeder {

    @Transactional
    fun onStart(@Observes ev: StartupEvent) {
        if (User.count() > 0) {
            return
        }

        val user = User()
        user.email = "test@example.com"
        user.password = BcryptUtil.bcryptHash("Password123") // Satisfies complexity requirements

        // Form 1
        val form1 = Form()
        form1.title = "Customer Feedback"
        form1.description = "We value your feedback"
        form1.user = user

        val section1Radio = Section()
        section1Radio.title = "Rate our service"
        section1Radio.type = SectionType.RADIO_BOX
        section1Radio.isRequired = true
        section1Radio.options.addAll(listOf("Excellent", "Good", "Poor"))
        section1Radio.form = form1

        val section1Check = Section()
        section1Check.title = "What features do you use?"
        section1Check.type = SectionType.CHECK_BOX
        section1Check.options.addAll(listOf("Search", "history", "Profile"))
        section1Check.form = form1

        form1.sections.add(section1Radio)
        form1.sections.add(section1Check)

        // Form 2
        val form2 = Form()
        form2.title = "Event Registration"
        form2.description = "Register for the upcoming event"
        form2.user = user

        val section2Radio = Section()
        section2Radio.title = "Attending?"
        section2Radio.type = SectionType.RADIO_BOX
        section2Radio.isRequired = true
        section2Radio.options.addAll(listOf("Yes", "No", "Maybe"))
        section2Radio.form = form2

        val section2Check = Section()
        section2Check.title = "Dietary Restrictions"
        section2Check.type = SectionType.CHECK_BOX
        section2Check.options.addAll(listOf("Vegetarian", "Vegan", "Gluten-Free"))
        section2Check.form = form2

        form2.sections.add(section2Radio)
        form2.sections.add(section2Check)

        // Add forms to user
        user.forms.add(form1)
        user.forms.add(form2)

        // Persist user (cascades to forms and sections)
        user.persist()
    }
}
