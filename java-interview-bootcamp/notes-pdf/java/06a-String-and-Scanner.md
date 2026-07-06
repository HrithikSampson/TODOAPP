# 06a — String, Scanner & Text I/O (Interview Prep)

**Follows:** [06-reference-and-non-primitive-data-types.pdf](06-reference-and-non-primitive-data-types.pdf)

`String` is the most-used reference type in Java. Interviews love questions on **immutability**, **pooling**, **`==` vs `equals`**, and when to use **StringBuilder** vs **StringBuffer**. For input, know **Scanner** (easy parsing) vs **BufferedReader** (fast line reading).

---

## Quick comparison (memorize this table)

| Type | Mutable? | Thread-safe? | Typical use |
|------|----------|--------------|-------------|
| `String` | No | Yes (immutable) | Text values, APIs, keys |
| `StringBuilder` | Yes | No | Single-thread string building (loops, buffers) |
| `StringBuffer` | Yes | Yes (`synchronized`) | Legacy / rare multi-thread append |
| `Scanner` | — | — | Parse tokens (`int`, `word`) from `System.in`, files, strings |
| `BufferedReader` | — | — | Read lines efficiently from console or files |

---

## 1. String fundamentals

### Reference type on the heap

```java
String title = "Clean Code";   // reference variable → String object on heap
String author = new String("Robert Martin");  // explicit object (usually avoid)
```

### Immutability

Once created, a `String` object **cannot change**. Every “change” creates a **new** object.

```java
String s = "hello";
s.toUpperCase();     // returns NEW "HELLO" — s is still "hello"
s = s.toUpperCase(); // reassign reference → now s points to "HELLO"
```

**Interview Q:** Why is String immutable?

- **Security** — strings used in class loading, network paths, etc. cannot be altered after creation.
- **String pool** — safe to reuse pooled literals.
- **Thread safety** — immutable objects need no locking.
- **Hash stability** — good hash codes for `HashMap` keys.

### String pool & `intern()`

```java
String a = "Java";                    // literal → may live in String pool
String b = "Java";                    // same pool entry
String c = new String("Java");        // new heap object (not pooled by default)

System.out.println(a == b);           // true  (same pool reference)
System.out.println(a == c);           // false (different objects)
System.out.println(a.equals(c));      // true  (same content)

String d = c.intern();                // returns pool reference if exists
System.out.println(a == d);           // true
```

**Rule:** Always use `.equals()` for content. Use `==` only when you intentionally mean same reference (rare for strings).

### Common String methods (hands-on must-know)

| Method | Example | Result / note |
|--------|---------|---------------|
| `length()` | `"abc".length()` | `3` |
| `charAt(i)` | `"abc".charAt(1)` | `'b'` |
| `substring(a, b)` | `"hello".substring(1, 4)` | `"ell"` |
| `indexOf("ll")` | `"hello".indexOf("ll")` | `2` |
| `contains("ell")` | `"hello".contains("ell")` | `true` |
| `startsWith` / `endsWith` | `"file.txt".endsWith(".txt")` | `true` |
| `trim()` | `"  hi  ".trim()` | `"hi"` |
| `strip()` (Java 11+) | same idea, Unicode-aware | prefer in new code |
| `toLowerCase()` / `toUpperCase()` | locale-sensitive | be careful in i18n |
| `split(",")` | `"a,b,c".split(",")` | `["a","b","c"]` |
| `replace("a","b")` | `"aaa".replace("a","b")` | `"bbb"` |
| `join(delimiter, parts)` | `String.join("-", "a","b")` | `"a-b"` |
| `formatted` / `format` | `"Hi %s".formatted("Alice")` | `"Hi Alice"` |
| `valueOf(42)` | `String.valueOf(42)` | `"42"` |
| `isBlank()` (Java 11+) | `"   ".isBlank()` | `true` |
| `isEmpty()` | `"".isEmpty()` | `true` (blank string is not empty) |

### Concatenation trap (interview favorite)

```java
// BAD in a loop — creates many intermediate String objects
String result = "";
for (int i = 0; i < 1000; i++) {
    result += i;   // each += copies entire string
}

// GOOD — one mutable buffer
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 1000; i++) {
    sb.append(i);
}
String result = sb.toString();
```

Compiler optimizes **simple** `+` in one expression (`"a" + "b" + c`) via `StringBuilder` under the hood — but **not** reliably inside loops.

### Text blocks (Java 15+)

```java
String json = """
    {
      "title": "Clean Code",
      "copies": 3
    }
    """;
```

---

## 2. StringBuilder vs StringBuffer

Both wrap a **mutable char array** that grows as needed.

```java
StringBuilder sb = new StringBuilder(16);  // optional initial capacity
sb.append("Book: ").append("Java");
sb.insert(0, "[Library] ");
sb.delete(0, 10);
sb.reverse();           // rarely needed in interviews, but know it exists
String out = sb.toString();
```

| | StringBuilder | StringBuffer |
|---|---------------|--------------|
| Since | Java 5 | Java 1.0 |
| Sync | No | Yes (every method) |
| Speed | Faster | Slower |
| When | Default choice | Only if multiple threads append to **same** instance |

**Interview Q:** String vs StringBuilder?

- `String` — immutable, safe to share, fine for constants and short ops.
- `StringBuilder` — when you build or modify text repeatedly (loops, parsers).

---

## 3. Scanner — convenient parsing

`java.util.Scanner` breaks input into **tokens** using delimiters (default: whitespace).

### Console input

