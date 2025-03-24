drop table if exists user_table;

create table user_table (
  id int primary key auto_increment,
  last_name varchar(20),
  firstname varchar(20)
);

insert into user_table (id, last_name, firstname)
values (1, 'Doe', 'John'), (2, 'Lane', 'Penny'), (3, 'Petty', 'Tom');


drop table if exists company;

create table company (
  id int primary key,
  name varchar(20),
  address varchar(20)
);

insert into company (id, name, address) values
(1, 'Aldus', 'Washington');
