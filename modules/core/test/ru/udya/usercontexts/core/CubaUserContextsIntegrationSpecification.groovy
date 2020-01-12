package ru.udya.usercontexts.core

import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.Metadata
import org.junit.ClassRule
import ru.udya.usercontexts.CubaUserContextsTestContainer
import ru.udya.usercontexts.builder.PersistenceObjectGraphBuilder
import spock.lang.Shared
import spock.lang.Specification

class CubaUserContextsIntegrationSpecification extends Specification {

    @ClassRule
    @Shared
    CubaUserContextsTestContainer container = CubaUserContextsTestContainer.Common.INSTANCE

    PersistenceObjectGraphBuilder builder
    Metadata metadata
    DataManager dataManager

    void setup() {
        builder = PersistenceObjectGraphBuilder.newInstance(metadata, dataManager)
        builder.classLoader = this.class.classLoader
        builder.classNameResolver = ["ru.udya.usercontexts.entity",
                                     "com.haulmont.cuba.core.entity",
                                     "com.haulmont.cuba.security.entity"]
    }
}