```java
import java.util.Scanner;

Scanner sc = new Scanner(System.in);

System.out.print("Enter book title: ");
String title = sc.nextLine();          // whole line including spaces

System.out.print("Enter copies: ");
int copies = sc.nextInt();
sc.nextLine();                         // consume leftover newline — IMPORTANT

System.out.print("Enter author: ");
String author = sc.nextLine();

sc.close();                            // close when done with System.in
```

### `next()` vs `nextLine()` — classic bug

```java
Scanner sc = new Scanner("Clean Code\n3");
String word = sc.next();       // "Clean"  (stops at space)
String line = sc.nextLine();   // " Code" or "\n3" depending on cursor — often surprises people
```

| Method | Reads |
|--------|-------|
| `next()` | Next token (no spaces) |
| `nextLine()` | Rest of current line (including spaces) |
| `nextInt()`, `nextDouble()`, … | Primitive token |
| `hasNext()`, `hasNextInt()` | Whether another token exists |
| `useDelimiter(",")` | Change separator (e.g. CSV) |

### Scanner on String or File

```java
Scanner sc = new Scanner("Java,Spring,Maven");
sc.useDelimiter(",");
while (sc.hasNext()) {
    System.out.println(sc.next());
}
// Java → Spring → Maven

Scanner file = new Scanner(new java.io.File("books.txt"));
while (file.hasNextLine()) {
    System.out.println(file.nextLine());
}
file.close();
```

**Pros:** Easy API, typed reads (`nextInt`).  
**Cons:** Slower than `BufferedReader` for large files; easy to mishandle newlines.

---

## 4. BufferedReader — efficient line reading

Wraps a `Reader` and reads **chunks** into a buffer, so fewer system calls.

### Console (with InputStreamReader)

```java
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
System.out.print("Enter title: ");
String title = reader.readLine();   // may throw IOException (checked)
int copies = Integer.parseInt(reader.readLine().trim());
reader.close();
```

### File

```java
try (BufferedReader br = new BufferedReader(new java.io.FileReader("books.txt"))) {
    String line;
    while ((line = br.readLine()) != null) {
        System.out.println(line);
    }
} catch (java.io.IOException e) {
    e.printStackTrace();
}
```

### Modern alternative (Java 11+)

```java
String content = java.nio.file.Files.readString(java.nio.file.Path.of("books.txt"));

java.nio.file.Files.lines(java.nio.file.Path.of("books.txt"))
    .filter(line -> line.contains("Java"))
    .forEach(System.out::println);
```

**Interview Q:** Scanner vs BufferedReader?

| | Scanner | BufferedReader |
|---|---------|----------------|
| Best for | Token parsing, mixed types | Line-by-line, large text |
| Speed | Slower | Faster |
| Exception | Unchecked (`InputMismatchException`) | Checked `IOException` |
| API | `nextInt()`, `next()` | `readLine()` + parse yourself |

---

## 5. Related I/O helpers (good to name-drop)

| Class | Role |
|-------|------|
| `InputStreamReader` | Bytes (`System.in`) → characters |
| `FileReader` | Read chars from file (legacy; prefer `Files`) |
| `PrintWriter` | Convenient formatted output + `println` |
| `StringReader` / `StringWriter` | In-memory char streams (testing, parsers) |
| `Files.readAllLines` / `readString` | NIO.2 file helpers |

```java
// Writing text
try (PrintWriter pw = new PrintWriter("out.txt")) {
    pw.println("BookVault");
    pw.printf("copies=%d%n", 3);
}
```

---

## 6. Hands-on — run the playground

From the bootcamp root:

```bash
cd java-interview-bootcamp
mkdir -p target/playground
javac -d target/playground src/main/java/com/bootcamp/playground/Step06aStringAndScanner.java
java -cp target/playground com.bootcamp.playground.Step06aStringAndScanner
```

The class demonstrates: pool behavior, `StringBuilder`, `Scanner` on a string, and `BufferedReader` on a `StringReader`.

### Exercise A — ISBN validator

Write a method:

```java
static boolean isValidIsbn13(String isbn) {
    // strip hyphens/spaces, length must be 13, all digits
}
```

Test: `"978-0134685991"` → `true`, `"abc"` → `false`.

### Exercise B — word count

Read a line from `System.in` with `Scanner.nextLine()`, split on spaces, print word count (ignore extra spaces).

### Exercise C — reverse words

Input: `"Java is fun"` → Output: `"fun is Java"` (use `split` + `StringBuilder` or loop).

### Exercise D — BufferedReader file

Create `books.txt` with 3 lines. Use try-with-resources + `BufferedReader` to print lines numbered `1: ...`, `2: ...`.

---

## 7. Interview cheat sheet

| Question | Short answer |
|----------|--------------|
| Is String primitive? | No — reference type (but special JVM support) |
| Mutable string class? | `StringBuilder` (prefer) or `StringBuffer` |
| `==` vs `equals` for strings? | `==` reference; `equals` content |
| What is String pool? | Intern table for literal reuse |
| Why immutable? | Security, pooling, thread safety, stable hash |
| Concat in loop? | Use `StringBuilder` |
| `next()` after `nextInt()` skips line? | Leftover `\n` — call `nextLine()` to consume |
| Fastest console line read? | `BufferedReader.readLine()` |
| `isEmpty()` vs `isBlank()`? | Empty = length 0; blank = only whitespace |
| Checked exception for file read? | `IOException` |

---

## Checkpoint

- [ ] I can explain String immutability and the pool
- [ ] I use `.equals()` not `==` for content
- [ ] I know when to pick `StringBuilder` over `+`
- [ ] I can read a line with `Scanner` and fix the `nextInt` newline bug
- [ ] I can read a file with `BufferedReader` or `Files.lines()`
- [ ] I ran `Step06aStringAndScanner` successfully

**Next PDF:** [07-methods.pdf](07-methods.pdf)
