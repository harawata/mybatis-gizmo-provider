-- @DELIMITER $
begin
  execute immediate 'drop table user_table';
exception
  when others then
    if sqlcode != -942 then
      raise;
    end if;
end;
$
-- @DELIMITER ;

create table user_table (
  id int generated always as identity,
  last_name varchar(20),
  firstname varchar(20)
);

insert into user_table (last_name, firstname) values ('Doe', 'John');
insert into user_table (last_name, firstname) values ('Lane', 'Penny');
insert into user_table (last_name, firstname) values ('Petty', 'Tom');


-- @DELIMITER $
begin
  execute immediate 'drop table company';
exception
  when others then
    if sqlcode != -942 then
      raise;
    end if;
end;
$
-- @DELIMITER ;

create table company (
  id int primary key,
  name varchar(20),
  address varchar(20)
);

insert into company (id, name, address) values
(1, 'Aldus', 'Washington');
