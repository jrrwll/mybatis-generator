create table tenant (
    id         bigint(20)  not null auto_increment comment 'tenant ID',
    name       varchar(50) not null comment 'tenant name',
    code       varchar(50) not null comment 'tenant code',
    created_at datetime    not null default current_timestamp,
    updated_at datetime    not null default current_timestamp on update current_timestamp,
    deleted    tinyint(1)  not null default 0 comment 'deleted',
    primary key (id)
) comment ='tenant table comment' charset utf8mb4;

create table app (
    id         bigint(20)  not null auto_increment comment 'app ID',
    tenant_id  bigint(20)  not null comment 'tenant ID',
    name       varchar(50) not null comment 'app name',
    code       varchar(50) not null comment 'app code',
    weight     int         not null default 0 comment 'app weight',
    created_at datetime    not null default current_timestamp,
    updated_at datetime    not null default current_timestamp on update current_timestamp,
    deleted    tinyint(1)  not null default 0 comment 'deleted',
    primary key (id)
) comment ='app table comment' charset utf8mb4;

create table app_task (
    id         bigint(20)  not null auto_increment comment 'task ID',
    tenant_id  bigint(20)  not null comment 'tenant ID',
    app_id     bigint(20)  not null comment 'app ID',
    name       varchar(50) not null comment 'task name',
    code       varchar(50) not null comment 'task code',
    params     text comment 'task params',
    weight     int         not null default 0 comment 'task weight',
    created_at datetime    not null default current_timestamp,
    updated_at datetime    not null default current_timestamp on update current_timestamp,
    deleted    tinyint(1)  not null default 0 comment 'deleted',
    primary key (id)
) comment ='app_task table comment' charset utf8mb4;
