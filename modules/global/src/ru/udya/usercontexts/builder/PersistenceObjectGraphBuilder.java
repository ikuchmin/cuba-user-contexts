package ru.udya.usercontexts.builder;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.View;
import groovy.lang.Closure;
import groovy.lang.MissingPropertyException;
import groovy.util.ObjectGraphBuilder;
import io.vavr.collection.Stream;
import org.apache.commons.collections4.CollectionUtils;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.emptyCollection;

/**
 * Primary needed for tests
 */
@SuppressWarnings("unused")
public class PersistenceObjectGraphBuilder extends ObjectGraphBuilder {

    protected ThreadLocal<List<Entity>> persistentEntities;
    protected ThreadLocal<List<Entity>> cleanUpEntities;

    protected Metadata metadata;

    protected DataManager dataManager;

    protected PersistenceObjectGraphBuilder(Metadata metadata, DataManager dataManager) {
        this.metadata = metadata;
        this.dataManager = dataManager;

        this.persistentEntities = new ThreadLocal<>();
        this.persistentEntities.set(new ArrayList<>());

        this.cleanUpEntities = new ThreadLocal<>();
        this.cleanUpEntities.set(new ArrayList<>());

        addDisposalClosure(persistEntitiesClosure());
    }

    public static PersistenceObjectGraphBuilder newInstance(Metadata metadata, DataManager dataManager) {
        PersistenceObjectGraphBuilder builder = new PersistenceObjectGraphBuilder(metadata, dataManager);

        builder.setChildPropertySetter(builder.new PersistenceChildPropertySetter());
        builder.setNewInstanceResolver(builder.new PersistenceNewInstanceResolver());
        builder.setRelationNameResolver(builder.new PersistenceRelationNameResolver());
        builder.setIdentifierResolver(builder.new PersistenceIdentifierResolver());

        return builder;
    }

    /**
     * Alias for the current
     *
     * @return the current node being built.
     */
    public Object get$$() {
        return getCurrent();
    }

    /**
     * Alias for the parent
     *
     * @return the node of the parent of the current node.
     */
    public Object getParent() {
        return getParentNode();
    }

    /**
     * Removes persisted entries
     */
    public void cleanUp() {
        //Entity is needed to reload because it may be changed or removed in DB and on removing will be thrown OptimisticLockException.

        List<Entity> reloaded = cleanUpEntities.get().stream()
                .map(e -> {
                    LoadContext<Entity> lc = new LoadContext<>(e.getMetaClass());
                    lc.setView(View.MINIMAL);
                    lc.setId(e.getId());
                    return dataManager.load(lc);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        CommitContext cc = new CommitContext(emptyCollection(), reloaded);
        dataManager.commit(cc);
    }

    protected Closure<Void> persistEntitiesClosure() {
        return new Closure<Void>(this, this) {
            public void doCall() {
                CommitContext cc = Stream.ofAll(persistentEntities.get())
                        .foldLeft(new CommitContext(), CommitContext::addInstanceToCommit);

                Set<Entity> committed = dataManager.commit(cc);
                if (CollectionUtils.isNotEmpty(committed)) {
                    cleanUpEntities.get().addAll(committed);
                }
                persistentEntities.get().clear();
            }
        };
    }

    /**
     * Adds behaviour for a list argument
     *
     * @param classNameResolver is ClassNameResolver instance, a String, a Closure, a Map or a List.
     */
    @SuppressWarnings("unchecked")
    public void setClassNameResolver(Object classNameResolver) {
        if (classNameResolver instanceof List) {
            List<String> packages = (List<String>) classNameResolver;
            super.setClassNameResolver(new Closure<String>(this) {
                @Override
                public String call(Object... args) {
                    String classname = (String) args[0];

                    for (String p : packages) {
                        String fullClassName = makeClassName(p, classname);

                        if (isClassAvailableInClassLoader(fullClassName)) {
                            return fullClassName;
                        }
                    }

                    return classname; // return original classname if no one is not found
                }
            });
        } else {
            super.setClassNameResolver(classNameResolver);
        }
    }

    protected boolean isClassAvailableInClassLoader(String fullClassName) {
        Class<?> aClass = null;
        try {
            aClass = getClassLoader().loadClass(fullClassName);
        } catch (ClassNotFoundException e) {
            // ignored. Definition for the fullClassName have not been found
        }
        return aClass != null;
    }

    protected String makeClassName(String root, String name) {
        return root + "." + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * Default impl that calls parent.propertyName = child<br>
     * If parent.propertyName is a Collection it will not perform any actions
     * collection.
     */
    public class PersistenceChildPropertySetter implements ChildPropertySetter {

        @Override
        public void setChild(Object parent, Object child, String parentName, String propertyName) {
            try {
                Object property = InvokerHelper.getProperty(parent, propertyName);
                if (property == null || ! Collection.class.isAssignableFrom(property.getClass())) {
                    InvokerHelper.setProperty(parent, propertyName, child);
                }
            } catch (MissingPropertyException mpe) {
                // ignore
            }
        }
    }

    /**
     * Default impl that calls Metadata.create()
     */
    public class PersistenceNewInstanceResolver implements NewInstanceResolver {

        @Override
        public Object newInstance(Class klass, Map attributes) throws InstantiationException,
                IllegalAccessException {

            //noinspection unchecked
            Entity entity = (Entity) metadata.create(klass);
            persistentEntities.get().add(entity);

            return entity;
        }
    }

    public class PersistenceRelationNameResolver extends DefaultRelationNameResolver {

        @Override
        public String resolveParentRelationName(String parentName, Object parent, String childName, Object child) {
            if (parentName.length() == 1) {
                return parentName.toLowerCase();
            }
            return parentName.substring(0, 1)
                    .toLowerCase() + parentName.substring(1);
        }
    }

    /**
     * Default impl, always returns 'refId'
     */
    public class PersistenceIdentifierResolver implements IdentifierResolver {
        public String getIdentifierFor(String nodeName) {
            return "graphId";
        }
    }
}
