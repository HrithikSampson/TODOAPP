package com.bootcamp.playground;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Scanner;

/**
 * STEP 06a — Strings, StringBuilder, Scanner, BufferedReader
 *
 *   cd java-interview-bootcamp
 *   mkdir -p target/playground
 *   javac -d target/playground src/main/java/com/bootcamp/playground/Step06aStringAndScanner.java
 *   java -cp target/playground com.bootcamp.playground.Step06aStringAndScanner
 */
public class Step06aStringAndScanner {

    public static void main(String[] args) throws IOException {
        System.out.println("=== STEP 06a: String & Scanner ===\n");

        demoStringPool();
        demoStringMethods();
        demoStringBuilder();
        demoScannerOnString();
        demoBufferedReader();
        demoExerciseReverseWords();
    }

    static void demoStringPool() {
        System.out.println("--- String pool ---");
        String a = "Java";
        String b = "Java";
        String c = new String("Java");

        System.out.println("a == b: " + (a == b));
        System.out.println("a == c: " + (a == c));
        System.out.println("a.equals(c): " + a.equals(c));
        System.out.println("a == c.intern(): " + (a == c.intern()));
        System.out.println();
    }

    static void demoStringMethods() {
        System.out.println("--- String methods ---");
        String title = "  Effective Java  ";
        System.out.println("trim: '" + title.trim() + "'");
        System.out.println("strip: '" + title.strip() + "'");
        System.out.println("contains 'Java': " + title.contains("Java"));
        System.out.println("split words: " + String.join(" | ", title.strip().split("\\s+")));
        System.out.println("formatted: " + "%s (%d copies)".formatted("Clean Code", 2));
        System.out.println();
    }

    static void demoStringBuilder() {
        System.out.println("--- StringBuilder ---");
        StringBuilder sb = new StringBuilder();
        for (String part : new String[]{"Book", ":", " ", "Vault"}) {
            sb.append(part);
        }
        System.out.println("built: " + sb);
        System.out.println();
    }

    static void demoScannerOnString() {
        System.out.println("--- Scanner (in-memory) ---");
        String csv = "978-0134685991,Effective Java,Joshua Bloch";
        Scanner sc = new Scanner(csv);
        sc.useDelimiter(",");
        while (sc.hasNext()) {
            System.out.println("token: " + sc.next());
        }
        sc.close();

        Scanner typed = new Scanner("42\nhello world");
        System.out.println("nextInt: " + typed.nextInt());
        typed.nextLine(); // consume newline after int
        System.out.println("nextLine: " + typed.nextLine());
        typed.close();
        System.out.println();
    }

    static void demoBufferedReader() throws IOException {
        System.out.println("--- BufferedReader (StringReader) ---");
        String data = "Line one\nLine two\nLine three";
        try (BufferedReader reader = new BufferedReader(new StringReader(data))) {
            String line;
            int n = 1;
            while ((line = reader.readLine()) != null) {
                System.out.println(n++ + ": " + line);
            }
        }
        System.out.println();
    }

    /** Exercise C sample: reverse word order */
    static void demoExerciseReverseWords() {
        System.out.println("--- Reverse words ---");
        String input = "Java is fun";
        String[] words = input.split(" ");
        StringBuilder reversed = new StringBuilder();
        for (int i = words.length - 1; i >= 0; i--) {
            if (i < words.length - 1) {
                reversed.append(' ');
            }
            reversed.append(words[i]);
        }
        System.out.println(input + " -> " + reversed);
    }
}
