create database cubausercontexts;

CREATE EXTENSION HSTORE;
CREATE EXTENSION plpython3u;

create type id_with_context as (
    primary_id uuid,
    context_id uuid
);

create type id_with_tablename as (
    primary_id uuid,
    tablename varchar
);

drop table cubausercontexts_context_log;
create table cubausercontexts_context_log
(
    context_id uuid primary key,
    login      varchar(50),
    operations hstore default hstore(array[]::varchar[])
);

create table CUBAUSERCONTEXTS_PRODUCT_DATA
(
    ID         id_with_context,
    VERSION    integer not null,
    CREATE_TS  timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS  timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS  timestamp,
    DELETED_BY varchar(50),
    --
    NAME       varchar(255),
    --
    PRIMARY KEY (ID)
);

create or replace function get_session_context_id() returns uuid as
$$
import uuid

session_context_id_res = plpy.execute(
    "SELECT current_setting('context.session_context_id', true) as session_context_id")

session_context_id = session_context_id_res[0]["session_context_id"]

return uuid.UUID(session_context_id) if session_context_id else None

$$ language plpython3u;

CREATE OR REPLACE VIEW CUBAUSERCONTEXTS_PRODUCT AS
SELECT (id).primary_id as id,
       VERSION,
       CREATE_TS,
       CREATED_BY,
       UPDATE_TS,
       UPDATED_BY,
       DELETE_TS,
       DELETED_BY,
       NAME
FROM CUBAUSERCONTEXTS_PRODUCT_DATA
WHERE CASE
          WHEN get_session_context_id() IS NOT NULL
              THEN ((id).context_id = get_session_context_id()
              OR id NOT IN
                 (SELECT inContext.id
                  FROM CUBAUSERCONTEXTS_PRODUCT_DATA inContext
                  WHERE (inContext.id).context_id = get_session_context_id()))
          ELSE (id).context_id IS NULL
          END;


create or replace function insert_for_view() returns trigger as
$$
def normalize(value):
    if value is None:
        return plpy.quote_nullable(value)
    elif isinstance(value, str):
        return plpy.quote_nullable(value)
    else:
        return str(value)


context_session_id_res = plpy.execute("SELECT get_session_context_id() as session_context_id")
context_session_id = context_session_id_res[0]["session_context_id"]

table = TD["args"][0]

updated = TD["new"]

keys_without_id = [k for k in updated.keys() if k != "id"]
values_without_id = [updated[k] for k in keys_without_id]

sql_insert_keys_section = ",".join([plpy.quote_ident(k) for k in keys_without_id])
sql_insert_values_section = ",".join([normalize(v) for v in values_without_id])

query = """
insert into {} (id, {})
values (row({}, {}), {})"""\
    .format(table, sql_insert_keys_section, normalize(updated["id"]),
            normalize(context_session_id), sql_insert_values_section)

plpy.info("insert in view query: {}".format(query))

plpy.execute(query)

# add information to the commit log
if context_session_id is not None:
    plpy.execute("""
update cubausercontexts_context_log
set operations = operations || hstore({}, null)"""
                 .format(normalize(table)))
$$ language plpython3u;

drop trigger cubausercontexts_product_insert_trigger on cubausercontexts_product;
create trigger cubausercontexts_product_insert_trigger
    instead of insert
    on cubausercontexts_product
    for each row
execute function insert_for_view('cubausercontexts_product_data');

--- cubausercontexts_product_update
create or replace function update_for_view() returns trigger as
$$def normalize(value):
    if value is None:
        return plpy.quote_nullable(value)
    elif isinstance(value, str):
        return plpy.quote_nullable(value)
    else:
        return str(value)


table = TD["args"][0]

updated = TD["new"]

context_session_id_res = plpy.execute("SELECT get_session_context_id() as session_context_id")
context_session_id = context_session_id_res[0]["session_context_id"]

keys_without_id = [k for k in updated.keys() if k != "id"]
values_without_id = [updated[k] for k in keys_without_id]

sql_update_set_section = ",".join(
    ["{} = {}".format(plpy.quote_ident(k), normalize(v)) for k, v in updated.items() if k != "id"])

