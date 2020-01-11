create extension "uuid-ossp";

begin work;
set local context.session_context_id = '295385ad-50e1-422e-8a49-c5f633be0b32';
EXPLAIN ANALYZE
insert into CUBAUSERCONTEXTS_PRODUCT
(ID, VERSION, CREATE_TS, CREATED_BY, UPDATE_TS, UPDATED_BY, DELETE_TS, DELETED_BY, NAME)
values (uuid_generate_v4(), 1, '2020-01-04 00:11:53', 'admin', '2020-01-04 00:11:53', null, null, null, 'oeu');
commit;

select * from cubausercontexts_context_log;

select *
from CUBAUSERCONTEXTS_PRODUCT;
select *
from cubausercontexts_product_data;

begin work;
set local context.session_context_id = '295385ad-50e1-422e-8a49-c5f633be0b32';
select *
from cubausercontexts_product;
commit;

select *
from cubausercontexts_product;
SET enable_seqscan = ON;
explain analyze
select *
from cubausercontexts_product
where id = '9aaae7fb-4ed8-4ee3-5c94-055fdc450ec3';

ANALYZE cubausercontexts_product_data;
select *
from cubausercontexts_product_data;

begin work;
set local context.long_context_session_id = '295385ad-50e1-422e-8a49-c5f633be0b32';
select get_long_context_id();
commit;

update CUBAUSERCONTEXTS_PRODUCT
set version = 3
where id = 'f71d6573-ed5d-4e16-953d-5f7432a39a89';

update cubausercontexts_product_data
set version    = 2,
    create_ts  = '2020-01-04 00:11:53',
    created_by = 'admin',
    update_ts  = '2020-01-04 00:11:53',
    updated_by = NULL,
    delete_ts  = NULL,
    deleted_by = NULL,
    name       = 'oeu'
where id = row ('fbac8390-c8ef-4bce-bcd1-8419e9993030', NULL)::id_with_context


select *
from cubausercontexts_order_item;

explain analyze select (select r.relname from pg_class r where r.oid = c.confrelid) as ftable
from pg_constraint c
where c.confrelid = (select oid from pg_class where relname = 'cubausercontexts_product_data');


select * from pg_constraint;
select oid from pg_class where relname = 'cubausercontexts_product_data';
select * from cubausercontexts.pg_catalog.pg_class where relname = 'cubausercontexts_order_item_data';

insert into cubausercontexts_order_item_data (id, version, order_id, product_id)
values (row(uuid_generate_v4(), null), 1, uuid_generate_v4(), 'c6a9c161-54ee-41e6-92b2-8dcb566367e2');

insert into cubausercontexts_order_item_data (id, version, order_id, product_id)
values (row(uuid_generate_v4(), null), 1, uuid_generate_v4(), uuid_generate_v4());

select exists(select 1 as result from cubausercontexts_product_data where id = row('4723f607-be1a-4987-ba59-08f223475c26', null)::id_with_context)
select exists(select 1 from cubausercontexts_product_data where id = row('ce0f70f8-254e-4fe3-80b0-fa4b287c279d', null)::id_with_context)
