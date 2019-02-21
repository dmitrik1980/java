import javafx.util.Pair;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class anagramApplication {


    /// <summary>Max string length to be checked using <see cref="ModPrimes"/></summary>
    /// <remarks>
    /// Mod check is faster, but only works for words that fit into ulong (64b) without overflow.
    /// As highest prime used is 157 and ulong64.MaxValue is 18446744073709551615, this makes 8 chars length.
    /// Use https://www.rapidtables.com/calc/math/Log_Calculator.html
    /// </remarks>
    private final static int MaxLengthForModPrimes = 8;

    public static void main(String[] args) throws IOException {

        long startTime = System.currentTimeMillis();
        StringBuilder result = new StringBuilder();
        List<String> dictionary = Files.readAllLines(Paths.get(args[0]), Charset.forName("windows-1257"));  // Dictionary is "cp-1257"
        // This is to find best and fastest.
        //FindAll(dictionary, dictionary);
        String input = args[1];
        int inputLength = input.length();
        Map<Character, Integer> inputChars = CountChars(input);
        long inputPrimes = ComputePrimes(input);
        List<String> anagrams = new ArrayList<>();
        for (String word : dictionary) {
            if (
//                IsAnagram(input, word) // First, 91
//                inputLength == word.length() && HasEnoughChars(word, inputChars) // First, but "inline" check and compute input once, 92
//                IsAnagramPrimes(input, word) // Second, 70
//                inputPrimes == ComputePrimes(word)    //  Second, 68.
                inputLength == word.length() && inputPrimes == ComputePrimes(word)    // Second, 64, process input once, inline length check.
//                inputLength == word.length() && (inputLength < MaxLengthForModPrimes
//                       ? ModPrimes(inputPrimes, word)
//                       : inputPrimes == ComputePrimes(word)) // Third, 62,use ModCheck
            ) {
                result.append(",").append(word);
                anagrams.add(word);
            }
        }
        long stop = System.currentTimeMillis() - startTime;
        System.out.print(String.valueOf(stop) + result);
    }

    /// <summary>First simple implementation</summary>
    /// <param name="strA">String A</param>
    /// <param name="strB">String B</param>
    /// <returns>True, if {strA;strB} is anagram pair</returns>
    public static Boolean IsAnagram(String strA, String strB) {
        return
                strA.length() == strB.length()
                        && HasEnoughChars(strA, CountChars(strB));
    }

    /// <summary>This is first primitive implementation - just count symbols in word.</summary>
    /// <param name="word">Word to split to chars.</param>
    /// <returns>Number of occurence in word by char (all to lower).</returns>
    private static HashMap<Character, Integer> CountChars(String word) {
        HashMap<Character, Integer> result = new HashMap<>();
        for (char ch : word.toLowerCase().toCharArray()) {
            int count = result.getOrDefault(ch, 0) + 1;
            result.put(ch, count);
        }

        return result;
    }

    /// <summary></summary>
    /// <param name="word">Word to check</param>
    /// <param name="symbolCount">Symbol count, occurence by char (to lower)</param>
    /// <see cref="CountChars"/>
    /// <returns>True, if is anagram, but note, length equality is not checked!</returns>
    private static Boolean HasEnoughChars(String word, HashMap<Character, Integer> symbolCount) {
        // As this count shall be modified, I need to "clone" it (shallow-copy enough)
        Map<Character, Integer> decounted = (HashMap<Character, Integer>) symbolCount.clone();
        for (char ch : word.toLowerCase().toCharArray()) {
            int count = decounted.getOrDefault(ch, 0);
            if (0 == count) {
                return false;
            } else {
                decounted.put(ch, --count);
            }
        }

        return true;
    }

    /// <summary>This is generated from <see cref="LearnEstonian"/>.</summary>
    private static long[] Multipliers =
            {
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 11, 0,
                    0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 37, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 157, 53, 23, 83,
                    149, 43, 73, 79, 151, 61, 113, 127, 103, 107, 101, 97, 5, 109, 139, 137, 131, 89, 7, 13, 17, 19, 0, 0, 0, 0,
                    0,
                    0, 157, 53, 23, 83, 149, 43, 73, 79, 151, 61, 113, 127, 103, 107, 101, 97, 5, 109, 139, 137, 131, 89, 7, 13,
                    17, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 71, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 67, 47, 0, 0, 0, 0, 0, 59, 0, 0, 0, 0, 0, 0, 0, 71, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 67, 47, 0, 0, 0, 0, 0, 59, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 41, 41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 31, 31
            };

    /// <summary></summary>
    /// <param name="strA"></param>
    /// <param name="strB"></param>
    /// <returns></returns>
    static Boolean IsAnagramPrimes(String strA, String strB) {
        int s1L = strA.length();
        if (s1L != strB.length())
            return false;
        long s1 = ComputePrimes(strA);
        return s1L <= MaxLengthForModPrimes ? ModPrimes(s1, strB) : s1 == ComputePrimes(strB);
    }

    private static long ComputePrimes(String word) {
        long result = 1;
        for (char l : word.toCharArray()) {
            result *= Multipliers[l];
        }
        return result;
    }

    /// <summary>Check by modulo from primes calculation</summary>
    /// <param name="cmp">ComputePrimes result for</param>
    /// <param name="word"></param>
    /// <returns></returns>
    private static Boolean ModPrimes(long cmp, String word) {
        for (char l : word.toCharArray()) {
            long mul = Multipliers[l];
            if (cmp % mul == 0) {
                cmp /= mul;
            } else {
                return false;
            }
        }
        return cmp == 1;
    }


    /// <summary>Get global list of anagrams.</summary>
    /// <param name="listA"></param>
    /// <param name="listB"></param>
    /// <returns></returns>
    static Map<String, List<String>> FindAll(List<String> listA, List<String> listB) {
        long startTime = System.currentTimeMillis();

        ArrayList<Pair<String, String>> anagrams = new ArrayList<>();
        for (String w1 : listA) {
            int w1Len = w1.length();
            HashMap<Character, Integer> w1Chars = CountChars(w1);
            long w1Primes = ComputePrimes(w1);
            for (String w2 : listB) {
                if (w1.equalsIgnoreCase(w2)) {
                    continue; // Too simple, do not anagram yourself
                }
                if (
//                    IsAnagram(w1, w2)                                         // []
//                    w1Len == w2.length() && HasEnoughChars(w2, w1Chars)       // [452.920, 9806]
//                    IsAnagramPrimes(w1, w2)                                   // []
//                    w1Primes == ComputePrimes(w2)                             // [575.666, 9806]
                        w1Len == w2.length() && w1Primes == ComputePrimes(w2)      // [166.747, 9806]
//                    w1Len == w2.length() && (w1Len < MaxLengthForModPrimes
//                            ? ModPrimes(w1Primes, w2)
//                            : w1Primes == ComputePrimes(w2))            // [249.184, 9806]
                ) {
                    anagrams.add(new Pair<>(w1, w2));
                }
            }
        }
        long stop = System.currentTimeMillis() - startTime;
        System.out.println(stop);
        System.out.println(anagrams.size());
        Map<String, List<String>> result =
                anagrams.stream().collect(Collectors.groupingBy(
                        Pair::getKey,
                        Collectors.mapping(Pair::getValue, Collectors.toList())
                ));
        for (Map.Entry<String, List<String>> e : result.entrySet()) {
            System.out.println(e.getKey() + ": " + String.join(",", e.getValue()));
        }
        System.out.println(stop);
        System.out.println(anagrams.size());
        return result;
    }
}
