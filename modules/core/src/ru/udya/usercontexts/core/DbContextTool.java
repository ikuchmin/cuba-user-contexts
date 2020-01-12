package ru.udya.usercontexts.core;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static java.lang.String.format;

@Component
public class DbContextTool {

    protected Persistence persistence;

    public DbContextTool(Persistence persistence) {
        this.persistence = persistence;
    }

    /**
     * The method must be executed in transaction
     *
     * @param contextId
     */
    public void joinDbContext(UUID contextId) {

        // method doesn't use getTransaction by contract

        persistence.getEntityManager()
                .createNativeQuery(format("set local context.session_context_id = '%s'", contextId))
                .executeUpdate(); // I think that it is a hack
    }
}
