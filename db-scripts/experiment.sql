create extension "uuid-ossp";

DROP TABLE context_demo_data;
CREATE TABLE context_demo_data
(
    id               uuid not null,
    val              int,
    version          int not null,
    delete_ts        timestamp,
    --based_on_version int,
    long_context_id  uuid,
    is_deleted       boolean
);

alter table context_demo_data add column is_deleted boolean;
create unique index context_demo_data_pkey on context_demo_data (id)
    where long_context_id is null;
create unique index context_demo_data_long_context_unq on context_demo_data (id, long_context_id)
    where long_context_id is not null;
create index context_demo_data_long_context_id_idx on context_demo_data (long_context_id) where long_context_id is not null;
delete from context_demo_data;

DROP VIEW context_demo;
CREATE OR REPLACE VIEW context_demo AS
SELECT *
FROM context_demo_data
WHERE CASE
          WHEN get_long_context_id() IS NOT NULL
              THEN ((id).long_context_id = get_long_context_id()
              OR id NOT IN (SELECT inContext.id
                            FROM context_demo_data inContext
                            WHERE (inContext.id).long_context_id = get_long_context_id()))
          ELSE long_context_id IS NULL
          END;

CREATE OR REPLACE FUNCTION get_long_context_id() RETURNS uuid AS $$
BEGIN
    RETURN nullif(coalesce(current_setting('context.long_context_session_id', true), ''), '')::uuid;
END;
$$ LANGUAGE plpgsql;


select current_setting('context.long_context_session_id') = '';

create or replace function context_demo_update() returns trigger as
$$
declare
    prior alias for old;
    updated alias for new;
    context_session_id uuid;
begin
    context_session_id = get_long_context_id();

    if context_session_id is null then
        update context_demo_data
        set val       = updated.val,
            version   = updated.version,
            delete_ts = updated.delete_ts;
        return updated;
    end if;

    insert into context_demo_data
    values (updated.id, updated.val, updated.version,
            updated.delete_ts, context_session_id)
    on conflict (id, long_context_id) where long_context_id is not null
        do update set val              = updated.val,
                      version          = updated.version,
                      delete_ts        = updated.delete_ts,
                      long_context_id  = context_session_id;
    return updated;
end;
$$ language plpgsql;

CREATE TRIGGER context_demo_update_trigger INSTEAD OF UPDATE ON context_demo
    FOR EACH ROW EXECUTE FUNCTION context_demo_update();

create or replace function context_demo_insert() returns trigger as
$$
declare
    prior alias for old;
    updated alias for new;
    context_session_id uuid;
begin
    context_session_id = get_long_context_id();

    insert into context_demo_data
    values (updated.id, updated.val, updated.version,
            updated.delete_ts);

    if context_session_id is not null then
        update context_demo_data
        set long_context_id = context_session_id
        where id = updated.id;
    end if;

    return updated;
end;
$$ language plpgsql;

drop trigger context_demo_insert_trigger on context_demo;
create trigger context_demo_insert_trigger instead of insert on context_demo
    for each row execute function context_demo_insert();

create or replace function context_demo_delete() returns trigger as
$$
declare
    prior alias for old;
    updated alias for new;
    context_session_id uuid;
begin
    context_session_id = get_long_context_id();

    if context_session_id is null then
        delete from context_demo_data where id = prior.id;
        return prior;
    end if;

    insert into context_demo_data
    values (prior.id, prior.val, prior.version,
            prior.delete_ts, context_session_id, true)
    on conflict (id, long_context_id) where long_context_id is not null
        do update set is_deleted = true;

    return prior;
end;
$$ language plpgsql;

create trigger context_demo_delete_trigger instead of delete on context_demo
    for each row execute function context_demo_delete();

insert into context_demo_data
values (updated.id, updated.val, updated.version,
        updated.delete_ts, updated.based_on_version,
        updated.long_context_id);

-- first insert
INSERT INTO context_demo_data
VALUES ('d5c458a8-12f5-491f-9b63-3d50f4c6b092', 1, 1, null, null, null);

BEGIN WORK;
UPDATE context_demo
SET delete_ts = now()
WHERE id = 'd5c458a8-12f5-491f-9b63-3d50f4c6b092'
  AND long_context_id IS NULL
  AND version = 1; -- if version doesn't equal with based_on_version that

UPDATE context_demo
SET long_context_id = NULL
WHERE id = 'd5c458a8-12f5-491f-9b63-3d50f4c6b092'
  AND long_context_id = '295385ad-50e1-422e-8a49-c5f633be0b32';

COMMIT WORK;
-- commit for first insert

select * from context_demo_data;

-- test working with transaction

BEGIN WORK;
SET LOCAL context.long_context_session_id = '295385ad-50e1-422e-8a49-c5f633be0b32';
--EXPLAIN ANALYZE
SELECT * FROM context_demo;
COMMIT;

begin work ;
set local context.long_context_session_id = '295385ad-50e1-422e-8a49-c5f633be0b32';
--EXPLAIN ANALYZE
update context_demo set val = val + 10;
commit ;

-- for conflict
begin work;
set local context.long_context_session_id = '295385ad-50e1-422e-8a49-c5f633be0b32';
EXPLAIN ANALYZE
insert into context_demo
values ('d5c458a8-12f5-491f-9b63-3d50f4c6b095', 1, 1, null);
commit;

-- to check performance
begin work;
set local context.long_context_session_id = '295385ad-50e1-422e-8a49-c5f633be0b32';
EXPLAIN ANALYZE
insert into context_demo
values (uuid_generate_v4(), 1, 1, null);
commit;

begin work;
set local context.long_context_session_id = '295385ad-50e1-422e-8a49-c5f633be0b32';
EXPLAIN ANALYZE
delete from context_demo where id = 'd5c458a8-12f5-491f-9b63-3d50f4c6b095';
commit;