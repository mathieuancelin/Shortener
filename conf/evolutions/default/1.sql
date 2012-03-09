# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table shorten (
  id                        bigint not null,
  url                       varchar(255),
  constraint pk_shorten primary key (id))
;

create sequence shorten_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists shorten;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists shorten_seq;

