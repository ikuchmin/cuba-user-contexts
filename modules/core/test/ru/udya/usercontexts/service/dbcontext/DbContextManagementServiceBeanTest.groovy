package ru.udya.usercontexts.service.dbcontext

import com.haulmont.cuba.core.entity.contracts.Id
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.global.UserSessionSource
import com.haulmont.cuba.security.global.UserSession
import ru.udya.usercontexts.core.CubaUserContextsIntegrationSpecification
import ru.udya.usercontexts.entity.Product
import ru.udya.usercontexts.service.repository.ProductRepositoryService

import static ru.udya.usercontexts.context.RepositoryExecutionContext.createRepositoryContext
import static ru.udya.usercontexts.context.RepositoryExecutionContext.emptyRepositoryContext

class DbContextManagementServiceBeanTest extends CubaUserContextsIntegrationSpecification {

    DbContextManagementService dbContextManagementService
    ProductRepositoryService productRepository

    UserSession userSession

    @Override
    void setup() {
        dbContextManagementService = AppBeans.get(DbContextManagementService)
        productRepository = AppBeans.get(ProductRepositoryService)

        userSession = AppBeans.get(UserSessionSource).getUserSession()
    }

    def "check that findAllDbContextIdsByLogin returns all available db context ids"() {
        given:
        def contextIds = [dbContextManagementService.createDbContext(userSession.user.login),
                          dbContextManagementService.createDbContext(userSession.user.login)]

        when:
        def actualContextIds = dbContextManagementService.findAllDbContextIdsByLogin(userSession.user.login)

        then:
        actualContextIds.containsAll(contextIds)
    }

    def "check that a db context can be created by method createDbContext"() {
        when:
        def contextId = dbContextManagementService.createDbContext(userSession.user.login)

        then:
        contextId in dbContextManagementService.findAllDbContextIdsByLogin(userSession.user.login)
    }

    def "check that commitDbContext does objects visible without contexts"() {
        given:
        def contextId = dbContextManagementService.createDbContext(userSession.user.login)

        when: "create product into context"
        def product = productRepository.createProduct(createRepositoryContext().setDbContextId(contextId))

        then:
        null == productRepository.findProductById(Id.of(product.id, Product), emptyRepositoryContext())

        when: "commit context"
        dbContextManagementService.commitDbContext(contextId)

        then:
        product == productRepository.findProductById(Id.of(product.id, Product), emptyRepositoryContext())
    }

    def "check that commitDbContext does objects visible in other contexts"() {
        given:
        def originalContextId = dbContextManagementService.createDbContext(userSession.user.login)
        def anotherContextId = dbContextManagementService.createDbContext(userSession.user.login)

        when: "create product into context"
        def product = productRepository.createProduct(createRepositoryContext().setDbContextId(originalContextId))

        then:
        null == productRepository.findProductById(Id.of(product.id, Product),
                createRepositoryContext().setDbContextId(anotherContextId))

        when: "commit context"
        dbContextManagementService.commitDbContext(originalContextId)

        then:
        product == productRepository.findProductById(Id.of(product.id, Product),
                createRepositoryContext().setDbContextId(anotherContextId))
    }
}
