package ru.udya.usercontexts.service.repository;

import com.haulmont.cuba.core.entity.contracts.Id;
import ru.udya.usercontexts.context.RepositoryExecutionContext;
import ru.udya.usercontexts.entity.Product;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface ProductRepositoryService {
    String NAME = "cubausercontexts_ProductRepositoryService";

    Product createProduct(RepositoryExecutionContext context);

    List<Product> findAllProducts(RepositoryExecutionContext context);

    @Nullable
    Product findProductById(Id<Product, UUID> productId, RepositoryExecutionContext context);

    void updateProduct(Product product, RepositoryExecutionContext context);

    void deleteAllProducts(DeleteAllProductsExecutionContext context);

    void deleteProductById(RepositoryExecutionContext context);

    interface DeleteAllProductsExecutionContext extends RepositoryExecutionContext {

        static DeleteAllProductsExecutionContextImpl createContext() {
            return new DeleteAllProductsExecutionContextImpl();
        }

        class DeleteAllProductsExecutionContextImpl
                extends RepositoryExecutionContextImpl
                implements DeleteAllProductsExecutionContext {

            // some additional methods

        }

        // some additional methods
    }

}