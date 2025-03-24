# MyBatis Gizmo Provider

mybatis-gizmo-provider is MyBatis' SQL provider implementation that auto-generates INSERT, UPDATE and UPSERT statements at runtime using reflection.

It does not support SELECT nor DELETE because SQL for these statements are either too simple or too complex for auto-generation, IMHO.

## Requirements

- Java 11
- MyBatis 3.5.6 or later (use the [latest](https://mvnrepository.com/artifact/org.mybatis/mybatis) if possible)

## Supported databases

There are implementations for MySQL, Oracle and MS SQL Server.
The OracleProvider does not support UPSERT.

Note: these providers are not thoroughly tested.

## Quick start

Let's assume we have a simple table and a Java class.

```sql
create table company (
  id int primary key,
  name varchar(20),
  address varchar(20)
);
```

```java
public class Company {
  private Integer id;
  private String name;
  private String address;
  // getters, setters
}
```

By adding the following methods to your mapper, the provider generates the `INSERT` and `UPDATE` statements for you.

```java
@InsertProvider(MysqlProvider.class)
int insert(Company company);

@UpdateProvider(MysqlProvider.class)
int updateById(Company company);
```

The above mapper method declarations are basically equivalent to the below.

```java
@Insert({
  "insert into company (id, name, address) values ",
  "(#{id}, #{name}, #{address})"
})
int insert(Company company);

@Update({
  "update company set ",
  "id = #{id}, name = #{name}, address = #{address} ",
  "where id = #{id}"
})
int updateById(Company company);
```

Lastly, it is worth mentioning that the annotation can be simpler if you set the `defaultSqlProviderType` in the config.

```java
@InsertProvider
int insert(Company company);

@UpdateProvider
int updateById(Company company);
```

## Documentation


### `@Table` annotation

mybatis-gizmo-provider assumes the first argument of the mapper method is the class that corresponds to the target table.
And the name of the target table is determined by the class name, by default.

If the first argument type is `User`, the provider assumes the table name is `user`, if the  argument type is `AddressBook`, the table name is assumed to be `address_book`.
To specify the table name explicitly, use `jakarta.persistence.Table`.

```java
@Table(name = "cmp")
public class Company {
```

If the table has to be referenced with schema, specify `catalog` or `schema`.

```java
@Table(name = "cmp", catalog = "foo")
public class Company {
```

### `@Column` annotation

You can add `jakarta.persistence.Column` annotation on a field to customize the followings.

- If `name` is specified, its value will be used as the column name.
- If `insertable = false` is specified, the field will be ignored in `INSERT` statement.
- If `updatable = false` is specified, the field will be ignored in `UPDATE` statement.


### INSERT

To generate INSERT statement, the mapper method must satisfy the following rules.

- the mapper method name must start with `insert`.
- the mapper method must take one argument.



### UPDATE

To generate UPDATE statement, the mapper method must satisfy the following rules.

- the mapper method name must start with `update`.
- if there are multiple arguments, they will be used in the `WHERE` clause.
- if the method takes only one argument, you must specify column names in the method name.

This one uses the method arguments as conditions.

```java
@UpdateProvider(MysqlProvider.class)
int update(Member member, Integer age, Integer gender);
```

```sql
update member set ... where age = #{age} and gender = #{gender}
```

This one uses the properties as conditions.
You can specify multiple properties/columns as follows.

```java
@UpdateProvider(MysqlProvider.class)
int updateByAgeAndGender(Member member);
```

```sql
update member set ... where age = #{age} and gender = #{gender}
```

Currently, Gizmo provider does not support generating `UPDATE` statement with no `WHERE` clause.


### UPSERT

To generate UPSERT statement, the mapper method must satisfy the following rules.

- the mapper method name must start with `upsert`.
- With `MysqlProvider`, there can be only one argument.
- With `MssqlProvider`, there must be multiple arguments which will be used in `ON` clause of `MERGE INTO` statement.

Note: `OracleProvider` does not support UPSERT.

#### MysqlProvider example

```java
@InsertProvider(MysqlProvider.class)
int upsertMember(Member member);
```

```sql
insert into member (id, ...) values (#{id}, ...) 
as newrow on duplicate key update id = newrow.id, ...
```

#### MssqlProvider example

```java
@InsertProvider(MssqlProvider.class)
int upsertMemberOnId(Member member);
```

```sql
merge into member with (holdlock) as desttbl
using (select #{id} id, ...) as srctbl
on (desttbl.id = srctbl.id)
when matched then update id = #{id}, ...
when not matched then insert (id, ...) values (srctbl.id, ...);
```


## Bugs, feature requests

Please use the GitHub issues.

I intend to keep this provider as simple as possible.
If you want/need to generate advanced SQLs, you should write your own provider. Feel free to copy and modify the source of this project if it helps.

## License

MIT License