if context_session_id is None:
    query = "update {} set {} where id = row({}, {})::id_with_context" \
        .format(table, sql_update_set_section,
                normalize(updated["id"]), normalize(context_session_id))
else:
    sql_insert_keys_section = ",".join([plpy.quote_ident(k) for k in keys_without_id])
    sql_insert_values_section = ",".join([normalize(v) for v in values_without_id])

query = """insert into {} (id, {}) values (row({}, {}), {}) on conflict (id)
     where (id).context_id is not null do update set {}"""\
    .format(table, sql_insert_keys_section, normalize(updated["id"]),
        normalize(context_session_id), sql_insert_values_section, sql_update_set_section)

plpy.info(query)

plpy.execute(query)

# add information to the commit log
if context_session_id is not None:
    plpy.execute("update cubausercontexts_context_log set operations = operations || hstore({}, null)"
                 .format(normalize(table)))
$$ language plpython3u;

create trigger cubausercontexts_product_update_trigger
    instead of update
    on cubausercontexts_product
    for each row
execute function update_for_view('cubausercontexts_product_data');

-- implement in feature
-- create or replace function delete_for_view() returns trigger as
-- $$
-- declare
--     prior alias for old;
--     updated alias for new;
--     context_session_id uuid;
-- begin
--     context_session_id = get_long_context_id();
--
--     if context_session_id is null then
--         delete from cubausercontexts_product_data where id = prior.id;
--         return prior;
--     end if;
--
--     insert into cubausercontexts_product_data
--     values (updated.id, updated.version, updated.create_ts,
--             updated.created_by, updated.update_ts, updated.updated_by,
--             updated.delete_ts, updated.deleted_by, updated.name,
--             context_session_id, true)
--     on conflict (id, long_context_id)
--     where long_context_id is not null
--         do
--     update
--     set is_deleted = true;
--
--     return prior;
-- end;
-- $$ language plpgsql;

-- create trigger cubausercontexts_product_delete_trigger
--     instead of delete
--     on cubausercontexts_product
--     for each row
-- execute function cubausercontexts_product_delete();

create or replace function foreign_key_constraint() returns trigger as
$$

foreign_key = TD["args"][0]
foreign_table = TD["args"][1]
table = TD["table_name"]

updated = TD["new"]

context_session_id_res = plpy.execute("SELECT get_session_context_id() as session_context_id")
context_session_id = context_session_id_res[0]["session_context_id"]

foreign_key_val = updated[foreign_key]

query = None
if context_session_id is None:
    query = "select exists(select 1 from {} where id = row({}, null)::id_with_context)" \
        .format(foreign_table, plpy.quote_literal(foreign_key_val))
else:
    query = "select exists(select 1 from {} where id in (row({}, null)::id_with_context, row({}, {})::id_with_context))" \
        .format(foreign_table, plpy.quote_literal(foreign_key_val),
                plpy.quote_literal(foreign_key_val), plpy.quote_literal(context_session_id))

plpy.info(query)

checking_result = plpy.execute(query)

if not checking_result[0]["exists"]:
    plpy.error("insert or update on table \"{}\" violates foreign key constraint \"{}\"".format(table, TD["name"]),
               detail="Key ({})=({}) is not present in table \"{}\""
               .format(foreign_key, foreign_key_val, foreign_table))

return None
$$ language plpython3u;

create or replace function create_context(context_id uuid, login varchar) returns integer as
$$
def normalize(value):
    if value is None:
        return plpy.quote_nullable(value)
    elif isinstance(value, str):
        return plpy.quote_nullable(value)
    else:
        return str(value)

plpy.execute("insert into cubausercontexts_context_log (context_id, login) values ({}, {})"
             .format(normalize(context_id), normalize(login)))

$$ language plpython3u;

create or replace function commit_context(login varchar) returns integer as
$$
context_session_id_res = plpy.execute("SELECT get_session_context_id() as session_context_id")
context_session_id = context_session_id_res[0]["session_context_id"]

