create table if not exists BATCH_JOB_INSTANCE (
    JOB_INSTANCE_ID bigint not null primary key,
    VERSION bigint,
    JOB_NAME varchar(100) not null,
    JOB_KEY varchar(32) not null,
    constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) engine=InnoDB;

create table if not exists BATCH_JOB_EXECUTION (
    JOB_EXECUTION_ID bigint not null primary key,
    VERSION bigint,
    JOB_INSTANCE_ID bigint not null,
    CREATE_TIME datetime(6) not null,
    START_TIME datetime(6) default null,
    END_TIME datetime(6) default null,
    STATUS varchar(10),
    EXIT_CODE varchar(2500),
    EXIT_MESSAGE varchar(2500),
    LAST_UPDATED datetime(6),
    constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
        references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) engine=InnoDB;

create table if not exists BATCH_JOB_EXECUTION_PARAMS (
    JOB_EXECUTION_ID bigint not null,
    PARAMETER_NAME varchar(100) not null,
    PARAMETER_TYPE varchar(100) not null,
    PARAMETER_VALUE varchar(2500),
    IDENTIFYING char(1) not null,
    constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) engine=InnoDB;

create table if not exists BATCH_STEP_EXECUTION (
    STEP_EXECUTION_ID bigint not null primary key,
    VERSION bigint not null,
    STEP_NAME varchar(100) not null,
    JOB_EXECUTION_ID bigint not null,
    CREATE_TIME datetime(6) not null,
    START_TIME datetime(6) default null,
    END_TIME datetime(6) default null,
    STATUS varchar(10),
    COMMIT_COUNT bigint,
    READ_COUNT bigint,
    FILTER_COUNT bigint,
    WRITE_COUNT bigint,
    READ_SKIP_COUNT bigint,
    WRITE_SKIP_COUNT bigint,
    PROCESS_SKIP_COUNT bigint,
    ROLLBACK_COUNT bigint,
    EXIT_CODE varchar(2500),
    EXIT_MESSAGE varchar(2500),
    LAST_UPDATED datetime(6),
    constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) engine=InnoDB;

create table if not exists BATCH_STEP_EXECUTION_CONTEXT (
    STEP_EXECUTION_ID bigint not null primary key,
    SHORT_CONTEXT varchar(2500) not null,
    SERIALIZED_CONTEXT text,
    constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
        references BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
) engine=InnoDB;

create table if not exists BATCH_JOB_EXECUTION_CONTEXT (
    JOB_EXECUTION_ID bigint not null primary key,
    SHORT_CONTEXT varchar(2500) not null,
    SERIALIZED_CONTEXT text,
    constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) engine=InnoDB;

create table if not exists BATCH_STEP_EXECUTION_SEQ (
    ID bigint not null,
    UNIQUE_KEY char(1) not null,
    constraint BATCH_STEP_EXECUTION_SEQ_UN unique (UNIQUE_KEY)
) engine=InnoDB;

insert into BATCH_STEP_EXECUTION_SEQ (ID, UNIQUE_KEY)
select * from (select 0 as ID, '0' as UNIQUE_KEY) as tmp
where not exists (select * from BATCH_STEP_EXECUTION_SEQ);

create table if not exists BATCH_JOB_EXECUTION_SEQ (
    ID bigint not null,
    UNIQUE_KEY char(1) not null,
    constraint BATCH_JOB_EXECUTION_SEQ_UN unique (UNIQUE_KEY)
) engine=InnoDB;

insert into BATCH_JOB_EXECUTION_SEQ (ID, UNIQUE_KEY)
select * from (select 0 as ID, '0' as UNIQUE_KEY) as tmp
where not exists (select * from BATCH_JOB_EXECUTION_SEQ);

create table if not exists BATCH_JOB_INSTANCE_SEQ (
    ID bigint not null,
    UNIQUE_KEY char(1) not null,
    constraint BATCH_JOB_INSTANCE_SEQ_UN unique (UNIQUE_KEY)
) engine=InnoDB;

insert into BATCH_JOB_INSTANCE_SEQ (ID, UNIQUE_KEY)
select * from (select 0 as ID, '0' as UNIQUE_KEY) as tmp
where not exists (select * from BATCH_JOB_INSTANCE_SEQ);
