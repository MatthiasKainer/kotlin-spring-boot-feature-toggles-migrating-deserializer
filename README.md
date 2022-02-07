# Spring Boot Kotlin Starter

Spring Boot Kotlin Starter project with some copy & pasting from [MBurchard/spring-boot-kotlin-starter](https://github.com/MBurchard/spring-boot-kotlin-starter) for sanitity

Shows a simplified usage of feature toggles and with a migrating deserializers with Spring Boot

## Environment

The services live in a microservice ecosystem deployed into multiple regions worldwide. It is deployed as a blue-green instance, and the requirement the team has is that support can switch it back to the inactive instance at any time after every deployment in case bugs pop up. Depending on auto-scaling requirements, the service can run 1-x times in each blue-green instance. Migration of the database happens via an external application, and it tracks the currently applied migration script in a separate table.

There is an independently deployed frontend application that uses the API, and the teams cannot orchestrate deployments to happen at the same time.

Downtimes are not okay.

## Scenario

For this example, we have one toggle: `example`. This toggle is created for a heavy change in the application's data structure. Before, we had an API that returns a JSON looking like this:

```json
{
    "name":"lastName, firstName",
    "email":"none"
}
```

from a database table looking like this:

```sql
TABLE Users(id VARCHAR, name VARCHAR)
```

After the feature is implemented, the JSON response 

```json
{
    "id":"1",
    "firstName":"firstName",
    "middleName":"middleName",
    "lastName":"lastName",
    "email":"email"
}
```

is based on the migrated database table

```sql
TABLE Users(id VARCHAR, firstName VARCHAR, lastName VARCHAR, email VARCHAR)
```

Even though this is a breaking change, the team will not increment the API version for the sake of this excercise.

## Approach

The solution approach is adding a feature toggle `example` and using the database version to decide how to load the User. 

```
┌────────────┐    ┌─────────────┐   ┌────────────┐  ┌────────────┐   ┌───────────┐
│            │    │             │   │            │  │            │   │           │
│   API      │    │ Controller  │   │ Toggle w   │  │ Repository │   │ Datatbase │
│ Consumer   │    │             │   │ ViewModel  │  │            │   │ Tables    │
│            │    │             │   │ Mapper     │  │            │   │           │
└─────┬──────┘    └──────┬──────┘   └──────┬─────┘  └──────┬─────┘   └─────┬─────┘
      │                  │                 │               │               │
      │  request user    │                 │               │               │
      ├─────────────────►│                 │               │               │
      │                  │  request user from database     │               │
      │                  ├─────────────────┬──────────────►│               │
      │                  │                 │               │  get version  │
      │                  │                 │               ├──────────────►│
      │                  │                 │               │◄──────────────┤
      │                  │                 │               │               │
      │                  │                 │               │ load user based
      │                  │                 │               │ on version    │
      │                  │                 │               ├──────────────►│
      │                  │                 │               │ fields        │
      │                  │                 │               │◄──────────────┤
      │                  │                 │               │               │
      │                  │                 │               ├────────────┐  │
      │                  │                 │               │ map fields │  │
      │                  │                 │               │ into       │  │
      │                  │                 │               │ model      │  │
      │                  │                 │               │◄───────────┘  │
      │                  │◄────────────────┴───────────────┤               │
      │                  │ send the db model to the mapper │               │
      │                  ├────────────────►│               │               │
      │                  │                 │               │               │
      │                  │ result based on toggle          │               │
      │                  │                 │               │               │
      │                  │◄────────────────┤               │               │
      │◄─────────────────┘                 │               │               │
```

As we can see, there are two branches in this approach - the loading of the User from the database and the mapping to the view model. This first step allows us to decouple the database migration from the feature release. 

## Feature Toggles

Multiple strategies can set the feature toggles in this application:

- Via Environment, ie by `features_toggles_example=false ./gradlew bootRun`
- Via Configuration, ie by setting `features.toggles.example=false` in the application configuration
- Via QueryString, by passing `features[example]=false` with url encoded [] to the endpoints

A working example for this can be seen in the test: [test/../controllers/UserControllerTests.kt](src/test/kotlin/de/matthiaskainer/acme/controllers/UserControllerTests.kt)

The implementation is rather simple. The map of the feature toggles and the map of the query string toggles is united, and the `User.toViewModel` mapper decides by a `if (toggles.getOrDefault("example", false))` statement. In the first case, the new view model is returned, otherwise the old.

The code is implemented in [main/../controllers/UserController.kt](src/main/kotlin/de/matthiaskainer/acme/controllers/UserController.kt).

## Migrating Deserializer

Having the database in a well-defined state is more complicated. Blue/Green Deployments and the requirement to roll back the application at any given time is a difficult feat. On top of that, with global rollout depending on different pipelines, the database in Asia might be migrated to the newest version. In contrast, the database in South America is still migrating. The deployed applications will have to be able to work with both.

A valid approach that solves a few of the problems we have seen is a serializer for your data objects that follow your migrations.

The idea is relatively simple. By checking the current table version (based on the version your migration script creates), you decide how to deserialize the data into an object.

The result can be seen in the test [test/../repositories/UserRepositoryTests.kt](src/test/kotlin/de/matthiaskainer/acme/repositories/UserRepositoryTests.kt). The feature state is defined in the properties for the Application.

Let's quickly compare how our tables look.

### v1 table

| id  | name                |
| --- | ------------------- |
| 1   | LastName, FirstName |

### v2 table

| id  | FirstName  | MiddleName  | LastName  | email |
| --- | ---------- | ----------- | --------- | ----- |
| 1   | First Name | Middle Name | Last Name | Email |

The [main/../repositories/UserRepository.kt](src/main/kotlin/de/matthiaskainer/acme/repositories/UserRepository.kt) returns a single type already representing the second version. By using a mapper, it can decide based on the version which entity to return:

```kt
fun splitUserName(name: String) = name.split(", ")

fun ResultSet.toUser(version: String): User =
    if (version == "1") splitUserName(getString("name")).let { User(getString("id"), it[0], it[1]) }
    else User(getString("id"), getString("lastName"), getString("firstName"), getString("email"))
```

In a real world application you would propably want to create some abstraction for that, but the idea would stay the same. You get the version from the database, and based on it you create the entity. This can and should obviously be further optimized; this is a simplified example showing the general concept. 
## The Special Case Of The Appearing Email

Looking at the test [`when requesting the endpoint with the toggle off, and the feature toggle is on, it still returns the old schema`](src/test/kotlin/de/matthiaskainer/acme/controllers/UserControllerTests.kt), we can see that even though we receive the old schema, the email is set. That is a very edgy case and fabricated in that scenario as the email wasn't in the database before. Something like this might happen, though, and it's relevant to decide how to return the data. In our case, we returned it, including the email address that could not have been available before. This is to avoid data loss in case of a feature rollback of the API endpoint. We should ideally persist the email also in other scenarios like edits.