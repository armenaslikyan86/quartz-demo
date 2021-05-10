package com.example.quartzdemo;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Scanner;

public class JavaCode {

    public static void main(String[] args) {

        final Scanner scanner = new Scanner(System.in);

        String textToAccummulate = scanner.nextLine();



        System.out.println(accum(textToAccummulate));
    }


    static String accum(final String word) {

        String accumulatedText = "";

        for (int i = 0; i < word.length(); ++i) {
            for (int j = 0; j < i + 1; ++j) {
                if (j == 0) {
                    accumulatedText += String.valueOf(word.charAt(i)).toUpperCase();
                } else {
                    accumulatedText += String.valueOf(word.charAt(i)).toLowerCase();
                }
            }
            if (i != word.length() -1) {
                accumulatedText += '-';
            }
        }
        return accumulatedText;
    }
}