if context_session_id is None:
    plpy.error("Context is not defined")

operations = plpy.execute("""
select akeys(operations) as tabls
from cubausercontexts_context_log
where context_id = {} limit 1"""
                          .format(plpy.quote_literal(context_session_id)))

tables = operations[0]["tabls"]

plpy.info(tables)

delete_queries = ["""
delete
from {} a using {} b
where (a.id).context_id is null
  and (b.id).context_id is not null
  and (a.id).primary_id = (b.id).primary_id"""
                      .format(plpy.quote_ident(t),
                              plpy.quote_ident(t)) for t in tables ]

update_queries = ["""
update {}
set id = row((id).primary_id, null)
where (id).context_id = {}"""
                      .format(plpy.quote_ident(t),
                              plpy.quote_literal(context_session_id)) for t in tables]

plpy.info("delete queries {}".format(delete_queries))
plpy.info("update queries {}".format(update_queries))

for query in delete_queries:
    plpy.execute(query)

for query in update_queries:
    plpy.execute(query)

# remove context session
plpy.execute("""
delete
from cubausercontexts_context_log
where context_id = {}"""
             .format(plpy.quote_literal(context_session_id)))
$$ language plpython3u;

create or replace function rollback_context(login varchar) returns integer as
$$
context_session_id_res = plpy.execute("SELECT get_session_context_id() as session_context_id")
context_session_id = context_session_id_res[0]["session_context_id"]

if context_session_id is None:
    plpy.error("Context is not defined")

plpy.execute("select ")
$$ language plpython3u;

create table CUBAUSERCONTEXTS_ORDER_ITEM_DATA
(
    ID         id_with_context,
    VERSION    integer not null,
    CREATE_TS  timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS  timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS  timestamp,
    DELETED_BY varchar(50),
    --
    ORDER_ID   uuid,
    PRODUCT_ID uuid,
    --
    primary key (ID)
);

alter table CUBAUSERCONTEXTS_ORDER_ITEM_DATA
    add constraint FK_CUBAUSERCONTEXTS_ORDER_ITEM_ON_PRODUCT foreign key (PRODUCT_ID) references cubausercontexts_product_data (ID) deferrable;

drop trigger fk_cubausercontexts_order_item_on_product_insert on cubausercontexts_order_item_data;
create constraint trigger fk_cubausercontexts_order_item_on_product_insert
    after insert
    on CUBAUSERCONTEXTS_ORDER_ITEM_DATA from CUBAUSERCONTEXTS_PRODUCT_DATA deferrable
    for each row
execute function foreign_key_constraint('product_id', 'cubausercontexts_product_data');

drop trigger fk_cubausercontexts_order_item_on_product_update on cubausercontexts_order_item_data;
create constraint trigger fk_cubausercontexts_order_item_on_product_update
    after update
    on CUBAUSERCONTEXTS_ORDER_ITEM_DATA from CUBAUSERCONTEXTS_PRODUCT_DATA deferrable
    for each row
    when (OLD.ORDER_ID <> NEW.ORDER_ID)
execute function foreign_key_constraint('product_id', 'cubausercontexts_product_data');

CREATE OR REPLACE VIEW CUBAUSERCONTEXTS_ORDER_ITEM AS
SELECT (id).primary_id as id,
       VERSION,
       CREATE_TS,
       CREATED_BY,
       UPDATE_TS,
       UPDATED_BY,
       DELETE_TS,
       DELETED_BY,
       --
       order_id        as order_id,
       product_id      as product_id

FROM cubausercontexts_order_item_data
WHERE CASE
          WHEN get_session_context_id() IS NOT NULL
              THEN ((id).context_id = get_session_context_id()
              OR id NOT IN
                 (SELECT inContext.id
                  FROM cubausercontexts_order_item_data inContext
                  WHERE (inContext.id).context_id = get_session_context_id()))
          ELSE (id).context_id IS NULL
          END;

create trigger cubausercontexts_order_item_insert_trigger
    instead of insert
    on cubausercontexts_order_item
    for each row
execute function insert_for_view('cubausercontexts_order_item_data');
