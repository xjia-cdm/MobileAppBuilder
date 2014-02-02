
package org.javax; 

import java.util.regex.Pattern;

public class StringUtil {

    /**
     * This converts a possibly negative index to a real index into the array.
     *
     * @param i    the unnormalised index
     * @param size the array size
     * @return the normalised index
     */
    protected static int normaliseIndex(int i, int size) {
        int temp = i;
        if (i < 0) {
            i += size;
        }
        if (i < 0) {
            throw new ArrayIndexOutOfBoundsException("Negative array index [" + temp + "] too large for array size " + size);
        }
        return i;
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>String</code>.
     *
     * @param text a String
     * @return the length of the String
     * @since 1.0
     */
    public static int size(String text) {
        return text.length();
    }

    /**
     * Provide the standard Groovy <code>size()</code> method for <code>StringBuffer</code>.
     *
     * @param buffer a StringBuffer
     * @return the length of the StringBuffer
     * @since 1.0
     */
    public static int size(StringBuffer buffer) {
        return buffer.length();
    }

    /**
     * Support the subscript operator for String.
     *
     * @param text  a String
     * @param index the index of the Character to get
     * @return the Character at the given index
     * @since 1.0
     */
    public static String getAt(String text, int index) {
        index = normaliseIndex(index, text.length());
        return text.substring(index, index + 1);
    }

    /**
     * Creates a new string which is the reverse (backwards) of this string
     *
     * @param self a String
     * @return a new string with all the characters reversed.
     * @since 1.0
     * @see java.lang.StringBuilder#reverse()
     */
    public static String reverse(String self) {
        return new StringBuilder(self).reverse().toString();
    }

    /**
     * Appends the String representation of the given operand to this string.
     *
     * @param left  a String
     * @param value any Object
     * @return the new string with the object appended
     * @since 1.0
     */
    public static String plus(String left, Object value) {
        return left + value.toString();
    }

    /**
     * Remove a part of a String. This replaces the first occurrence
     * of target within self with '' and returns the result. If
     * target is a regex Pattern, the first occurrence of that
     * pattern will be removed (using regex matching), otherwise
     * the first occurrence of target.toString() will be removed.
     *
     * @param self   a String
     * @param target an object representing the part to remove
     * @return a String minus the part to be removed
     * @since 1.0
     */
    public static String minus(String self, Object target) {
        if (target instanceof Pattern) {
            return ((Pattern)target).matcher(self).replaceFirst("");
        }
        String text = target.toString();
        int index = self.indexOf(text);
        if (index == -1) return self;
        int end = index + text.length();
        if (self.length() > end) {
            return self.substring(0, index) + self.substring(end);
        }
        return self.substring(0, index);
    }

    /**
     * Provide an implementation of contains() like
     * {@link java.util.Collection#contains(java.lang.Object)} to make Strings more polymorphic.
     * This method is not required on JDK 1.5 onwards
     *
     * @param self a String
     * @param text a String to look for
     * @return true if this string contains the given text
     * @since 1.0
     */
    public static boolean contains(String self, String text) {
        int idx = self.indexOf(text);
        return idx >= 0;
    }

    /**
     * Count the number of occurencies of a substring.
     *
     * @param self a String
     * @param text a substring
     * @return the number of occurrencies of the given string inside this String
     * @since 1.0
     */
    public static int count(String self, String text) {
        int answer = 0;
        for (int idx = 0; true; idx++) {
            idx = self.indexOf(text, idx);
            if (idx >= 0) {
                ++answer;
            } else {
                break;
            }
        }
        return answer;
    }

    /**
     * This method is called by the ++ operator for the class String.
     * It increments the last character in the given string. If the
     * character in the string is Character.MAX_VALUE a Character.MIN_VALUE
     * will be appended. The empty string is incremented to a string
     * consisting of the character Character.MIN_VALUE.
     *
     * @param self a String
     * @return an incremented String
     * @since 1.0
     */
    public static String next(String self) {
        StringBuilder buffer = new StringBuilder(self);
        if (buffer.length() == 0) {
            buffer.append(Character.MIN_VALUE);
        } else {
            char last = buffer.charAt(buffer.length() - 1);
            if (last == Character.MAX_VALUE) {
                buffer.append(Character.MIN_VALUE);
            } else {
                char next = last;
                next++;
                buffer.setCharAt(buffer.length() - 1, next);
            }
        }
        return buffer.toString();
    }

    /**
     * This method is called by the -- operator for the class String.
     * It decrements the last character in the given string. If the
     * character in the string is Character.MIN_VALUE it will be deleted.
     * The empty string can't be decremented.
     *
     * @param self a String
     * @return a String with a decremented digit at the end
     * @since 1.0
     */
    public static String previous(String self) {
        StringBuilder buffer = new StringBuilder(self);
        if (buffer.length() == 0) throw new IllegalArgumentException("the string is empty");
        char last = buffer.charAt(buffer.length() - 1);
        if (last == Character.MIN_VALUE) {
            buffer.deleteCharAt(buffer.length() - 1);
        } else {
            char next = last;
            next--;
            buffer.setCharAt(buffer.length() - 1, next);
        }
        return buffer.toString();
    }

    /**
     * Repeat a String a certain number of times.
     *
     * @param self   a String to be repeated
     * @param factor the number of times the String should be repeated
     * @return a String composed of a repetition
     * @throws IllegalArgumentException if the number of repetitions is &lt; 0
     * @since 1.0
     */
    public static String multiply(String self, Number factor) {
        int size = factor.intValue();
        if (size == 0)
            return "";
        else if (size < 0) {
            throw new IllegalArgumentException("multiply() should be called with a number of 0 or greater not: " + size);
        }
        StringBuilder answer = new StringBuilder(self);
        for (int i = 1; i < size; i++) {
            answer.append(self);
        }
        return answer.toString();
    }





}