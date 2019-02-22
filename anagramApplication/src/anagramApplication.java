import javafx.util.Pair;

import java.awt.*;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

public class anagramApplication {
    /**
     * Max string length to be checked using @see ModPrimes
     * Mod check is faster, but only works for words that fit into ulong (64b) without overflow.
     * As highest prime used is 157 and ulong64.MaxValue is 18446744073709551615, this makes 8 chars length.
     * Use https://www.rapidtables.com/calc/math/Log_Calculator.html
     */
    private final static int MaxLengthForModPrimes = 8;

    public static void main(String[] args) throws IOException {

        long startTime = System.currentTimeMillis();
        StringBuffer result = new StringBuffer();   // is multi-threaded
        List<String> dictionary = Files.readAllLines(Paths.get(args[0]), Charset.forName("windows-1257"));  // Dictionary is "cp-1257"
        // This is to find best and fastest.
//        FindAll(dictionary, s->s, anagramApplication::IsAnagram);   // 821.021, 9806
//        anagramApplication.<HashMap<Character,Integer>>FindAll(dictionary, anagramApplication::CountChars, anagramApplication::HasEnoughChars); // 452.954, 9806
//        anagramApplication.<Long>FindAll(dictionary, anagramApplication::ComputePrimes, (p, s) -> p == anagramApplication.ComputePrimes(s));   // 182.895, 9806, parallelStream() 40.244
//        anagramApplication.<Long>FindAll(dictionary, anagramApplication::ComputeAdd, (p, s) -> p == anagramApplication.ComputeAdd(s)); // 181.239, 16.824, can try as pre-filter: 40910, 9.820, only 14 f-p!
//        anagramApplication.<Long>FindAll(dictionary, anagramApplication::ComputeXor, (p,s) -> p==anagramApplication.ComputeXor(s)); // 153.414, 89.130 too many false positives, negate!
//        FindAllAdd(dictionary);         // 194.397, 9806 => best win parallelStream() 51.578, new bitmask 46.792, super-win!
//        FindAllPrimes(dictionary);      // 251.863, 9806, parallelStream() 53.125, 52.148
//        for (Map.Entry<String, List<String>> e : FindAll(...).entrySet()) {
//            System.out.println(e.getKey() + ": " + String.join(",", e.getValue()));
//        }
        // Outcome:
        // If input.length() < MaxLengthForModPrimes
        //      use CheckModPrimes
        // Else
        //      use ComputeAdd, validate false-positives using ComputePrimes

        String input = args[1];
        int inputLength = input.length();

        // Here: also, parallelStream has more overhead, do not use!
        if (inputLength <= MaxLengthForModPrimes) {
            for (String word : dictionary) {
                long inputRes = ComputePrimes(input);
                if (
                        inputLength == word.length()
                                && CheckModPrimes(inputRes, word)
                                && !input.equalsIgnoreCase(word)
                ) {
                    result.append(",").append(word);
                }
            }
        } else {
            for (String word : dictionary) {
                long inputRes = ComputeAdd(input);
                if (
                        inputLength == word.length()
                                && inputRes == ComputeAdd(word)
                                && ComputePrimes(input) == ComputePrimes(word)    // Catch out those 14 f-s!
                                && !input.equalsIgnoreCase(word)
                ) {
                    result.append(",").append(word);
                }
            }
        }
        long stop = System.currentTimeMillis() - startTime;
        System.out.print(stop);
        System.out.print(result);
    }

    /**
     * Very first implementation which is awlays correct - use are ref.
     *
     * @param strA
     * @param strB
     * @return
     */
    public static Boolean IsAnagram(String strA, String strB) {
        return
                strA.length() == strB.length()
                        && HasEnoughChars(CountChars(strB), strA);
    }

    /**
     * Get char count per char in word
     *
     * @param word
     * @return Number of occurence in word by char (all to lower).
     */
    private static HashMap<Character, Integer> CountChars(String word) {
        HashMap<Character, Integer> result = new HashMap<>();
        for (char ch : word.toLowerCase().toCharArray()) {
            int count = result.getOrDefault(ch, 0) + 1;
            result.put(ch, count);
        }

        return result;
    }

