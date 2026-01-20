---
name: sqldelight
description: SQLDelight database patterns for KMP. Use when working with database schemas, queries, migrations, or local data persistence.
---

# SQLDelight Skill

## Contents

- [Overview](#overview)
- [Directory Structure](#directory-structure)
- [Reference Files](#reference-files)
- [Schema Patterns](#schema-patterns)
- [Query Patterns](#query-patterns)
- [Sync Patterns](#sync-patterns)
- [Driver Setup](#driver-setup)

---

## Overview

SQLDelight generates type-safe Kotlin APIs from SQL:
- Write SQL, get Kotlin
- Compile-time verification
- Flow support for reactive queries
- Cross-platform (Android, iOS, JVM, JS)

---

## Directory Structure

```
shared/src/commonMain/sqldelight/
└── com/example/database/
    ├── User.sq                # User table and queries
    ├── Book.sq                # Book table and queries
    └── migrations/
        ├── 1.sqm              # Migration v1 -> v2
        └── 2.sqm              # Migration v2 -> v3
```

---

## Reference Files

| Topic | File | Description |
|-------|------|-------------|
| **Schema** | [reference/schema.md](reference/schema.md) | Table definitions, types, constraints |
| **Migrations** | [reference/migrations.md](reference/migrations.md) | Migration scripts, versioning |

---

## Schema Patterns

### Basic Table

```sql
-- User.sq
CREATE TABLE User (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
);
```

### With Sync Support

```sql
-- Book.sq
CREATE TABLE Book (
    id TEXT NOT NULL PRIMARY KEY,
    title TEXT NOT NULL,
    authorId TEXT NOT NULL,
    isbn TEXT,

    -- Sync metadata
    version INTEGER NOT NULL DEFAULT 1,
    syncStatus TEXT NOT NULL DEFAULT 'synced',
    isDeleted INTEGER NOT NULL DEFAULT 0,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,

    FOREIGN KEY (authorId) REFERENCES Author(id)
);

CREATE INDEX book_author_idx ON Book(authorId);
CREATE INDEX book_sync_status_idx ON Book(syncStatus);
```

### Sync Status Enum

```sql
-- Use TEXT for enum-like values
-- Values: 'synced', 'pending', 'conflict'
```

---

## Query Patterns

### Basic CRUD

```sql
-- User.sq

-- Select all
selectAll:
SELECT * FROM User ORDER BY name ASC;

-- Select by ID
selectById:
SELECT * FROM User WHERE id = ?;

-- Insert
insert:
INSERT INTO User (id, name, email, createdAt, updatedAt)
VALUES (?, ?, ?, ?, ?);

-- Update
update:
UPDATE User SET name = ?, email = ?, updatedAt = ? WHERE id = ?;

-- Delete
delete:
DELETE FROM User WHERE id = ?;
```

### Flow Queries (Reactive)

```sql
-- These automatically return Flow<List<T>>
selectAllAsFlow:
SELECT * FROM User ORDER BY name ASC;

selectByIdAsFlow:
SELECT * FROM User WHERE id = ?;
```

### Soft Delete Pattern

```sql
-- Book.sq

-- Select only non-deleted
selectAll:
SELECT * FROM Book WHERE isDeleted = 0 ORDER BY title ASC;

-- Soft delete
softDelete:
UPDATE Book SET
    isDeleted = 1,
    syncStatus = 'pending',
    updatedAt = ?
WHERE id = ?;

-- Hard delete (for sync cleanup)
hardDelete:
DELETE FROM Book WHERE id = ? AND syncStatus = 'synced';

-- Select pending sync
selectPendingSync:
SELECT * FROM Book WHERE syncStatus = 'pending';
```

### Version Increment

```sql
-- Update with version bump
updateWithVersion:
UPDATE Book SET
    title = ?,
    authorId = ?,
    version = version + 1,
    syncStatus = 'pending',
    updatedAt = ?
WHERE id = ?;
```

---

## Sync Patterns

### SyncStatus Values

| Status | Meaning |
|--------|---------|
| `synced` | Data matches server |
| `pending` | Local changes need sync |
| `conflict` | Conflict detected during sync |

### Sync Workflow

```kotlin
// Repository with sync support
class BookRepository(
    private val database: AppDatabase,
    private val api: BookApi
) {
    private val queries = database.bookQueries

    // Local operations mark as pending
    suspend fun updateBook(book: Book) {
        queries.updateWithVersion(
            title = book.title,
            authorId = book.authorId,
            updatedAt = currentTimeMillis(),
            id = book.id
        )
    }

    // Sync pushes pending and pulls updates
    suspend fun sync() {
        // Push local changes
        val pending = queries.selectPendingSync().executeAsList()
        for (book in pending) {
            try {
                api.updateBook(book.toDto())
                queries.markSynced(book.id)
            } catch (e: ConflictException) {
                queries.markConflict(book.id)
            }
        }

        // Pull remote changes
        val remote = api.getBooks(lastSyncTimestamp)
        for (dto in remote) {
            queries.upsert(dto.toEntity())
        }
    }
}
```

### Upsert Pattern

```sql
-- Book.sq
upsert:
INSERT INTO Book (id, title, authorId, version, syncStatus, isDeleted, createdAt, updatedAt)
VALUES (?, ?, ?, ?, 'synced', ?, ?, ?)
ON CONFLICT(id) DO UPDATE SET
    title = excluded.title,
    authorId = excluded.authorId,
    version = excluded.version,
    syncStatus = 'synced',
    isDeleted = excluded.isDeleted,
    updatedAt = excluded.updatedAt
WHERE excluded.version > Book.version;

markSynced:
UPDATE Book SET syncStatus = 'synced' WHERE id = ?;

markConflict:
UPDATE Book SET syncStatus = 'conflict' WHERE id = ?;
```

---

## Driver Setup

### Android

```kotlin
// androidMain
actual fun createDriver(context: Any?): SqlDriver {
    return AndroidSqliteDriver(
        schema = AppDatabase.Schema,
        context = context as Context,
        name = "app.db"
    )
}
```

### iOS

```kotlin
// iosMain
actual fun createDriver(context: Any?): SqlDriver {
    return NativeSqliteDriver(
        schema = AppDatabase.Schema,
        name = "app.db"
    )
}
```

### Koin Integration

```kotlin
// commonMain/di/DatabaseModule.kt
val databaseModule = module {
    single { createDriver(getOrNull()) }
    single { AppDatabase(get()) }
    single { get<AppDatabase>().userQueries }
    single { get<AppDatabase>().bookQueries }
}
```

---

## Best Practices

| Area | Recommendation |
|------|----------------|
| **IDs** | Use TEXT UUIDs for cross-platform compatibility |
| **Timestamps** | Store as INTEGER (epoch millis) |
| **Booleans** | Use INTEGER (0/1), not BOOLEAN |
| **Indexes** | Add on foreign keys and frequently queried columns |
| **Soft deletes** | Use for sync-enabled tables |
| **Version** | Increment on every local change |
| **Flow queries** | Use for UI-bound data |
| **Transactions** | Wrap bulk operations |
