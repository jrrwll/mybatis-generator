create table live_room (
    tenant_id   varchar(32) not null,
    seq_id      bigint      not null,
    record_date date        not null,
    room_id     varchar(30) not null,
    created_at  datetime    default current_timestamp,
    primary key (tenant_id, seq_id, record_date)
);

create table way_bill (
    way_bill_id   bigint not null primary key ,
    customer_id   bigint not null,
    shop_id   varchar(50) default null,
    created_at  datetime not null default current_timestamp
);
