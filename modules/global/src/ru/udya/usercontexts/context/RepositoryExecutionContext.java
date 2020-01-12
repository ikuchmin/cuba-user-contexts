package ru.udya.usercontexts.context;

import java.util.UUID;

public interface RepositoryExecutionContext extends ExecutionContext {

    UUID getDbContextId();

    static RepositoryExecutionContextImpl createRepositoryContext() {
        return new RepositoryExecutionContextImpl();
    }

    static RepositoryExecutionContextImpl emptyRepositoryContext() {
        return new RepositoryExecutionContextImpl();
    }

    class RepositoryExecutionContextImpl implements RepositoryExecutionContext {

        protected UUID dbContextId;


        public RepositoryExecutionContextImpl() {
        }

        @Override
        public UUID getDbContextId() {
            return dbContextId;
        }

        public RepositoryExecutionContextImpl setDbContextId(UUID dbContextId) {
            this.dbContextId = dbContextId;

            return this;
        }

    }
}
