drop table if exists user_table;

create table user_table (
  id int identity(1, 1) primary key,
  last_name varchar(20),
  firstname varchar(20)
);

insert into user_table (last_name, firstname)
values ('Doe', 'John'), ('Lane', 'Penny'), ('Petty', 'Tom');


drop table if exists company;

create table company (
  id int primary key,
  name varchar(20),
  address varchar(20)
);

insert into company (id, name, address) values
(1, 'Aldus', 'Washington');
