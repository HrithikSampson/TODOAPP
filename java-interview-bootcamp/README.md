# BookVault — Java Interview Bootcamp (1 Day)

**One README. One topic at a time. Build as you go.**

You will build **BookVault Library API** — a single Spring Boot app to manage books, members, and borrow/return.  
Covers: **Java → Maven → Spring → JUnit** (no microservices, no LLD/HLD).

---

## How to use this README

1. Start at **Topic 1** below.
2. Read concepts → do the hands-on task → check the box.
3. Move to the next topic only when the checkpoint is done.
4. Reply **`next`** in chat if you want me to walk you through live.

**Run the finished app anytime:**
```bash
cd java-interview-bootcamp
mvn spring-boot:run          # API on http://localhost:8080
mvn test                     # run all tests
```

---

## Project map (where code lives)

```
java-interview-bootcamp/
├── README.md                        ← you are here (12 topics)
├── notes-pdf/                       ← extracted study PDFs (146 files)
│   ├── INDEX.md                     ← master index by category
│   ├── java/                        ← 34 PDFs (Topics 1–6)
│   ├── spring-boot/                 ← 20 PDFs (Topics 7–11)
│   ├── junit/                       ← 15 PDFs (Topic 12)
│   ├── microservices/               ← reference only (skipped)
│   ├── lld/                         ← reference only (skipped)
│   ├── hld/                         ← reference only (skipped)
│   └── event-driven/                ← reference only (skipped)
├── pom.xml                          ← Maven config
├── src/main/java/com/bootcamp/
│   ├── playground/                  ← small Java exercises (Topics 1–6)
│   └── library/                   ← full Spring app (Topics 7–12)
└── src/test/java/                   ← JUnit tests
```

## Study PDFs (extracted from your Drive link)

All PDFs from the original index are in **`notes-pdf/`**, organized by category.

| Bootcamp topic | Read these PDFs |
|----------------|-----------------|
| Topic 1–2 (Basics, OOP) | `notes-pdf/java/01-oops-concept.pdf`, `02-how-java-program-works.pdf` |
| Topic 3 (Interfaces) | `notes-pdf/java/14-interfaces.pdf` |
| Topic 4 (Collections) | `notes-pdf/java/22-collections-part-1-framework.pdf`, `28-collections-part-7-streams.pdf` |
| Topic 5 (Modern Java) | `notes-pdf/java/41-sealed-classes-and-interfaces-java-17.pdf`, `45-record-class-java-16.pdf` |
| Topic 6 (Exceptions) | `notes-pdf/java/19-exception-handling.pdf` |
| Topic 7 (Maven) | `notes-pdf/spring-boot/03-maven-and-its-lifecycle.pdf` |
| Topic 8–11 (Spring) | `notes-pdf/spring-boot/` folder |
| Topic 12 (JUnit) | `notes-pdf/junit/` folder |

Re-download PDFs anytime:
```bash
cd java-interview-bootcamp/notes-pdf
python3 build_index.py
python3 download_pdfs.py
```

---

# TOPIC 1 — Java Basics & How Java Runs

## Concepts

| Term | Meaning |
|------|---------|
| **JDK** | Dev kit — compiler (`javac`) + tools + JRE |
| **JRE** | Runtime — JVM + standard libraries |
| **JVM** | Runs bytecode — "Write Once, Run Anywhere" |

**Flow:** `.java` → `javac` → `.class` (bytecode) → JVM executes

**Primitive types:** `int`, `long`, `double`, `boolean`, `char`, `byte`, `short`, `float`  
**Reference types:** `String`, arrays, any object — variable holds a memory reference

**Interview Q:** `==` vs `.equals()`?  
→ `==` compares references (or primitive values). `.equals()` compares content (when overridden).

## Hands-on

```bash
cd java-interview-bootcamp
java -version

mkdir -p target/playground
javac -d target/playground src/main/java/com/bootcamp/playground/Step01Basics.java
java -cp target/playground com.bootcamp.playground.Step01Basics
```

Open `src/main/java/com/bootcamp/playground/Step01Basics.java` and add:
- a `long memberId` variable
- a method `formatBook(title, author)` returning `"title by author"`

## Checkpoint
- [ ] I can explain JDK / JRE / JVM
- [ ] I ran Step01Basics successfully

**➡️ NEXT → Topic 2**

---

# TOPIC 2 — OOP: Classes, Objects & Encapsulation

## Concepts

**Four pillars of OOP**

