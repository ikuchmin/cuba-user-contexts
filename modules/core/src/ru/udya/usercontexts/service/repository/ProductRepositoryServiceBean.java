package ru.udya.usercontexts.service.repository;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.entity.contracts.Id;
import com.haulmont.cuba.core.global.Metadata;
import org.springframework.stereotype.Service;
import ru.udya.usercontexts.context.RepositoryExecutionContext;
import ru.udya.usercontexts.core.DbContextTool;
import ru.udya.usercontexts.entity.Product;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

@Service(ProductRepositoryService.NAME)
public class ProductRepositoryServiceBean implements ProductRepositoryService {

    protected Metadata metadata;

    protected Persistence persistence;

    protected DbContextTool dbContextTool;

    public ProductRepositoryServiceBean(Metadata metadata,
                                        Persistence persistence,
                                        DbContextTool dbContextTool) {
        this.metadata = metadata;
        this.persistence = persistence;
        this.dbContextTool = dbContextTool;
    }

    @Override
    public Product createProduct(RepositoryExecutionContext context) {

        // check constraint

        Product product = metadata.create(Product.class);

        try(Transaction tx = persistence.getTransaction()) {

            if (context.getDbContextId() != null) {
                dbContextTool.joinDbContext(context.getDbContextId());
            }

            persistence.getEntityManager().persist(product);

            tx.commit();
        }

        return product;
    }

    @Override
    public List<Product> findAllProducts(RepositoryExecutionContext context) {
        return null;
    }

    @Nullable
    @Override
    public Product findProductById(Id<Product, UUID> productId, RepositoryExecutionContext context) {

        try (Transaction tx = persistence.getTransaction()) {

            if (context.getDbContextId() != null) {
                dbContextTool.joinDbContext(context.getDbContextId());
            }

            Product foundProduct = persistence.getEntityManager()
                    .createQuery("select p from cubausercontexts$Product p where p.id = :productId", Product.class)
                    .setParameter("productId", productId.getValue())
                    .getFirstResult();

            tx.commit();

            return foundProduct;
        }
    }

    @Override
    public void updateProduct(Product product, RepositoryExecutionContext context) {

    }

    @Override
    public void deleteAllProducts(DeleteAllProductsExecutionContext context) {

    }

    @Override
    public void deleteProductById(RepositoryExecutionContext context) {

    }
}