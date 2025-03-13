package org.example;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.exit(2);
        }
        else if (args[0].length() < 8) {
            System.exit(1);
        }
        else {
            System.exit(0);
        }
    }
}