| Pillar | Meaning | In BookVault |
|--------|---------|--------------|
| Encapsulation | Hide data, expose via methods | `private` fields + getters |
| Abstraction | Show only what matters | `BookService` hides DB |
| Inheritance | IS-A relationship | `JpaRepository` extends |
| Polymorphism | One interface, many forms | `List<Book>` from repository |

```java
public class Book {
    private String title;              // state

    public Book(String title) {        // constructor
        this.title = title;
    }

    public String getTitle() {         // accessor
        return title;
    }
}
```

**Interview Q:** Why private fields?  
→ Validation, hide implementation, easier to change internals.

## Hands-on

Study: `src/main/java/com/bootcamp/library/model/Book.java`

Create and run `Step02Book.java` in the playground package:

```java
package com.bootcamp.playground;

public class Step02Book {
    private final String title;
    private final String author;
    private int copies;

    public Step02Book(String title, String author, int copies) {
        this.title = title;
        this.author = author;
        setCopies(copies);
    }

    public void setCopies(int copies) {
        if (copies < 0) throw new IllegalArgumentException("copies cannot be negative");
        this.copies = copies;
    }

    public String describe() {
        return title + " by " + author + " (" + copies + " copies)";
    }

    public static void main(String[] args) {
        System.out.println(new Step02Book("Effective Java", "Joshua Bloch", 2).describe());
    }
}
```

## Checkpoint
- [ ] I can write a class with constructor + validation
- [ ] I read the real `Book.java` entity

**➡️ NEXT → Topic 3**

---

# TOPIC 3 — Interfaces, Abstraction & Polymorphism

## Concepts

| | Interface | Abstract class |
|---|-----------|----------------|
| Multiple inheritance | Yes (`implements A, B`) | No (single `extends`) |
| Use when | Capability / contract | Shared base code |

Spring generates implementations for repository interfaces at runtime:

```java
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByIsbn(String isbn);
}
```

**Interview Q:** What is dependency inversion?  
→ High-level modules depend on abstractions (interfaces), not concrete classes.

## Hands-on

Read: `repository/BookRepository.java`, `repository/MemberRepository.java`

Write a tiny interface example:

```java
interface Notifiable {
    void notifyUser(String message);
    default void notifyWithPrefix(String msg) { notifyUser("[Library] " + msg); }
}
class EmailNotifier implements Notifiable {
    public void notifyUser(String message) { System.out.println("EMAIL: " + message); }
}
```

## Checkpoint
- [ ] I understand why Spring repos are interfaces
- [ ] I can explain compile-time vs runtime polymorphism

**➡️ NEXT → Topic 4**

---

# TOPIC 4 — Collections, Streams & Optional

## Concepts

| Collection | Implementation | Use case |
|------------|----------------|----------|
| `List` | `ArrayList` | Ordered, allows duplicates |
| `Set` | `HashSet` | Unique elements only |
| `Map` | `HashMap` | Key → value lookup |

**Streams:**
```java
List<BookResponse> result = books.stream()
    .filter(Book::isAvailable)
    .map(BookResponse::from)
    .toList();
```
- Intermediate ops: `filter`, `map`, `sorted` (lazy)
- Terminal ops: `toList`, `count`, `collect` (execute pipeline)

**Optional** — avoid `NullPointerException`:
```java
return bookRepository.findById(id)
    .map(BookResponse::from)
    .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
```

**Interview Q:** How does HashMap work?  
→ Array of buckets → `hashCode()` picks bucket → `equals()` resolves collisions.

## Hands-on

Trace streams in `service/BookService.java` — `findAll()`, `findById()`, `findAvailableBooks()`.

Exercise:
```java
List<String> titles = List.of("Java", "Spring", "Maven", "JUnit");
List<String> result = titles.stream()
    .filter(t -> t.length() > 4)
    .map(String::toUpperCase)
    .toList();
// [SPRING, MAVEN, JUNIT]
```

## Checkpoint
- [ ] I can write filter/map/toList from memory
- [ ] I know when to use Optional vs null

**➡️ NEXT → Topic 5**

---

# TOPIC 5 — Modern Java (Records, Enums, Switch)

## Concepts

**Enum** — fixed set of constants:
```java
public enum BookStatus { AVAILABLE, BORROWED, RESERVED }
```
See: `model/BookStatus.java`

**Record** — immutable data carrier (Java 16+):
```java
public record BookRequest(@NotBlank String title, @NotBlank String author, @NotBlank String isbn) {}
```
See: `dto/BookRequest.java`, `dto/BookResponse.java`

**Switch expression** (Java 14+):
```java
String label = switch (status) {
    case AVAILABLE -> "On shelf";
    case BORROWED  -> "Checked out";
    case RESERVED  -> "Held for member";
};
```

