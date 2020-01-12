package ru.udya.usercontexts.service.dbcontext;

import java.util.List;
import java.util.UUID;

public interface DbContextManagementService {
    String NAME = "cubausercontexts_DbContextManagementService";

    UUID createDbContext(String forUserLogin);

    Integer commitDbContext(UUID contextId);

    void rollbackDbContext(UUID contextId);

    List<UUID> findAllDbContextIdsByLogin(String login);
}