    /**
     * Very primitive implementation of char-counting
     *
     * @param symbolCount
     * @param word
     * @return
     */
    private static Boolean HasEnoughChars(HashMap<Character, Integer> symbolCount, String word) {
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

    /**
     * This is generated from <see cref="Analyser.CodeGen"/> (in netCore part)
     */
    private static long[] Primes =
            {
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 5, 0, 0,
                    0, 0, 0, 3, 0, 0, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 137, 43, 37, 59,
                    127, 97, 41, 113, 157, 47, 107, 103, 101, 109, 151, 67, 2, 61, 139, 131, 149, 79, 29, 7, 17, 31, 0, 0, 0, 0,
                    0, 0, 137, 43, 37, 59, 127, 97, 41, 113, 157, 47, 107, 103, 101, 109, 151, 67, 2, 61, 139, 131, 149, 79, 29,
                    7, 17, 31, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 89, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 73, 71, 0, 0, 0, 0, 0, 83, 0, 0, 0, 0,
                    0, 0, 0, 89, 0, 0, 0, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 73, 71, 0, 0, 0, 0, 0, 83, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 53, 53, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 23
            };

    /**
     * This is generated from <see cref="Analyser.CodeGen"/> (in netCore part)
     * For each symbol I have one bit set in lookup
     * Theoretically, this means, if I Xor all symbols, for two words, and results are equal
     * - This is anagram.
     * - The difference is 2 letters.
     * - If length of words is same, I may only have false positives like ('nöör'/'noor' or 'papa'/'mama')
     * => Once this check is true, a real <see cref="ComputePrimes"/> should be performed.
     * -> After modifying bitmasks, I have only 14 false-positives, i.e. 7 collisions:
     *      pööninguaken != rinnakõrgune / rinnakõrgune != pööninguaken
     *      pööv != võrr / võrr != pööv
     *      püük != kärr / kärr != püük / kärr != küüp / küüp != kärr
     *      lõrrama != lööpama / lööpama != lõrrama
     *      õrr != ööp / ööp != õrr
     *      üldteada != tavarelv / tavarelv != üldteada
     */
    private static final long[] Bitmasks =
            {
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    2251799813685248L, 576460752303423488L, 0, 0, 0, 0, 0, 288230376151711744L, 0, 0, 0, 0, 0, 9007199254740992L, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 281474976710656L, 70368744177664L, 137438953472L,
                    524288, 35184372088832L, 140737488355328L, 65536, 1, 562949953421312L, 2147483648L, 268435456, 33554432,
                    17179869184L, 8192, 549755813888L, 144115188075855872L, 274877906944L, 128, 4194304, 1024, 4398046511104L,
                    36028797018963968L, 1152921504606846976L, 4503599627370496L, 72057594037927936L, 0, 0, 0, 0, 0, 0, 16,
                    281474976710656L, 70368744177664L, 137438953472L, 524288, 35184372088832L, 140737488355328L, 65536, 1,
                    562949953421312L, 2147483648L, 268435456, 33554432, 17179869184L, 8192, 549755813888L, 144115188075855872L,
                    274877906944L, 128, 4194304, 1024, 4398046511104L, 36028797018963968L, 1152921504606846976L, 4503599627370496L,
                    72057594037927936L, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 17592186044416L, 0, 0, 0, 0, 2305843009213693952L, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    2199023255552L, 1099511627776L, 0, 0, 0, 0, 0, 8796093022208L, 0, 0, 0, 0, 0, 0, 0, 17592186044416L, 0, 0, 0, 0,
                    2305843009213693952L, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2199023255552L, 1099511627776L, 0, 0, 0, 0, 0,
                    8796093022208L, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 1125899906842624L, 1125899906842624L, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 18014398509481984L, 18014398509481984L

            };

    /**
     * Check using prime computation and comparison.
     *
     * @param strA
     * @param strB
     * @return
     */
    static Boolean IsAnagramPrimes(String strA, String strB) {
        int s1L = strA.length();
        if (s1L != strB.length())
            return false;
        long s1 = ComputePrimes(strA);
        return s1L <= MaxLengthForModPrimes ? CheckModPrimes(s1, strB) : s1 == ComputePrimes(strB);
    }

    /**
     * Compute prime product for word.
     *
     * @param word
     * @return
     */
    private static long ComputePrimes(String word) {
        long result = 1;
        for (char l : word.toCharArray()) {
            result *= Primes[l];
        }
        return result;
    }

    /**
     * Check by modulo from primes calculation
     * - This only works for words of length log base 200 of Long.MAX_VALUE, effectively sizeof(ulong)/8. And we use ulong due to x64 processors, suppose, x32 will be slower.
     *
     * @param cmp
     * @param word
     * @return
     */
    private static Boolean CheckModPrimes(long cmp, String word) {
        for (char l : word.toCharArray()) {
            long mul = Primes[l];
            if (cmp % mul == 0) {
                cmp /= mul;
            } else {
                return false;
            }
        }
        return cmp == 1;
    }

    /**
     * Computing XorBitmask
     *
     * @param word
     * @return
     */
    private static long ComputeXor(String word) {
        long result = 0;
        for (char l : word.toCharArray()) {
            result ^= Bitmasks[l];
        }
        return result;
    }

    /**
     * This is generated from <see cref="Analyser.CodeGen"/> (iIBITn netCore part)
     * For each symbol I have one bit set in lookup
     * Theoretically, this means, if I Add all symbols, for two words, and results are equal
     * - This is anagram.
     * - I can have some collisions, but I cannot think out an example, may be there is none!
     * - Theoretically, this is shall only work, if + is faster than *
     *
     * @param word
     * @return
     */
    private static long ComputeAdd(String word) {
        long result = 0;
        for (char l : word.toCharArray()) {
            result += Bitmasks[l];
        }
        return result;
    }


    /**
     * Get global list of anagrams.
     * - Used to find out fastest run.
     *
     * @param dictionary
     * @param prepare
     * @param compare
     * @return
     */
    static <T> Map<String, List<String>> FindAll(List<String> dictionary, Function<String, T> prepare, BiPredicate<T, String> compare) {
        long startTime = System.currentTimeMillis();
        List<Pair<String, String>> anagrams = Collections.synchronizedList(new ArrayList<>());
        dictionary.parallelStream().forEach(w1 -> {
            int w1Len = w1.length();
            T w1prep = prepare.apply(w1);
            dictionary.parallelStream().forEach(w2 ->
            {
                if (
                        w1Len == w2.length()
                                && compare.test(w1prep, w2)
                                && !w1.equalsIgnoreCase(w2)  // Too simple, do not anagram yourself
                ) {
                    anagrams.add(new Pair<>(w1, w2));
                }
            });
        });
        long stop = System.currentTimeMillis() - startTime;
        System.out.println(stop);
        System.out.println(anagrams.size());
        Map<String, List<String>> result =
                anagrams.stream().collect(Collectors.groupingBy(
                        Pair::getKey,
                        Collectors.mapping(Pair::getValue, Collectors.toList())
                ));
        return result;
    }

    static Map<String, List<String>> FindAllPrimes(List<String> dictionary) {
        long startTime = System.currentTimeMillis();
        List<Pair<String, String>> anagrams = Collections.synchronizedList(new ArrayList<>());
        dictionary.parallelStream().forEach(w1 ->
        {
            int w1Len = w1.length();
            long w1prep = ComputePrimes(w1);
            dictionary.parallelStream().forEach(w2 -> {
                if (
                        w1Len == w2.length()
                                && w1prep == ComputePrimes(w2)
                                && !w1.equalsIgnoreCase(w2)
                ) {
                    anagrams.add(new Pair<>(w1, w2));
                }
            });
        });

        long stop = System.currentTimeMillis() - startTime;
        System.out.println(stop);
        System.out.println(anagrams.size());
        Map<String, List<String>> result =
                anagrams.stream().collect(Collectors.groupingBy(
                        Pair::getKey,
                        Collectors.mapping(Pair::getValue, Collectors.toList())
                ));
        return result;
    }

    static Map<String, List<String>> FindAllAdd(List<String> dictionary) {
        long startTime = System.currentTimeMillis();
        List<Pair<String, String>> anagrams = Collections.synchronizedList(new ArrayList<>());
        dictionary.parallelStream().forEach(w1 ->
        {
            int w1Len = w1.length();
            long w1prep = ComputeAdd(w1);
            dictionary.parallelStream().forEach(w2 -> {
                if (
                        w1Len == w2.length()
                                && w1prep == ComputeAdd(w2)
                                && ComputePrimes(w1) == ComputePrimes(w2)
                                && !w1.equalsIgnoreCase(w2)
                ) {
                    anagrams.add(new Pair<>(w1, w2));
                }
            });
        });
        long stop = System.currentTimeMillis() - startTime;
        System.out.println(stop);
        System.out.println(anagrams.size());
        Map<String, List<String>> result =
                anagrams.stream().collect(Collectors.groupingBy(
                        Pair::getKey,
                        Collectors.mapping(Pair::getValue, Collectors.toList())
                ));
        return result;
    }
}