**Interview Q:** Record vs class?  
→ Record = immutable data, auto-generates constructor/getters/equals/hashCode/toString.

## Hands-on

Read all files in `dto/` — notice records replace boilerplate POJOs.

Add a `describeStatus(BookStatus status)` method using switch in a playground class.

## Checkpoint
- [ ] I can write a record and enum
- [ ] I know records are immutable

**➡️ NEXT → Topic 6**

---

# TOPIC 6 — Exception Handling

## Concepts

| Type | Examples | Must catch? |
|------|----------|-------------|
| **Checked** | `IOException`, `SQLException` | Yes (or declare `throws`) |
| **Unchecked** | `RuntimeException`, `IllegalArgumentException` | No |

**Custom exceptions in BookVault:**
- `ResourceNotFoundException` → 404
- `BusinessRuleException` → 409 (e.g. book already borrowed)

```java
if (!bookRepository.existsById(id)) {
    throw new ResourceNotFoundException("Book not found: " + id);
}
```

**Global handler:** `exception/GlobalExceptionHandler.java` uses `@RestControllerAdvice`

**Interview Q:** `throw` vs `throws`?  
→ `throw` = actually throw an exception. `throws` = declare method may throw (checked only).

## Hands-on

Read: `exception/ResourceNotFoundException.java`, `GlobalExceptionHandler.java`

Trigger a 404:
```bash
curl http://localhost:8080/api/books/9999
```

## Checkpoint
- [ ] I know checked vs unchecked
- [ ] I understand `@RestControllerAdvice`

**➡️ NEXT → Topic 7**

---

# TOPIC 7 — Maven & Spring Boot Bootstrap

## Concepts

**Maven lifecycle (interview favorites):**
| Phase | What it does |
|-------|--------------|
| `compile` | `.java` → `.class` |
| `test` | Runs JUnit via Surefire |
| `package` | Creates `.jar` |
| `install` | Puts jar in local `~/.m2` repo |

**Key `pom.xml` sections:**
```xml
<parent>spring-boot-starter-parent</parent>   <!-- versions managed -->
<dependencies>                                 <!-- libraries -->
<build><plugins>spring-boot-maven-plugin     <!-- runnable jar -->
```

**Spring Boot entry:**
```java
@SpringBootApplication  // = @Configuration + @EnableAutoConfiguration + @ComponentScan
public class LibraryApplication {
    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }
}
```

**Interview Q:** What is a starter dependency?  
→ Pre-bundled dependency set (e.g. `spring-boot-starter-web` brings Tomcat + Jackson + Spring MVC).

## Hands-on

```bash
cd java-interview-bootcamp
mvn clean compile          # compile only
mvn test                   # compile + test
mvn package                # creates target/library-api-1.0.0.jar
mvn spring-boot:run        # start the app
```

Open `pom.xml` — find Java 21, web, JPA, H2, test starters.

## Checkpoint
- [ ] I can explain Maven phases
- [ ] App starts on port 8080

**➡️ NEXT → Topic 8**

---

# TOPIC 8 — Spring DI & Service Layer

## Concepts

**IoC (Inversion of Control):** Spring creates and wires objects — you don't `new` services.

**DI (Dependency Injection):** Dependencies passed in via constructor:
```java
@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {  // constructor injection
        this.bookRepository = bookRepository;
    }
}
```

**Stereotype annotations:**
| Annotation | Layer |
|------------|-------|
| `@Service` | Business logic |
| `@Repository` | Data access (auto via JpaRepository) |
| `@RestController` | HTTP endpoints |
| `@Component` | Generic bean |

**Interview Q:** Constructor vs field injection?  
→ Constructor injection is preferred: immutable, testable, required deps explicit.

## Hands-on

Read: `service/BookService.java`, `service/MemberService.java`, `service/BorrowService.java`

Notice `@Transactional(readOnly = true)` on class — write methods override with `@Transactional`.

Draw the dependency graph:
```
BookController → BookService → BookRepository
BorrowController → BorrowService → BookService + MemberService + BorrowRecordRepository
```

## Checkpoint
- [ ] I can explain IoC and DI
- [ ] I know `@Service` vs `@Repository` vs `@RestController`

**➡️ NEXT → Topic 9**

---

# TOPIC 9 — REST API & Validation

## Concepts

**REST mapping:**
```java
@RestController
@RequestMapping("/api/books")
public class BookController {

    @GetMapping("/{id}")           // GET  /api/books/1
    @PostMapping                   // POST /api/books
    @PutMapping("/{id}")           // PUT  /api/books/1
    @DeleteMapping("/{id}")        // DELETE /api/books/1
}
```

