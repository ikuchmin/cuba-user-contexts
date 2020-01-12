package ru.udya.usercontexts.service.dbcontext;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.UuidProvider;
import org.springframework.stereotype.Service;
import ru.udya.usercontexts.core.DbContextTool;

import java.util.List;
import java.util.UUID;

@Service(DbContextManagementService.NAME)
public class DbContextManagementServiceBean implements DbContextManagementService {

    protected Persistence persistence;

    protected DbContextTool dbContextTool;

    public DbContextManagementServiceBean(Persistence persistence,
                                          DbContextTool dbContextTool) {
        this.persistence = persistence;
        this.dbContextTool = dbContextTool;
    }

    @Override
    public UUID createDbContext(String forUserLogin) {

        UUID contextId = UuidProvider.createUuid();

        try (Transaction tx = persistence.getTransaction()) {

            EntityManager em = persistence.getEntityManager();

            Query query = em.createNativeQuery("select create_context(?, ?)")
                    .setParameter(1, contextId)
                    .setParameter(2, forUserLogin);

            query.getSingleResult();

            tx.commit();
        }

        return contextId;
    }

    @Override
    public Integer commitDbContext(UUID contextId) {
        try (Transaction tx = persistence.getTransaction()) {

            EntityManager em = persistence.getEntityManager();

            dbContextTool.joinDbContext(contextId);

            Query query = em.createNativeQuery("select commit_context()");

            Integer queryResult = (Integer) query.getSingleResult();

            tx.commit();

            return queryResult;
        }
    }

    @Override
    public void rollbackDbContext(UUID contextId) {

    }

    @Override
    public List<UUID> findAllDbContextIdsByLogin(String login) {

        try (Transaction tx = persistence.getTransaction()) {

            EntityManager em = persistence.getEntityManager();

            Query query = em.createNativeQuery(
                    "select context_id from cubausercontexts_context_log where login = ?")
                    .setParameter(1, login);

            //noinspection unchecked
            List<UUID> contextIds = query.getResultList();

            tx.commit();

            return contextIds;
        }
    }
}