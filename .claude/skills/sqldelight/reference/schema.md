# SQLDelight Schema Patterns

## Contents

- [Data Types](#data-types)
- [Table Definitions](#table-definitions)
- [Constraints](#constraints)
- [Indexes](#indexes)
- [Import Types](#import-types)

---

## Data Types

### SQLite to Kotlin Mapping

| SQLite Type | Kotlin Type | Notes |
|-------------|-------------|-------|
| `TEXT` | `String` | Default for strings |
| `INTEGER` | `Long` | Default for numbers |
| `REAL` | `Double` | Floating point |
| `BLOB` | `ByteArray` | Binary data |
| `INTEGER NOT NULL` | `Long` | Non-nullable |
| `INTEGER` (nullable) | `Long?` | Nullable |

### Custom Type Adapters

```kotlin
// Define adapter for custom types
val dateAdapter = object : ColumnAdapter<Instant, Long> {
    override fun decode(databaseValue: Long): Instant =
        Instant.fromEpochMilliseconds(databaseValue)

    override fun encode(value: Instant): Long =
        value.toEpochMilliseconds()
}

// Use in database creation
val database = AppDatabase(
    driver = driver,
    BookAdapter = Book.Adapter(
        createdAtAdapter = dateAdapter,
        updatedAtAdapter = dateAdapter
    )
)
```

---

## Table Definitions

### Complete Example

```sql
-- Book.sq

-- Table definition
CREATE TABLE Book (
    -- Primary key
    id TEXT NOT NULL PRIMARY KEY,

    -- Required fields
    title TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',

    -- Optional fields
    isbn TEXT,
    coverUrl TEXT,
    pageCount INTEGER,

    -- Foreign keys
    authorId TEXT NOT NULL,
    publisherId TEXT,

    -- Enums as TEXT
    status TEXT NOT NULL DEFAULT 'draft',

    -- Sync metadata
    version INTEGER NOT NULL DEFAULT 1,
    syncStatus TEXT NOT NULL DEFAULT 'synced',
    isDeleted INTEGER NOT NULL DEFAULT 0,

    -- Timestamps
    publishedAt INTEGER,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,

    -- Constraints
    FOREIGN KEY (authorId) REFERENCES Author(id) ON DELETE CASCADE,
    FOREIGN KEY (publisherId) REFERENCES Publisher(id) ON DELETE SET NULL
);
```

### Enum-Like Fields

```sql
-- Use CHECK constraint for enum validation
CREATE TABLE Task (
    id TEXT NOT NULL PRIMARY KEY,
    title TEXT NOT NULL,
    priority TEXT NOT NULL DEFAULT 'medium'
        CHECK (priority IN ('low', 'medium', 'high', 'urgent')),
    status TEXT NOT NULL DEFAULT 'todo'
        CHECK (status IN ('todo', 'in_progress', 'done', 'archived'))
);
```

### JSON Storage

```sql
-- Store JSON as TEXT, parse in Kotlin
CREATE TABLE Settings (
    id TEXT NOT NULL PRIMARY KEY,
    preferences TEXT NOT NULL DEFAULT '{}'
);
```

```kotlin
// Kotlin side
@Serializable
data class UserPreferences(
    val theme: String = "system",
    val notifications: Boolean = true
)

// In repository
val prefs = Json.decodeFromString<UserPreferences>(settings.preferences)
```

---

## Constraints

### Primary Key

```sql
-- Single column
id TEXT NOT NULL PRIMARY KEY

-- Composite key
CREATE TABLE BookAuthor (
    bookId TEXT NOT NULL,
    authorId TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'author',
    PRIMARY KEY (bookId, authorId)
);
```

### Foreign Keys

```sql
-- Basic foreign key
FOREIGN KEY (authorId) REFERENCES Author(id)

-- With cascade delete
FOREIGN KEY (authorId) REFERENCES Author(id) ON DELETE CASCADE

-- With set null
FOREIGN KEY (publisherId) REFERENCES Publisher(id) ON DELETE SET NULL

-- With restrict (default)
FOREIGN KEY (categoryId) REFERENCES Category(id) ON DELETE RESTRICT
```

### Unique Constraints

```sql
-- Single column unique
email TEXT NOT NULL UNIQUE

-- Composite unique
CREATE TABLE UserBook (
    userId TEXT NOT NULL,
    bookId TEXT NOT NULL,
    UNIQUE (userId, bookId)
);
```

### Check Constraints

```sql
-- Range check
pageCount INTEGER CHECK (pageCount > 0)

-- Enum check
status TEXT NOT NULL CHECK (status IN ('active', 'inactive', 'deleted'))

-- Complex check
rating INTEGER CHECK (rating >= 1 AND rating <= 5)
```

---

## Indexes

### Single Column

```sql
CREATE INDEX book_title_idx ON Book(title);
CREATE INDEX book_author_idx ON Book(authorId);
```

### Composite Index

```sql
-- Order matters for query optimization
CREATE INDEX book_author_status_idx ON Book(authorId, status);
```

### Partial Index

```sql
-- Index only non-deleted rows
CREATE INDEX book_active_idx ON Book(title) WHERE isDeleted = 0;
```

### Unique Index

```sql
CREATE UNIQUE INDEX user_email_idx ON User(email);
```

### When to Add Indexes

| Add Index | Reason |
|-----------|--------|
| Foreign keys | JOIN performance |
| Frequently filtered columns | WHERE clause performance |
| Sorted columns | ORDER BY performance |
| syncStatus | Sync query performance |
| Unique business keys | Constraint enforcement |

---

## Import Types

### Using Kotlin Types in Schema

```sql
-- Book.sq

-- Import Kotlin types for complex data
import com.example.domain.BookStatus;
import kotlinx.datetime.Instant;

CREATE TABLE Book (
    id TEXT NOT NULL PRIMARY KEY,
    title TEXT NOT NULL,
    status TEXT AS BookStatus NOT NULL,
    createdAt INTEGER AS Instant NOT NULL
);
```

### Defining Type Adapters

```kotlin
// BookStatus adapter
val bookStatusAdapter = object : ColumnAdapter<BookStatus, String> {
    override fun decode(databaseValue: String): BookStatus =
        BookStatus.valueOf(databaseValue)

    override fun encode(value: BookStatus): String =
        value.name
}

// Instant adapter
val instantAdapter = object : ColumnAdapter<Instant, Long> {
    override fun decode(databaseValue: Long): Instant =
        Instant.fromEpochMilliseconds(databaseValue)

    override fun encode(value: Instant): Long =
        value.toEpochMilliseconds()
}

// Provide adapters to database
val database = AppDatabase(
    driver = driver,
    BookAdapter = Book.Adapter(
        statusAdapter = bookStatusAdapter,
        createdAtAdapter = instantAdapter
    )
)
```

### Sealed Class Mapping

```kotlin
// Domain model
sealed class BookStatus {
    object Draft : BookStatus()
    object Published : BookStatus()
    data class Archived(val reason: String) : BookStatus()
}

// Adapter with JSON for complex variants
val bookStatusAdapter = object : ColumnAdapter<BookStatus, String> {
    override fun decode(databaseValue: String): BookStatus {
        return when {
            databaseValue == "draft" -> BookStatus.Draft
            databaseValue == "published" -> BookStatus.Published
            databaseValue.startsWith("archived:") ->
                BookStatus.Archived(databaseValue.removePrefix("archived:"))
            else -> error("Unknown status: $databaseValue")
        }
    }

    override fun encode(value: BookStatus): String {
        return when (value) {
            is BookStatus.Draft -> "draft"
            is BookStatus.Published -> "published"
            is BookStatus.Archived -> "archived:${value.reason}"
        }
    }
}
```
