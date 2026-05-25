# Proposal: real SparkSessionExtensions

## Intent
At the moment SQL-functions should be registered manually -- not working via "plugin" + pyspark / sql

## Scope
- Add a real SparkSessionExtension
- It registers all the SQL functions
- Should work as cluster/spark conf for pyspark/sql users

## What Changes: real SparkSessionExtensions
New saprk session extension; old one should be kept for advanced users/JVM library devs

## Approach
New class, tests, regression tests.
