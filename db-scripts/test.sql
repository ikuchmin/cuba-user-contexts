create extension "uuid-ossp";

begin work;
set local context.long_context_session_id = '295385ad-50e1-422e-8a49-c5f633be0b32';
EXPLAIN ANALYZE
insert into CUBAUSERCONTEXTS_PRODUCT
(ID, VERSION, CREATE_TS, CREATED_BY, UPDATE_TS, UPDATED_BY, DELETE_TS, DELETED_BY, NAME)
values (uuid_generate_v4(), 1, '2020-01-04 00:11:53', 'admin', '2020-01-04 00:11:53', null, null, null, 'oeu');
commit;

begin work;
set local context.long_context_session_id = '295385ad-50e1-422e-8a49-c5f633be0b32';
select * from cubausercontexts_product;
commit;

select * from cubausercontexts_product;
SET enable_seqscan = ON;
explain analyze select * from cubausercontexts_product where id = '9aaae7fb-4ed8-4ee3-5c94-055fdc450ec3';

ANALYZE cubausercontexts_product_data;
select * from cubausercontexts_product_data;