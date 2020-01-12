-- begin CUBAUSERCONTEXTS_ORDER
create table CUBAUSERCONTEXTS_ORDER (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    primary key (ID)
)^
-- end CUBAUSERCONTEXTS_ORDER
-- begin CUBAUSERCONTEXTS_CUSTOMER
create table CUBAUSERCONTEXTS_CUSTOMER (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    FIRST_NAME varchar(255) not null,
    LAST_NAME varchar(255) not null,
    BIRTH_DAY date,
    --
    primary key (ID)
)^
-- end CUBAUSERCONTEXTS_CUSTOMER
