# SQLDelight Migrations

## Contents

- [Overview](#overview)
- [Migration Files](#migration-files)
- [Common Operations](#common-operations)
- [Migration Strategies](#migration-strategies)
- [Testing Migrations](#testing-migrations)

---

## Overview

SQLDelight uses numbered `.sqm` files for migrations:
- `1.sqm` migrates from version 1 to version 2
- `2.sqm` migrates from version 2 to version 3
- Schema version = number of `.sqm` files + 1

---

## Migration Files

### Location

```
shared/src/commonMain/sqldelight/
└── com/example/database/
    ├── Book.sq
    └── migrations/
        ├── 1.sqm    # v1 → v2
        ├── 2.sqm    # v2 → v3
        └── 3.sqm    # v3 → v4
```

### Basic Migration

```sql
-- migrations/1.sqm
-- Add email column to User table
ALTER TABLE User ADD COLUMN email TEXT NOT NULL DEFAULT '';
```

---

## Common Operations

### Add Column

```sql
-- migrations/1.sqm
ALTER TABLE Book ADD COLUMN isbn TEXT;
ALTER TABLE Book ADD COLUMN pageCount INTEGER DEFAULT 0;

-- With NOT NULL (requires default)
ALTER TABLE Book ADD COLUMN rating INTEGER NOT NULL DEFAULT 0;
```

### Add Table

```sql
-- migrations/2.sqm
CREATE TABLE Review (
    id TEXT NOT NULL PRIMARY KEY,
    bookId TEXT NOT NULL,
    userId TEXT NOT NULL,
    rating INTEGER NOT NULL,
    content TEXT,
    createdAt INTEGER NOT NULL,
    FOREIGN KEY (bookId) REFERENCES Book(id) ON DELETE CASCADE,
    FOREIGN KEY (userId) REFERENCES User(id) ON DELETE CASCADE
);

CREATE INDEX review_book_idx ON Review(bookId);
CREATE INDEX review_user_idx ON Review(userId);
```

### Add Index

```sql
-- migrations/3.sqm
CREATE INDEX book_title_idx ON Book(title);
CREATE INDEX book_status_idx ON Book(status) WHERE isDeleted = 0;
```

### Rename Table (SQLite 3.25+)

```sql
-- migrations/4.sqm
ALTER TABLE OldName RENAME TO NewName;
```

### Rename Column (SQLite 3.25+)

```sql
-- migrations/5.sqm
ALTER TABLE User RENAME COLUMN userName TO name;
```

### Drop Column (SQLite 3.35+)

```sql
-- migrations/6.sqm
ALTER TABLE Book DROP COLUMN deprecatedField;
```

---

## Migration Strategies

### Complex Schema Changes (Pre-SQLite 3.35)

For older SQLite versions or complex changes:

```sql
-- migrations/7.sqm

-- 1. Create new table with desired schema
CREATE TABLE Book_new (
    id TEXT NOT NULL PRIMARY KEY,
    title TEXT NOT NULL,
    authorId TEXT NOT NULL,
    -- New structure, removed/changed columns
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
);

-- 2. Copy data (transform as needed)
INSERT INTO Book_new (id, title, authorId, createdAt, updatedAt)
SELECT id, title, authorId, createdAt, updatedAt FROM Book;

-- 3. Drop old table
DROP TABLE Book;

-- 4. Rename new table
ALTER TABLE Book_new RENAME TO Book;

-- 5. Recreate indexes
CREATE INDEX book_author_idx ON Book(authorId);
```

### Data Transformation

```sql
-- migrations/8.sqm

-- Add new column
ALTER TABLE User ADD COLUMN role TEXT NOT NULL DEFAULT 'user';

-- Migrate existing data
UPDATE User SET role = 'admin' WHERE isAdmin = 1;

-- Note: Can't drop isAdmin column in older SQLite
-- Mark as deprecated in code, remove in future migration
```

### Adding Sync Metadata

```sql
-- migrations/9.sqm

-- Add sync columns to existing table
ALTER TABLE Book ADD COLUMN version INTEGER NOT NULL DEFAULT 1;
ALTER TABLE Book ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'synced';
ALTER TABLE Book ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0;

-- Add sync index
CREATE INDEX book_sync_status_idx ON Book(syncStatus) WHERE isDeleted = 0;
```

---

## Testing Migrations

### Gradle Configuration

```kotlin
// shared/build.gradle.kts
sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.example.database")
            verifyMigrations.set(true)  // Verify migrations at compile time
        }
    }
}
```

### Migration Test

```kotlin
// commonTest
class MigrationTest {
    @Test
    fun testMigrationFrom1To2() {
        // Create v1 schema
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        AppDatabase.Schema.migrate(driver, 0, 1)

        // Insert v1 data
        driver.execute(null, "INSERT INTO Book (id, title) VALUES ('1', 'Test')", 0)

        // Run migration
        AppDatabase.Schema.migrate(driver, 1, 2)

        // Verify new column exists with default
        val cursor = driver.executeQuery(
            null,
            "SELECT isbn FROM Book WHERE id = '1'",
            { it.getString(0) },
            0
        )
        assertNull(cursor.value) // isbn is nullable
    }
}
```

### Full Migration Verification

```kotlin
@Test
fun testAllMigrations() {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)

    // Apply all migrations step by step
    for (version in 0 until AppDatabase.Schema.version) {
        AppDatabase.Schema.migrate(driver, version, version + 1)
    }

    // Verify final schema works
    val database = AppDatabase(driver)
    // Run some queries to verify
}
```

---

## Best Practices

| Area | Recommendation |
|------|----------------|
| **Backup** | Always recommend users backup before migration |
| **One change** | One logical change per migration file |
| **Test** | Test migrations with real data scenarios |
| **Defaults** | Use sensible defaults for new NOT NULL columns |
| **Indexes** | Remember to recreate indexes after table rebuild |
| **Version** | Check SQLite version for feature availability |
| **Rollback** | SQLDelight doesn't support rollback - plan carefully |

### SQLite Version Compatibility

| Feature | Minimum SQLite Version |
|---------|------------------------|
| Basic ALTER TABLE | 3.0 |
| RENAME TABLE | 3.25 (2018) |
| RENAME COLUMN | 3.25 (2018) |
| DROP COLUMN | 3.35 (2021) |
| UPSERT | 3.24 (2018) |

**Android versions:**
- API 27-29: SQLite 3.19-3.22
- API 30: SQLite 3.28
- API 31+: SQLite 3.32+

For older Android versions, use the table rebuild strategy.
