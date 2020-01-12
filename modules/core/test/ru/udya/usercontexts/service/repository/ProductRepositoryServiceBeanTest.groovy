package ru.udya.usercontexts.service.repository


import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.global.UserSessionSource
import com.haulmont.cuba.security.global.UserSession
import ru.udya.usercontexts.core.CubaUserContextsIntegrationSpecification
import ru.udya.usercontexts.entity.Product
import ru.udya.usercontexts.service.dbcontext.DbContextManagementService

import static com.haulmont.cuba.core.entity.contracts.Id.of
import static ru.udya.usercontexts.context.RepositoryExecutionContext.createRepositoryContext
import static ru.udya.usercontexts.context.RepositoryExecutionContext.emptyRepositoryContext

class ProductRepositoryServiceBeanTest extends CubaUserContextsIntegrationSpecification {

    ProductRepositoryService productRepository
    DbContextManagementService dbContextManagementService

    UserSession userSession

    @Override
    void setup() {
        productRepository = AppBeans.get(ProductRepositoryService)
        dbContextManagementService = AppBeans.get(DbContextManagementService)

        userSession = AppBeans.get(UserSessionSource).getUserSession()
    }

    def "check that empty product can be created"() {
        given:
        UUID dbContext = dbContextManagementService.createDbContext(userSession.user.login)

        when:
        def created = productRepository
                .createProduct(createRepositoryContext().setDbContextId(dbContext))

        then:
        productRepository.findProductById(of(created.id, Product),
                createRepositoryContext().setDbContextId(dbContext)) == created
    }

    def "check that product is created into db context available only in the context"() {
        given:
        UUID dbContext = dbContextManagementService.createDbContext(userSession.user.login)
        UUID dbContextSecond = dbContextManagementService.createDbContext(userSession.user.login)

        when:
        def created = productRepository
                .createProduct(createRepositoryContext().setDbContextId(dbContext))

        then:
        productRepository.findProductById(of(created.id, Product),
                createRepositoryContext().setDbContextId(dbContext)) == created

        productRepository.findProductById(of(created.id, Product),
                createRepositoryContext().setDbContextId(dbContextSecond)) == null

        productRepository.findProductById(of(created.id, Product), emptyRepositoryContext()) == null
    }

    def "check that product is updated into db context available only in the context"() {
        given:
        UUID dbContext = dbContextManagementService.createDbContext(userSession.user.login)
        UUID dbContextSecond = dbContextManagementService.createDbContext(userSession.user.login)

        def created = productRepository
                .createProduct(createRepositoryContext().setDbContextId(dbContext))

        when:
        created.name = "New name"
        productRepository.updateProduct(created, createRepositoryContext().setDbContextId(dbContext))

        then:
        productRepository.findProductById(of(created.id, Product),
                createRepositoryContext().setDbContextId(dbContext)) == created

        productRepository.findProductById(of(created.id, Product),
                createRepositoryContext().setDbContextId(dbContextSecond)) == null

        productRepository.findProductById(of(created.id, Product), emptyRepositoryContext()) == null
    }

    def "check that optimistic locking works as well"() {

    }

    def "check that not null restrictions on entities works as well"() {

    }

    def "check that foreign keys on entity works as well"() {

    }

    // think about changing behaviour for foreign_key to make them work only with records without context_id
}