**HTTP status codes:**
| Code | Meaning | Used when |
|------|---------|-----------|
| 200 | OK | GET, PUT success |
| 201 | Created | POST success |
| 204 | No Content | DELETE success |
| 400 | Bad Request | Validation failed |
| 404 | Not Found | Resource missing |
| 409 | Conflict | Business rule broken |

**Validation:**
```java
public record BookRequest(@NotBlank String title, @NotBlank String author, @NotBlank String isbn) {}

@PostMapping
public BookResponse create(@Valid @RequestBody BookRequest request) { ... }
```

## Hands-on

Start app, then test all endpoints:

```bash
# Create book
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Effective Java","author":"Joshua Bloch","isbn":"978-0134685991"}'

# List books
curl http://localhost:8080/api/books

# Search by author
curl "http://localhost:8080/api/books?author=Bloch"

# Available books only
curl http://localhost:8080/api/books/available

# Create member
curl -X POST http://localhost:8080/api/members \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com"}'

# Borrow (use real ids)
curl -X POST http://localhost:8080/api/borrows \
  -H "Content-Type: application/json" \
  -d '{"bookId":1,"memberId":1}'

# Return book
curl -X POST http://localhost:8080/api/borrows/1/return

# Bad request (empty title)
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"","author":"X","isbn":"123"}'
```

Read: `controller/BookController.java`, `controller/MemberController.java`, `controller/BorrowController.java`

## Checkpoint
- [ ] I can map HTTP verbs to CRUD
- [ ] I tested create + list + validation error

**➡️ NEXT → Topic 10**

---

# TOPIC 10 — Spring Data JPA

## Concepts

**JPA entity:**
```java
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String isbn;

    @Enumerated(EnumType.STRING)
    private BookStatus status;
}
```

**Relationships in BookVault:**
```
Book ←—— BorrowRecord ——→ Member
     @ManyToOne        @ManyToOne
```

**Repository — Spring generates SQL from method names:**
```java
List<Book> findByAuthorContainingIgnoreCase(String author);
boolean existsByBookIdAndReturnedAtIsNull(Long bookId);
```

**Interview Q:** `ddl-auto: create-drop` vs `validate`?  
→ `create-drop` = recreate schema each run (dev). `validate` = schema must match entities (prod).

H2 console: http://localhost:8080/h2-console → JDBC URL: `jdbc:h2:mem:librarydb`

## Hands-on

Read: `model/Book.java`, `model/Member.java`, `model/BorrowRecord.java`  
Read: all files in `repository/`

Run app, open H2 console, run:
```sql
SELECT * FROM books;
SELECT * FROM members;
SELECT * FROM borrow_records;
```

## Checkpoint
- [ ] I can annotate an `@Entity`
- [ ] I understand `@ManyToOne` relationship
- [ ] I know how derived query method names work

**➡️ NEXT → Topic 11**

---

# TOPIC 11 — Transactions & Business Rules

## Concepts

**`@Transactional`:**
```java
@Service
@Transactional(readOnly = true)   // default for all methods
public class BorrowService {

    @Transactional               // override for writes
    public BorrowResponse borrowBook(BorrowRequest request) {
        // all DB ops succeed together or roll back
    }
}
```

**ACID (interview):**
- **A**tomicity — all or nothing
- **C**onsistency — valid state before/after
- **I**solation — concurrent txs don't interfere
- **D**urability — committed data survives crash

**Business rules in BorrowService:**
1. Book must be available
2. Book cannot already be on active loan
3. On borrow → set status `BORROWED`, create `BorrowRecord`
4. On return → set status `AVAILABLE`, set `returnedAt`

## Hands-on

Read: `service/BorrowService.java` — trace `borrowBook()` and `returnBook()` line by line.

Test the conflict case:
```bash
# Borrow same book twice — second call should return 409 Conflict
curl -X POST http://localhost:8080/api/borrows -H "Content-Type: application/json" -d '{"bookId":1,"memberId":1}'
curl -X POST http://localhost:8080/api/borrows -H "Content-Type: application/json" -d '{"bookId":1,"memberId":1}'
```

## Checkpoint
- [ ] I can explain `@Transactional`
- [ ] I understand ACID at a high level
- [ ] I traced borrow + return flow

**➡️ NEXT → Topic 12**

---

# TOPIC 12 — JUnit 5 & Mockito

## Concepts

