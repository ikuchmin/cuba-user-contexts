create table CUBAUSERCONTEXTS_PRODUCT_DATA
(
    ID              uuid,
    VERSION         integer not null,
    CREATE_TS       timestamp,
    CREATED_BY      varchar(50),
    UPDATE_TS       timestamp,
    UPDATED_BY      varchar(50),
    DELETE_TS       timestamp,
    DELETED_BY      varchar(50),
    --
    NAME            varchar(255),
    --
    long_context_id uuid,
    is_deleted      boolean
);

CREATE OR REPLACE FUNCTION get_long_context_id() RETURNS uuid AS $$
BEGIN
    RETURN nullif(coalesce(current_setting('context.long_context_session_id', true), ''), '')::uuid;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE VIEW CUBAUSERCONTEXTS_PRODUCT AS
SELECT *
FROM CUBAUSERCONTEXTS_PRODUCT_DATA
WHERE CASE
          WHEN get_long_context_id() IS NOT NULL
              THEN (long_context_id = get_long_context_id()
              OR id NOT IN (SELECT inContext.id
                            FROM CUBAUSERCONTEXTS_PRODUCT_DATA inContext
                            WHERE inContext.long_context_id = get_long_context_id()))
          ELSE long_context_id IS NULL
          END;


create or replace function cubausercontexts_product_insert() returns trigger as
$$
declare
    prior alias for old;
    updated alias for new;
    context_session_id uuid;
begin
    context_session_id = get_long_context_id();

    insert into cubausercontexts_product_data
    values (updated.id, updated.version, updated.create_ts,
            updated.created_by, updated.update_ts, updated.updated_by,
            updated.delete_ts, updated.deleted_by, updated.name);

    if context_session_id is not null then
        update CUBAUSERCONTEXTS_PRODUCT_DATA
        set long_context_id = context_session_id
        where id = updated.id;
    end if;

    return updated;
end;
$$ language plpgsql;

create trigger cubausercontexts_product_insert_trigger instead of insert on cubausercontexts_product
    for each row execute function cubausercontexts_product_insert();

create or replace function cubausercontexts_product_update() returns trigger as
$$
declare
    prior alias for old;
    updated alias for new;
    context_session_id uuid;
begin
    context_session_id = get_long_context_id();

    if context_session_id is null then
        update cubausercontexts_product_data
        set version    = updated.version,
            create_ts  = updated.create_ts,
            created_by = updated.created_by,
            update_ts  = updated.update_ts,
            updated_by = updated.updated_by,
            delete_ts  = updated.delete_ts,
            deleted_by = updated.deleted_by,
            name       = updated.name
        where id = updated.id;
        return updated;
    end if;

    insert into cubausercontexts_product_data
    values (updated.id, updated.version, updated.create_ts,
            updated.created_by, updated.update_ts, updated.updated_by,
            updated.delete_ts, updated.deleted_by, updated.name,
            context_session_id)
    on conflict (id, long_context_id)
    where long_context_id is not null
        do
    update
    set version         = updated.version,
        create_ts       = updated.create_ts,
        created_by      = updated.created_by,
        update_ts       = updated.update_ts,
        updated_by      = updated.updated_by,
        delete_ts       = updated.delete_ts,
        deleted_by      = updated.deleted_by,
        name            = updated.name,
        long_context_id = context_session_id;
    return updated;
end;
$$ language plpgsql;

create trigger cubausercontexts_product_update_trigger instead of update on cubausercontexts_product
    for each row execute function cubausercontexts_product_update();

create or replace function cubausercontexts_product_delete() returns trigger as
$$
declare
    prior alias for old;
    updated alias for new;
    context_session_id uuid;
begin
    context_session_id = get_long_context_id();

    if context_session_id is null then
        delete from cubausercontexts_product_data where id = prior.id;
        return prior;
    end if;

    insert into cubausercontexts_product_data
    values (updated.id, updated.version, updated.create_ts,
            updated.created_by, updated.update_ts, updated.updated_by,
            updated.delete_ts, updated.deleted_by, updated.name,
            context_session_id, true)
    on conflict (id, long_context_id) where long_context_id is not null
        do update set is_deleted = true;

    return prior;
end;
$$ language plpgsql;

create trigger cubausercontexts_product_delete_trigger instead of delete on cubausercontexts_product
    for each row execute function cubausercontexts_product_delete();