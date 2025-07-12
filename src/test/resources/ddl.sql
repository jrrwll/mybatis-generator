create table live_room (
    tenant_id   varchar(32) not null comment 'tenant ID',
    seq_id      bigint      not null comment 'seq id',
    record_date date        not null comment 'record date',
    room_id     varchar(30) not null comment '',
    created_at  datetime default current_timestamp,
    extra       text comment '',
    primary key (tenant_id, seq_id, record_date)
) comment 'a live room record';

create table way_bill (
    way_bill_id bigint   not null comment 'waybill id',
    customer_id bigint   not null comment 'customer ID',
    shop_id     varchar(50)       default null, -- shop id
    created_at  datetime not null default current_timestamp,
    primary key (`way_bill_id`)
) comment = 'a waybill record';
