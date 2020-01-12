-- begin CUBAUSERCONTEXTS_ORDER_ITEM
--alter table CUBAUSERCONTEXTS_ORDER_ITEM add constraint FK_CUBAUSERCONTEXTS_ORDER_ITEM_ON_ORDER foreign key (ORDER_ID) references CUBAUSERCONTEXTS_ORDER(ID)^
--alter table CUBAUSERCONTEXTS_ORDER_ITEM add constraint FK_CUBAUSERCONTEXTS_ORDER_ITEM_ON_PRODUCT foreign key (PRODUCT_ID) references cubausercontexts_product_data(ID)^
--create index IDX_CUBAUSERCONTEXTS_ORDER_ITEM_ON_ORDER on CUBAUSERCONTEXTS_ORDER_ITEM (ORDER_ID)^
--create index IDX_CUBAUSERCONTEXTS_ORDER_ITEM_ON_PRODUCT on CUBAUSERCONTEXTS_ORDER_ITEM (PRODUCT_ID)^
-- end CUBAUSERCONTEXTS_ORDER_ITEM