**JUnit 5 annotations:**
| Annotation | Purpose |
|------------|---------|
| `@Test` | Mark test method |
| `@BeforeEach` | Run before each test |
| `@DisplayName` | Readable test name |
| `@ExtendWith(MockitoExtension.class)` | Enable Mockito |

**Unit test** — isolate class, mock dependencies:
```java
@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    @Mock BookRepository bookRepository;
    @InjectMocks BookService bookService;

    @Test
    void findById_success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        assertThat(bookService.findById(1L).title()).isEqualTo("Clean Code");
    }
}
```

**Integration test** — load full Spring context:
```java
@SpringBootTest
@AutoConfigureMockMvc
class BookControllerIntegrationTest {
    @Autowired MockMvc mockMvc;

    @Test
    void createAndFetchBook() throws Exception {
        mockMvc.perform(post("/api/books").contentType(APPLICATION_JSON).content(json))
               .andExpect(status().isCreated());
    }
}
```

**Interview Q:** Unit vs integration test?  
→ Unit = fast, one class, mocked deps. Integration = slower, real Spring context + DB.

## Hands-on

```bash
mvn test
```

Read and understand:
- `test/.../service/BookServiceTest.java` — unit test with mocks
- `test/.../service/BorrowServiceTest.java` — business rule tests
- `test/.../controller/BookControllerIntegrationTest.java` — full stack test

Add your own test in `BookServiceTest`:
```java
@Test
@DisplayName("delete throws when book not found")
void delete_notFound() {
    when(bookRepository.existsById(99L)).thenReturn(false);
    assertThatThrownBy(() -> bookService.delete(99L))
        .isInstanceOf(ResourceNotFoundException.class);
}
```

Run: `mvn test -Dtest=BookServiceTest`

## Checkpoint
- [ ] All tests pass (`mvn test`)
- [ ] I can write a `@Mock` + `when().thenReturn()` test
- [ ] I know difference between unit and integration tests

**➡️ DONE — go to Final Revision below**

---

# FINAL REVISION — Interview Cheatsheet

## Java (top questions)

| Question | Answer |
|----------|--------|
| JDK vs JRE vs JVM? | JDK=dev kit, JRE=runtime, JVM=executes bytecode |
| `==` vs `.equals()`? | `==` reference/value, `.equals()` content |
| ArrayList vs LinkedList? | ArrayList O(1) get, LinkedList O(1) add/remove ends |
| HashMap internals? | Buckets, hashCode, equals, collision chain/tree |
| String immutable? | Yes — every change creates new object |
| final vs finally vs finalize? | final=constant, finally=try block cleanup, finalize=deprecated GC hook |
| Comparable vs Comparator? | Comparable=natural order (`compareTo`), Comparator=external (`compare`) |

## Maven

| Question | Answer |
|----------|--------|
| Maven lifecycle order? | validate → compile → test → package → verify → install → deploy |
| What is `pom.xml`? | Project Object Model — deps, plugins, build config |
| Local repo path? | `~/.m2/repository` |
| `mvn clean install`? | Delete target, compile, test, package, install to local repo |

## Spring

| Question | Answer |
|----------|--------|
| What is IoC? | Framework controls object creation/lifecycle |
| `@Component` vs `@Service`? | Same thing — `@Service` is semantic alias |
| `@Autowired` on constructor? | Preferred — immutable, testable |
| `@Transactional` propagation? | REQUIRED (default) joins existing or creates new |
| Bean scopes? | singleton (default), prototype, request, session |
| `@RestController` vs `@Controller`? | Rest = `@ResponseBody` on every method |

## JUnit / Testing

| Question | Answer |
|----------|--------|
| `@Mock` vs `@InjectMocks`? | Mock=fake dependency, InjectMocks=class under test |
| `when().thenReturn()`? | Stub mock behavior |
| `verify(mock).save()`? | Assert mock method was called |
| `@SpringBootTest`? | Loads full application context |

---

## 1-day schedule

| Block | Topics | Focus |
|-------|--------|-------|
| Morning (3h) | 1–4 | Java core |
| Midday (2h) | 5–7 | Modern Java + Maven + Spring boot |
| Afternoon (2.5h) | 8–10 | Services + REST + JPA |
| Evening (2h) | 11–12 + revision | Transactions + tests + cheatsheet |

---

## You finished when

- [ ] App runs: `mvn spring-boot:run`
- [ ] All tests pass: `mvn test`
- [ ] You can explain every layer: Controller → Service → Repository → Entity
- [ ] You can answer the cheatsheet questions without looking

**Reply `next` anytime to get a live walkthrough of the current topic.**
