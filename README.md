# flyway-play

![Scala CI](https://github.com/ponkotuy/flyway-play/actions/workflows/scala.yml/badge.svg)

Flyway module for Play 2.4 or later. It aims to be a substitute for play-evolutions.

This is a fork of [playframework/flyway-play](https://github.com/playframework/flyway-play),
published to Maven Central as `com.ponkotuy % flyway-play`. The main difference is support for newer Flyway versions.

## <a class="anchor" name="features"></a>Features

- Based on [Flyway](https://flywaydb.org/)
- No 'Downs' part.
- Independent of DBPlugin(play.api.db).

## <a class="anchor" name="install"></a>Install

| flyway-play version | play version | flyway version |
| ------------------- | ------------ | -------------- |
| 10.2.0              | 3.0.x        | 12.11.0        |
| 10.1.0              | 3.0.x        | 11.8.2         |
| 10.0.0              | 3.0.x        | 10.11.1        |

build.sbt

```scala
libraryDependencies ++= Seq(
  "com.ponkotuy" %% "flyway-play" % "10.2.0"
)
```

Since Flyway 10, support for most databases has been split out of `flyway-core`
into separate modules. Only H2 and SQLite are still bundled. For any other
database, add the corresponding module to your dependencies. For example, for
MySQL:

```scala
libraryDependencies ++= Seq(
  "org.flywaydb" % "flyway-mysql" % "12.11.0"
)
```

The module version should match the `flyway-core` version that flyway-play
pulls in (see the table above).

| Database                                     | Artifact                    |
| -------------------------------------------- | --------------------------- |
| MySQL / MariaDB / Aurora MySQL               | flyway-mysql                |
| PostgreSQL / Aurora PostgreSQL / CockroachDB | flyway-database-postgresql  |
| SQL Server / Azure Synapse                   | flyway-sqlserver            |
| Oracle                                       | flyway-database-oracle      |
| DB2                                          | flyway-database-db2         |
| HSQLDB                                       | flyway-database-hsqldb      |
| Derby                                        | flyway-database-derby       |
| Snowflake                                    | flyway-database-snowflake   |
| Redshift                                     | flyway-database-redshift    |
| H2 / SQLite                                  | (bundled in flyway-core)    |

For other databases, see the [Flyway database driver reference](https://documentation.red-gate.com/flyway/reference/database-driver-reference)
and the `flyway-database-*` artifacts under
[org.flywaydb on Maven Central](https://central.sonatype.com/namespace/org.flywaydb).

Versions up to 9.1.0 were published by upstream as `"org.flywaydb" %% "flyway-play"`.

conf/application.conf

```
play.modules.enabled += "org.flywaydb.play.PlayModule"
```

## Maintenance

This fork is maintained by [@ponkotuy](https://github.com/ponkotuy).
The upstream project [playframework/flyway-play](https://github.com/playframework/flyway-play) is a community project
maintained by [@tototoshi](https://github.com/tototoshi), and is not officially maintained by the Flyway Team at Redgate.

## <a class="anchor" name="getting-started"></a>Getting Started

### Basic configuration

Database settings can be set in the manner of Play2.

```
db.default.driver=org.h2.Driver
db.default.url="jdbc:h2:mem:example2;db_CLOSE_DELAY=-1"
db.default.username="sa"
db.default.password="secret"

# optional
db.default.migration.schemas=["public", "other"]
```

### Place migration scripts

A migration script is just a simple SQL file.

```sql
CREATE TABLE FOO (.............


```

By default place your migration scripts in `conf/db/migration/${dbName}` .

If scriptsDirectory parameter is set, it will look for migrations scripts in `conf/db/migration/${scriptsDirectory}` .

```
playapp
├── app
│   ├── controllers
│   ├── models
│   └── views
├── conf
│   ├── application.conf
│   ├── db
│   │   └── migration
│   │       ├── default
│   │       │   ├── V1__Create_person_table.sql
│   │       │   └── V2__Add_people.sql
│   │       └── secondary
│   │           ├── V1__create_job_table.sql
│   │           └── V2__Add_job.sql
│   ├── play.plugins
│   └── routes
```

Alternatively, specify one or more locations per database and place your migrations in `conf/db/migration/${dbName}/${locations[1...N]}` . By varying the locations in each environment you are able to specify different scripts per RDBMS for each upgrade. These differences should be kept minimal.

For example, in testing use the configuration:

```
db.default.migration.locations=["common","h2"]
```

And in production use the configuration:

```
db.default.migration.locations=["common","mysql"]
```

Then put your migrations in these folders. Note that the migrations for the `secondary` database remain in the default location.

```
playapp
├── app
│   ├── controllers
│   ├── models
│   └── views
├── conf
│   ├── application.conf
│   ├── db
│   │   └── migration
│   │       ├── default
│   │       │   ├── common
│   │       │   │   └── V2__Add_people.sql
│   │       │   ├── h2
│   │       │   │   └── V1__Create_person_table.sql
│   │       │   └── mysql
│   │       │       └── V1__Create_person_table.sql
│   │       └── secondary
│   │           ├── V1__create_job_table.sql
│   │           └── V2__Add_job.sql
│   ├── play.plugins
│   └── routes
```

Please see flyway's documents about the naming convention for migration scripts.

https://flywaydb.org/documentation/migration/sql.html

### Placeholders

Flyway can replace placeholders in Sql migrations.
The default pattern is ${placeholder}.
This can be configured using the placeholderPrefix and placeholderSuffix properties.

The placeholder prefix, suffix, and key-value pairs can be specified in application.conf, e.g.

```
db.default.migration.placeholderPrefix="$flyway{{{"
db.default.migration.placeholderSuffix="}}}"
db.default.migration.placeholders.foo="bar"
db.default.migration.placeholders.hoge="pupi"
```

This would cause

```sql
INSERT INTO USERS ($flyway{{{foo}}}) VALUES ('$flyway{{{hoge}}}')
```

to be rewritten to

```sql
INSERT INTO USERS (bar) VALUES ('pupi')
```

### Enable/disable Validation

From flyway 3.0, `validate` run before `migrate` by default.
Set `validateOnMigrate` to false if you want to disable this.

```
db.${dbName}.migration.validateOnMigrate=false // true by default
```

### Migration prefix

Custom sql migration prefix key-value pair can be specified in application.conf:

```
db.${dbName}.migration.sqlMigrationPrefix="migration_"
```

### Insert Query

If you want to apply your migration not via the web interface, but manually on
your production databases you also need the valid insert query for flyway.

```
db.${dbName}.migration.showInsertQuery=true
```

### Dev

![screenshot](screenshot1.png)

For existing schema, Flyway has a option called 'initOnMigrate'. This option is enabled when `-Ddb.${dbName}.migration.initOnMigrate=true`.
For example,

```
$ play -Ddb.default.migration.initOnMigrate=true
```

Of course, You can write this in your `application.conf`.

Manual migration is also supported. Click 'Other operations' or open `/@flyway/${dbName}` directly.

![screenshot](screenshot2.png)

### Test

In Test mode, migration is done automatically.

### Prod

In production mode, migration is done automatically if `db.${dbName}.migration.auto` is set to be true in application.conf.
Otherwise, it failed to start when migration is needed.

```
$ play -Ddb.default.migration.auto=true start
```

## <a class="anchor" name="example"></a>Example application

[seratch/devteam-app](https://github.com/scalikejdbc/devteam-app "seratch/devteam-app") is using play-flyway. Maybe this is a good example.

## compile-time DI support

```scala
class MyComponents(context: Context)
    extends BuiltInComponentsFromContext(context)
    with FlywayPlayComponents
    ...
    {
  flywayPlayInitializer
  ...
}
```

## <a class="anchor" name="license"></a>License

- Apache 2.0 License
