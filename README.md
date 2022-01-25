# Hibernate soft deletion

Soft deletion is a widely used pattern applied for business applications. It allows you to mark some records as deleted without actual erasure from the database. Effectively, you prevent a soft-deleted record from being selected, meanwhile all old records can still refer to it.

Hibernate soft deletion provides a few ways to add soft deletion to Hibernate entities.

All soft deleted entities should have `@SoftDelete` annotation.

Soft delete column can be declared in

1) `@SoftDelete` annotation with column or property name

Example:
```java
@Entity
@SoftDelete(column = "DELETED_AT", type = Date.class)
public class Clinic {

    @Id
    private Long id;

    private String name;
```

2) `@SoftDeleteColumn` annotation on entity property

Example
```java
@Entity
@SoftDelete
public class Owner {

    @Id
    private Long id;

    private String name;

    @SoftDeleteColumn
    @Column(nullable = false)
    private Boolean isDeleted = false;
```

Soft deleted column supported following column types:
- Date - soft deletion will be marked with current date
- Boolean - true if entity was soft deleted
- Integer - 1 if entity was soft deleted
- String - 'deleted' is entity was soft deleted

This library is tested with Hibernate 5.5.x

_Currently still under development_

