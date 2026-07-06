package com.bootcamp.playground;

/**
 * STEP 01 playground — compile & run without Spring:
 *   cd java-interview-bootcamp
 *   javac -d target/playground src/main/java/com/bootcamp/playground/Step01Basics.java
 *   java -cp target/playground com.bootcamp.playground.Step01Basics
 */
public class Step01Basics {

    public static void main(String[] args) {
        System.out.println("=== STEP 01: Java Basics ===");

        // Primitives (stored on stack / inline in objects)
        int copies = 3;
        double lateFee = 2.50;
        boolean isMember = true;
        char shelf = 'A';

        // Reference type (object on heap, variable holds reference)
        String title = "Clean Code";

        System.out.println("Book: " + title + ", copies=" + copies + ", fee=" + lateFee);

        greetMember("Alice");
        System.out.println("Sum of loan days: " + add(7, 7));
    }

    static void greetMember(String name) {
        System.out.println("Welcome, " + name + "!");
    }

    static int add(int a, int b) {
        return a + b;
    }
}
