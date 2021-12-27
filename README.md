# Relational database access

## Terms and theory

There are two ways of accessing data in a relational database.

### Plain database access

This is usually a domain covered by JDBC, which is an ancient Java standard
for accessing relational databases.

You can access the database
execute queries using SQL and retrieve data as collections of objects.
There is some basic support for converting data between a table/query
column and its corresponding Java type.

Usually you need to use the native query language of the database engine
you are using, there is not much in the way of useful unification.

This approach has been superseded by ORM, but it should not be forgotten
as it provides simplicity and freedom which ORM does not.

It is also good to know about JDBC as ORM frameworks are usually using
it as a base layer.

#### Spring Data JDBC

Spring Data JDBC module simplifies the way of using plain JDBC providing
developers with some high(er) level of abstractions above the original
API.

#### Drivers

If you want to access a database engine using JDBC, you need the appropriate
driver. Most of the currently available databases provide such drivers,
there are even some pretty obscure formats are accessible this way like
MS Excel, DBF and many other.

### ORM - Object-Relational Mapping

ORM is in the Java world represented by the JPA standard, and provides
developers with a way of persisting entire Java objects and/or collections.
It provides powerful tools for manipulating and querying the stored data.

As many other Java standards, JPA is just an API specification. There are
multiple implementations, perhaps the most widely known opensource implementation
is Hibernate. On top of JDBC drivers for specific databases, Hibernate introduces
dialects which are used as a bridge between the varying SQL implementations and the
unified 
[JPA query language (JPQL)](https://docs.oracle.com/javaee/6/tutorial/doc/bnbrg.html).

#### Spring Data JPA

As always, Spring brings new possibilities to the world of JPA.

One small improvement is an injectable `EntityManager` instance, but the
most important is the introduction of
[Spring Data repositories](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.repositories)
and [transaction management](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#transactions).

The `@Transactional` annotation is usable not only within repositories, but also 
in services on class (all methods within class are transactional) or method level.
Just remember what you know about proxies and annotations, before you run
into problems. If you need to call a transactional method from another method
within the same class, the easies method is to mark the calling method
as `@Transactional` as well.

The repositories allow you to query/create/modify/delete your entities
without implementing a single line of code. You can either follow the
[naming conventions](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation),
and construct the queries just by naming the repository interface methods
correctly.
Or you can use the
[`@Query` annotation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.at-query),
if the naming conventions are not powerful enough (or the method name is way to
long and complicated).

### Schema management

For both, JDBC and JPA, you need some mechanism to manage the database schema.
There are many useful tools developed just for this purpose. We are going
to manage the schema manually here, but you can take a look at some of
the tools available and perhaps try using one:

* [Liquibase](https://www.liquibase.org/)
* [Flyway](https://flywaydb.org/)

They allow you to set up the schema for the first time, but also to
upgrade the schema as your development process continues.

Hibernate also provides a way of creating/updating its schema, but I don't 
recommend it for production purposes. And it's not a good idea (or perhaps
impossible) to mix this way with the tools mentioned above.

## Tasks

### Explore the sample project

This project's main class is `cz.zcu.fav.pia.jsf.backend.DataApp`. Again, it's
a JSF/Spring Boot application which contains only two pages.

#### `index.xhtml`

This page only shows the logged user and their assigned roles.

#### `login.xhtml`

This is the login screen which on top of the usual user name and password
also contains a login method selector. Each method represents a different
implementation. Currently only the fake one is fully implemented.

To find out which method is implemented by which class, take a look at the
classes in the `cz.zcu.fav.pia.jsf.auth.service.impl.method` package. The names
displayed on the login screen correspond with the service names defined
in their respective `@Service` annotations:

```
@Service("fake")
public class FakeAuthServiceImpl implements UserDetailsServiceInternal {
	...
}
```

You simply fill in the form, select an authentication method and press login.
For convenience, the application remembers your last auth method.

### Run the PostgreSQL database

To run your PostgreSQL database, use this Docker command:

```
docker run --name pg -ePOSTGRES_PASSWORD=superstrongpassword -ePOSTGRES_USER=piadata -d --rm --shm-size=256MB -p5432:5432 postgres
```

or if you are using Podman:

```
podman run --name pg -ePOSTGRES_PASSWORD=superstrongpassword -ePOSTGRES_USER=piadata -d --rm --shm-size=256MB -p5432:5432 postgres
```

This way you run a one-time PostgreSQL container which will be removed
(with all its stored data) on termination:

```
docker stop pg
```

of:

```
podman stop pg
```

#### Volume backend.configuration

If you want to make the storage permanent, you can externalize the
storage directory using volumes.

To create a volume (do it just once):

```
docker volume create pgdata
```

Then mount the volume to the `/var/lib/postgresql/data` directory using the `-v` option.
You also have to tell PostgreSQL to use a subdirectory as the owner of the root of
the mout cannot be changed, this is done by setting the `PGDATA` variable:

```
docker run --name pg -ePOSTGRES_PASSWORD=superstrongpassword -ePOSTGRES_USER=piadata \
	-ePGDATA=/var/lib/postgresql/data/pgdata -d --rm --shm-size=256MB -p5432:5432 \
	-v pgdata:/var/lib/postgresql/data postgres
```

When you do persist your database, you can create the schema manually using
the `psql` tool:

```
$ docker exec -ti pg psql -Upiadata
psql (13.1 (Debian 13.1-1.pgdg100+1))
Type "help" for help.

piadata=# 
```

To exit the tool, type `\q` and enter (Ctrl+D does the same, but I'm not sure
whether it works in Windows):

```
piadata=# \q
$
```

To create the sample table necessary to run the project in its current state:

```
piadata=# CREATE TABLE SAMPLE (id UUID PRIMARY KEY);
CREATE TABLE
piadata=#
```

This way you don't have to manage the schema within the application.

You can use your own installation or even your own database engine,
but the project is configured for PostgreSQL started as shown above.

### Create all necessary tables

You can find example schema and sample data
[here](https://grokonez.com/spring-framework/spring-security/use-spring-security-jdbc-authentication-postgresql-spring-boot#4_Configure_Database),
but feel free to experiment.

If you decide not to [use persistent volumes](#volume-configuration), you need to
add a `@PostConstruct` method to any service within the project (perhaps one of the
authentication methods, they have the database access prepared after all) and
create the schema there.

For the code currently implemented to work, at least the following table must exist:

```
CREATE TABLE SAMPLE (id UUID PRIMARY KEY);
```

As you can see we are using UUID as a primary key type.

### Implement all authentication methods

In the `cz.zcu.fav.pia.jsf.auth.service.impl.method` package, there are
skeletons of a few possible authentication methods.

Finish the implementation of each one using the proposed way
of accessing the database. The classes contain a hint
on how the selected technology should be used.

#### `PlainJdbcAuthServiceImpl`

Try implementing the authentication here using just the plain
JDBC connection.

#### `JdbcTemplateAuthServiceImpl`

This one should be implemented using the Spring's `JdbcTemplate`.

#### JPA Entities

All JPA related tasks can share the entities so I recommend preparing
them first. You can use the first two entities from
[this tutorial](https://www.baeldung.com/role-and-privilege-for-spring-security-registration#user-role-and-privilege).

#### `JpaJpqlAuthServiceImpl`

Inject an `EntityManager` here and use its `createQuery` methods, look
[here](https://docs.oracle.com/javaee/6/tutorial/doc/bnbrg.html) for inspiration.

#### `JpaCriteriaAuthServiceImpl` - **Optional**

[Criteria API](https://www.objectdb.com/java/jpa/query/criteria)
is an advanced and very powerful tool, but it's rather complicated.
I recommend at least reading a few paragraphs about it just to remember
that it's available and what its purpose is.

#### `JpaSpringDataRepoAuthServiceImpl`

I would like you to implement this one from scratch using
[this tutorial](https://spring.io/guides/gs/accessing-data-jpa/).

## Resources & Links

* [JSF 2.3 Taglib #1 - prefixes f: and ui:](https://javaserverfaces.github.io/docs/2.3/vdldoc/index.html)
* [JSF 2.3 Taglib #2 - prefix h:](https://javaserverfaces.github.io/docs/2.3/vdldocs/facelets/index.html)
