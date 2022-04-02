package com.plexobject.deps;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Adler32;
import java.util.zip.CRC32;


public class StringHelper {
    public static final NumberFormat numberFormat = NumberFormat.getNumberInstance();
    public static final char NEGATE_CHAR = '^';
    public static int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE;

    public static String filterDigits(String value)
            throws java.lang.NumberFormatException {
        if (value == null) return null;
        StringBuffer buffer = new StringBuffer(value.length());
        for (int i = 0; i < value.length(); i++) {
            if (Character.isDigit(value.charAt(i))) {
                buffer.append(value.charAt(i));
            }
        }
        return buffer.toString();
    }

    public static String parseTag(String tag, String value) {
        return parseTag(tag, value, ";");
    }

    public static String parseTag(String tag, String value, String delimited) {
        try {
            int starti;
            int endi;
            if (value == null || tag == null) return null;
            while (true) {
                starti = value.indexOf(tag);
                if (starti == -1) return null;
                if (starti == 0 ||
                        Character.isWhitespace(value.charAt(starti - 1)) ||
                        delimited.indexOf(value.charAt(starti - 1)) != -1) break;
                if (starti + tag.length() >= value.length()) return null;
                value = value.substring(starti + tag.length());
            }

            // skip whitespace
            starti += tag.length();
            while (Character.isWhitespace(value.charAt(starti))) starti++;

            if (value.charAt(starti) != '=' && value.charAt(starti) != ':') {
                if (starti < value.length()) return parseTag(tag, value.substring(starti));
                return null;
            }

            // skip = or :
            starti++;

            // skip whitespace
            while (Character.isWhitespace(value.charAt(starti))) starti++;
            endi = starti;
            while (endi < value.length() &&
                    delimited.indexOf(value.charAt(endi)) == -1) {
                if (endi == value.length() - 1) {
                    endi = value.length();
                    break;
                }
                endi++;
            }
            return value.substring(starti, endi);
        } catch (StringIndexOutOfBoundsException e) {
            return null;
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public static long parseLong(String value)
            throws java.lang.NumberFormatException {
        return Long.parseLong(filterDigits(value));
    }

    public static int parseInt(String value)
            throws java.lang.NumberFormatException {
        return Integer.parseInt(filterDigits(value));
    }

    public static float parseFloat(String value)
            throws java.lang.NumberFormatException {
        return new Float(value).floatValue();
    }

    public static double parseDouble(String value)
            throws java.lang.NumberFormatException {
        return new Double(value).doubleValue();
    }

    public static String[] quotewords(String delim, boolean keep, String line) {
        ArrayList list = new ArrayList();
        StringBuffer sb = new StringBuffer();
        char ch = ' ';
        char lastch = ' ';
        boolean quote = false;
        for (int i = 0; i < line.length(); i++) {
            ch = line.charAt(i);
            if (lastch != '\\' && ch == '"') {
                if (keep) sb.append(ch);
                quote = !quote;
            } else if (lastch != '\\' && !quote && delim.indexOf(ch) != -1) {
                if (sb.length() > 0) list.add(sb.toString());
                sb.setLength(0);
            } else if (ch == '\\' && lastch != '\\') {
            } else {
                sb.append(ch);
            }
            lastch = ch;
        }
        if (sb.length() > 0) list.add(sb.toString());
        String[] s = new String[list.size()];
        for (int i = 0; i < list.size(); i++) s[i] = (String) list.get(i);
        return s;
    }

    public static String replace(String str, String frompat, String topat) {
        Pattern p = Pattern.compile(frompat);
        Matcher matcher = p.matcher(str);
        return matcher.replaceAll(topat);
    }

    public static String[] lineSplit(String text, boolean index) {
        ArrayList list = new ArrayList();
        if (index) {
            int n = -1;
            while ((n = text.indexOf('\n')) != -1) {
                list.add(text.substring(0, n));
                text = text.substring(n + 1);
            }
            if (!text.equals("")) list.add(text);
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(text.getBytes())));
            String line = null;
            try {
                while ((line = in.readLine()) != null) {
                    list.add(line);
                }
            } catch (IOException e) {
            }
        }
        String[] content = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            content[i] = (String) list.get(i);
        }
        return content;
    }

    public static String[] split(String str) {
        if (str == null) return null;
        return split(str, null);
    }

    public static String[] split(String str, String delim) {
        if (str == null) return null;
        str = str.trim();
        if (str.length() == 0) return new String[0];
        Vector vec = new Vector();
        StringTokenizer st = null;
        if (delim == null) st = new StringTokenizer(str);
        else st = new StringTokenizer(str, delim);
        while (st.hasMoreTokens()) {
            vec.addElement(st.nextToken());
        }
        String[] tokens = new String[vec.size()];
        vec.copyInto(tokens);
        return tokens;
    }

    // full split even if token is empty
    public static String[] fsplit(String str, String delim) {
        if (str == null) return null;
        str = str.trim();
        if (str.length() == 0) return new String[0];
        Vector vec = new Vector();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            boolean isDelim = false;
            for (int j = 0; j < delim.length(); j++) {
                if (str.charAt(i) == delim.charAt(j)) {
                    isDelim = true;
                    break;
                }
            }
            if (isDelim) {
                vec.addElement(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(str.charAt(i));
            }
        }
        vec.addElement(sb.toString());

        String[] tokens = new String[vec.size()];
        vec.copyInto(tokens);
        return tokens;
    }

    public static String join(String[] tokens) {
        if (tokens == null) return "";
        return join(tokens, 0, tokens.length, null);
    }

    public static String join(String[] tokens, int offset) {
        if (tokens == null) return "";
        return join(tokens, offset, tokens.length, null);
    }

    public static String join(String[] tokens, String delim) {
        if (tokens == null) return "";
        return join(tokens, 0, tokens.length, delim);
    }

    public static String join(String[] tokens, int offset, String delim) {
        return join(tokens, offset, tokens.length, delim);
    }

    public static String join(String[] tokens, int offset, int length,
                              String delim) {
        if (tokens == null) return "";
        if (tokens.length == 0) return "";
        if (tokens.length == 1 && offset == 0) return tokens[0].trim();
        if (length > tokens.length) {
            throw new IllegalArgumentException("StringHelper.join() length is beyond array's size");
        }

        if (delim == null) delim = " ";
        StringBuffer buffer = new StringBuffer();
        for (int i = offset; tokens != null && i < length; i++) {
            buffer.append(tokens[i]);
            if (i < (length - 1)) {
                buffer.append(delim);
            }
        }
        return buffer.toString();
    }

    public static String skipDigits(String string) {
        if (string == null) return null;
        int i = 0;
        for (i = 0; string != null && i < string.length() &&
                Character.isDigit(string.charAt(i)); i++) {
        }
        if (i < string.length()) {
            return string.substring(i);
        } else {
            return null;
        }
    }

    public static String skipUntilDigits(String string) {
        if (string == null) return null;
        int i = 0;
        for (i = 0; string != null && i < string.length() &&
                !Character.isDigit(string.charAt(i)); i++) {
        }
        if (i < string.length()) {
            return string.substring(i);
        } else {
            return null;
        }
    }

    public static String skipLetters(String string) {
        if (string == null) return null;
        int i = 0;
        for (i = 0; string != null && i < string.length() &&
                Character.isLetter(string.charAt(i)); i++) {
        }
        if (i < string.length()) {
            return string.substring(i);
        } else {
            return null;
        }
    }

    public static String skipUntilLetters(String string) {
        if (string == null) return null;
        int i = 0;
        for (i = 0; string != null && i < string.length() &&
                !Character.isLetter(string.charAt(i)); i++) {
        }
        if (i < string.length()) {
            return string.substring(i);
        } else {
            return null;
        }
    }

    public static String skipLetterOrDigits(String string) {
        if (string == null) return null;
        int i = 0;
        for (i = 0; string != null && i < string.length() &&
                Character.isLetterOrDigit(string.charAt(i)); i++) {
        }
        if (i < string.length()) {
            return string.substring(i);
        } else {
            return null;
        }
    }

    public static String skipUntilLetterOrDigits(String string) {
        if (string == null) return null;
        int i = 0;
        for (i = 0; string != null && i < string.length() &&
                !Character.isLetterOrDigit(string.charAt(i)); i++) {
        }
        if (i < string.length()) {
            return string.substring(i);
        } else {
            return null;
        }
    }

    public static String skipWhitespaces(String string) {
        if (string == null) return null;
        int i = 0;
        for (i = 0; string != null && i < string.length() &&
                Character.isWhitespace(string.charAt(i)); i++) {
        }
        if (i < string.length()) {
            return string.substring(i);
        } else {
            return null;
        }
    }

    public static String skipUntilWhitespaces(String string) {
        if (string == null) return null;
        int i = 0;
        for (i = 0; string != null && i < string.length() &&
                !Character.isWhitespace(string.charAt(i)); i++) {
        }
        if (i < string.length()) {
            return string.substring(i);
        } else {
            return null;
        }
    }

    public static String skip(String string, char letter) {
        if (string == null) return null;
        int i = 0;
        for (i = 0; string != null && i < string.length() &&
                string.charAt(i) == letter; i++) {
        }
        if (i < string.length()) {
            return string.substring(i);
        } else {
            return null;
        }
    }

    public static String skipUntil(String string, char letter) {
        if (string == null) return null;
        int i = 0;
        for (i = 0; string != null && i < string.length() &&
                string.charAt(i) != letter; i++) {
        }
        if (i < string.length()) {
            return string.substring(i);
        } else {
            return null;
        }
    }

    public static String skip(String string, String delim) {
        if (string == null) return null;
        int i = 0;
        for (i = 0; string != null && i < string.length(); i++) {
            boolean found = false;
            for (int j = 0; delim != null && j < delim.length(); j++) {
                if (string.charAt(i) == delim.charAt(j)) {
                    found = true;
                    break;
                }
                if (!found) break;
            }
        }
        if (i < string.length()) {
            return string.substring(i);
        } else {
            return null;
        }
    }

    public static String stripHTML(String html) {
        if (html == null) return null;

        Pattern p = null;
        Matcher matcher = null;

/*
    p = Pattern.compile("<head>.*<.head>", FLAGS);
    matcher = p.matcher(html);
    html = matcher.replaceAll("");

    p = Pattern.compile("<style>.*<.style>", FLAGS);
    matcher = p.matcher(html);
    html = matcher.replaceAll("");

    p = Pattern.compile("<javascript>.*<.javascript>", FLAGS);
    matcher = p.matcher(html);
    html = matcher.replaceAll("");
*/


        //p = Pattern.compile("<span>.*<.span>", FLAGS);
        //matcher = p.matcher(html);
        //html = matcher.replaceAll("");

/*
    p = Pattern.compile("<.--.*-->", FLAGS);
    matcher = p.matcher(html);
    html = matcher.replaceAll("");
*/

        StringBuffer buffer = new StringBuffer();
        char ch;
        int length = html.length();
        char last = '\u0000';
        boolean sgml = false;
        boolean tag = false;
        char quote = '\u0000';

/*
    for (int i=0; i<length; i++) {
      ch = html.charAt(i);
      if (ch == '<') {
        for (; i<length && html.charAt(i) != '>';) {
          if (html.charAt(i) == '\n') buffer.append(html.charAt(i));
          i++;
        }
      } else {
        buffer.append(ch);
      } 
    }
*/

        int i = 0;
        while (i < length) {
            ch = html.charAt(i++);
            if (ch == quote) {
                if (ch == '-' && last != '-') {
                    last = ch;
                    continue;
                } else {
                    last = '\u0000';
                }
                quote = '\u0000';
            } else if (quote == '\u0000') {
                if (ch == '<') {
                    tag = true;
                    if (i < length && html.charAt(i++) == '!') {
                        //String s = join('', @chars[$i .. $i + 10]);
                        sgml = true;
                    }
                } else if (ch == '>') {
                    if (tag) {
                        sgml = false;
                        tag = false;
                    }
                } else if (ch == '-') {
                    if (sgml && last == '-') {
                        quote = '-';
                    } else {
                        if (!tag) buffer.append(ch);
                    }
                } else if (ch == '"' || ch == '\'') {
                    if (tag) {
                        quote = ch;
                    } else {
                        if (!tag) buffer.append(ch);
                    }
                } else if (ch == '&') {
                    int[] lenchar = htmlXlat(html, i); // check i >= html.length ?????
                    // causing array error 12/19/03 SAB
                    int len = lenchar[0];
                    if (len != 0) {
                        ch = (char) lenchar[1];
                        buffer.append(ch);
                        i += len;
                    } else {
                        buffer.append(ch);
                    }
                } else {
                    if (!tag) buffer.append(ch);
                }
            } // if (quote == '\u0000')
            last = ch;
        }

        //if (true) return unfilterHtmlTags(buffer.toString());
        if (true) return buffer.toString();

        p = Pattern.compile("&#[0-9]+;", FLAGS);
        matcher = p.matcher(buffer.toString());
        return matcher.replaceAll("");
    }


    //
    String html = "<table border=\"0\" cols=\"2\" cellspacing=\"0\" cellpadding=\"0\"><tr><td valign=\"top\" class=\"MNSEven\" nowrap><span class=\"MNSSummaryLabel\">Career Level:</span>&nbsp;</td><td class=\"MNSEven\">Experienced (Non-Manager)</td></tr><tr><td valign=\"top\" class=\"MNSEven\" nowrap><span class=\"MNSSummaryLabel\">Job Type:</span>&nbsp;</td><td class=\"MNSEven\">Employee</td></tr><tr><td valign=\"top\" class=\"MNSEven\" nowrap><span class=\"MNSSummaryLabel\">Job Status:</span>&nbsp;</td><td class=\"MNSEven\">Full Time</td></tr></table>";

    public static String lctagForHtml(String html, int length) {
        if (html == null) return null;
        if (html.length() < length) return html.toLowerCase();
        return html.substring(0, length).toLowerCase();
    }

    public static String[][] htmlTable2textArray(String html) {
        List list = htmlTable2text(html);
        String[][] arr = new String[list.size()][];
        int i = 0;
        Iterator it = list.iterator();
        while (it.hasNext()) {
            List ilist = (List) it.next();
            arr[i] = (String[]) ilist.toArray(new String[ilist.size()]);
        }
        return arr;
    }

    public static List htmlTable2text(String html) {
        int start = 0;
        ArrayList list = new ArrayList();
        Stack tdstarts = new Stack();
        boolean href = false;
        Stack rows = new Stack();
        while ((start = html.indexOf("<", start)) != -1) {
            String tag = null;
            if (html.charAt(start + 1) == '/') tag = lctagForHtml(html.substring(start), 4);
            else tag = lctagForHtml(html.substring(start), 3);
            if ("<tr".equals(tag)) {
                rows.push(new ArrayList());
            } else if ("</tr".equals(tag)) {
                List row = (List) rows.pop();
                list.add(row);
            }
            if ("<td".equals(tag)) {
                tdstarts.push(new Integer(start));
            } else if ("</td".equals(tag)) {
                if (tdstarts.size() == 0) throw new RuntimeException("Failed to find valid td " + html);
                int tdstart = ((Integer) tdstarts.pop()).intValue();
                List row = (List) rows.peek();
                if (row == null)
                    throw new RuntimeException("Internal error -- failed to find row " + rows + ">" + html.substring(tdstart, start));
                if (row != null) row.add(html2text(html.substring(tdstart, start)));
                href = false;
            } else if (false && "<a ".equals(tag)) {
                int n = html.indexOf("=", start);
                StringBuffer sb = new StringBuffer();
                for (int i = n + 1; i < html.length(); i++) {
                    char ch = html.charAt(i);
                    if (ch == '>') break;
                    if (Character.isWhitespace(ch) || ch == '"' || ch == '\'') {
                        if (sb.length() > 5) break;
                    } else {
                        sb.append(ch);
                    }
                }
                href = true;
                start += (sb.length() - 1);
                List row = (List) rows.peek();
                if (row == null)
                    throw new RuntimeException("Internal error -- failed to find row " + rows + "> href " + sb);
                if (row != null) row.add(sb.toString());
                tdstarts.pop();
                tdstarts.push(new Integer(start));
                //tdstart = start;
            }
            start++;
        }
        return list;
    }

    public static String[] parseTds(String html) {
        int start = 0;
        ArrayList list = new ArrayList();
        Stack tdstarts = new Stack();
        while ((start = html.indexOf("<", start)) != -1) {
            String tag = null;
            if (html.charAt(start + 1) == '/') tag = lctagForHtml(html.substring(start), 4);
            else tag = lctagForHtml(html.substring(start), 3);
            if ("<td".equals(tag)) {
                tdstarts.push(new Integer(start));
            } else if ("</td".equals(tag)) {
                int tdstart = 0;
                if (tdstarts.size() > 0) tdstart = ((Integer) tdstarts.pop()).intValue();
                list.add(trimWhitespace(html2text(html.substring(tdstart, start))));
            } else if (false && "<a ".equals(tag)) {
                int n = html.indexOf("=", start);
                StringBuffer sb = new StringBuffer();
                for (int i = n + 1; i < html.length(); i++) {
                    char ch = html.charAt(i);
                    if (ch == '>') break;
                    if (Character.isWhitespace(ch) || ch == '"' || ch == '\'') {
                        if (sb.length() > 5) break;
                    } else {
                        sb.append(ch);
                    }
                }
                start += (sb.length() - 1);
                tdstarts.pop();
                tdstarts.push(new Integer(start));
                list.add(sb.toString());
            }
            start++;
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    public static String html2text(String html) {
        if (html == null) return null;
        StringBuffer buffer = new StringBuffer();
        char ch;
        int end;
        int length = html.length();
        for (int i = 0; i < length; i++) {
            ch = html.charAt(i);
            if (ch == '<') {
                StringBuffer tagBuf = new StringBuffer();
                i++;
                for (; i < length && html.charAt(i) != '>'; i++) {
                    tagBuf.append(Character.toLowerCase(html.charAt(i)));
                }
                String tag = tagBuf.toString();
                if (tag.equals("br") || tag.equals("p") || tag.equals("hr")) {
                    buffer.append("\n");
                } else if (tag.equals("li")) {
                    buffer.append("\n  - ");
                } else if (tag.equals("td")) {
                    buffer.append(" ");
                }
            } else {
                end = -1;
                if (html.substring(i).startsWith("&#")) {
                    end = html.indexOf(";", i);
                }
                if (end != -1) {
                    ch = (char) Integer.parseInt(html.substring(i + 2, end));
                    i = end;
                }
                buffer.append(ch);
            }
        }
        //return buffer.toString();
        return unfilterHtmlTags(buffer.toString());
    }


    public static String skipUntil(String string, String delim) {
        if (string == null) return null;
        int i = 0;
        for (i = 0; string != null && i < string.length(); i++) {
            boolean found = false;
            for (int j = 0; delim != null && j < delim.length(); j++) {
                if (string.charAt(i) == delim.charAt(j)) {
                    found = true;
                    break;
                }
                if (found) break;
            }
        }
        if (i < string.length()) {
            return string.substring(i);
        } else {
            return null;
        }
    }

    public static int indexOf(String string, String delim) {
        if (string == null || delim == null) return -1;
        int ndx = Integer.MAX_VALUE;
        for (int i = 0; i < delim.length(); i++) {
            int n = string.indexOf(String.valueOf(delim.charAt(i)));
            if (n < ndx) ndx = n;
        }
        return (ndx == Integer.MAX_VALUE) ? -1 : ndx;
    }
    // pattern -- ###,###,##
    //            ###.##
    //            0000000.00
    //            $###,###.###
    //            \u00a5###,###.###       japanes yen
    // see setDecimalSeparator('|')
    // see setGroupingSeparator('^')
    // see setGroupingSize(4)

    public static String formatNumber(double value, String pattern) {
        DecimalFormat df = (DecimalFormat) numberFormat;
        df.applyPattern(pattern);
        return df.format(value);
    }

    public static String formatNumber(long value, String pattern) {
        DecimalFormat df = (DecimalFormat) numberFormat;
        df.applyPattern(pattern);
        return df.format(value);
    }

    public static String toZeroPaddedString(int number, int digits) {
        StringBuffer buffer = new StringBuffer(String.valueOf(number));
        if (buffer.length() < digits) {
            int n = digits - buffer.length();
            for (int i = 0; i < n; i++) {
                buffer.insert(0, '0');
            }
        }
        return buffer.toString();
    }

    public static String toZeroPaddedString(long number, int digits) {
        StringBuffer buffer = new StringBuffer(String.valueOf(number));
        if (buffer.length() < digits) {
            int n = digits - buffer.length();
            for (int i = 0; i < n; i++) {
                buffer.insert(0, '0');
            }
        }
        return buffer.toString();
    }

    /*
     *  Match text and pattern, return 1 (TRUE), 0 (FALSE), or -1 (ABORT).
     */
    public static int regMatch(String text, String pattern) {
        int i = 0, j = 0;
        int matched = 0;
        if (text == null || pattern == null) return -1;
        for (i = 0, j = 0; i < pattern.length(); i++, j++) {
            if (j == text.length() && pattern.charAt(i) != '*') return -1;

            switch (pattern.charAt(i)) {
                case '\\':
                    // literal mach with following character
                    i++;
                    // fall through
                default:
                    if (text.charAt(j) != pattern.charAt(i)) return 0;
                    continue;
                case '?':
                    // match anything
                    continue;
                case '*':
                    while (++i < pattern.length() && pattern.charAt(i) == '*') {
                        // consecutive starts acts like one
                        continue;
                    }
                    if (i == pattern.length()) {
                        // trailing star matches everything
                        return 1;
                    }
                    while (j < text.length()) {
                        if ((matched = regMatch(text.substring(j), pattern.substring(i)))
                                != 0)
                            return matched;
                        j++;
                    }
                    return -1;
                case '[':
                    boolean reverse = pattern.charAt(i + 1) == NEGATE_CHAR ? true : false;
                    if (reverse) i++; // inverted character class
                    matched = 0;
                    if (i + 1 < pattern.length() && (pattern.charAt(i + 1) == ']' ||
                            pattern.charAt(i + 1) == '-')) {
                        i++;
                        if (pattern.charAt(i) == text.charAt(j)) {
                            matched = 1;
                        }
                    }
                    for (char last = pattern.charAt(i); ++i < pattern.length() &&
                            pattern.charAt(i) != ']'; last = pattern.charAt(i)) {
                        if (pattern.charAt(i) == '-' && i + 1 < pattern.length() &&
                                pattern.charAt(i + 1) != ']') {
                            if (++i < pattern.length() && text.charAt(j)
                                    <= pattern.charAt(i) && text.charAt(j) >= last) {
                                matched = 1;
                            }
                        } else {
                            if (text.charAt(j) == pattern.charAt(i)) {
                                matched = 1;
                            }
                        }
                        if (matched == 1 && reverse) return 0;
                        continue;
                    } // for
            }    // switch
        }
        return j == text.length() ? 1 : 0;
    }

    public static boolean wildMatch(String text, String pattern, boolean icase) {
        if (text == null || pattern == null) return false;
        if (pattern.equals("*")) return true;
        if (icase) {
            text = text.toLowerCase();
            pattern = pattern.toLowerCase();
        }
        return regMatch(text, pattern) == 1;
    }

    public static boolean isAscii(char ch) {
        return isAscii((char) ch);
    }

    public static boolean isAscii(int ch) {
        return (ch != 0 ? 1 : 0 & ~0177) != 0;
    }

    // check if value is not null/Null/NULL
    public static boolean hasText(String str) {
        return hasText(str, false);
    }

    public static boolean hasText(String str, boolean trim) {
        return str != null && (trim ? str.trim().length() > 0 : str.length() > 0);
    }

    public static String strip(String string, String chars) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            if (chars.indexOf(String.valueOf(string.charAt(i))) == -1) {
                buffer.append(string.charAt(i));
            }
        }
        return buffer.toString();
    }

    // check whether a string matches a given wildcard pattern
    public static boolean match(String string, String pattern) {
        for (int p = 0; ; ++p) {
            for (int s = 0; ; ++p, ++s) {
                boolean sEnd = (s >= string.length());
                boolean pEnd = (p >= pattern.length() || pattern.charAt(p) == '|');
                if (sEnd && pEnd) return true;
                if (sEnd || pEnd) break;
                if (pattern.charAt(p) == '?') continue;
                if (pattern.charAt(p) == '*') {
                    int i;
                    ++p;
                    for (i = string.length(); i >= s; --i)
                        if (match(pattern.substring(p),
                                string.substring(i)))  /* not quite right */
                            return true;
                    break;
                }
                if (pattern.charAt(p) != string.charAt(s)) break;
            } // for ( int s = 0; ; ++p, ++s )
            p = pattern.indexOf('|', p);
            if (p == -1) return false;
        } // for ( int p = 0; ; ++p )
    }

    public static byte hexToByte(String x) {
        int n = new Integer("0x" + x).intValue();
        return (byte) n;
    }


    public static String byteToHex(byte byt) {
        int bigByte = byt & 255;
        char a, b;
        byte nibble = (byte) (bigByte & 15);
        if (nibble < 10) a = (char) ('0' + nibble);
        else a = (char) ('A' + nibble - 10);
        nibble = (byte) (bigByte >> 4);
        if (nibble < 10) b = (char) ('0' + nibble);
        else b = (char) ('A' + nibble - 10);
        StringBuffer sb = new StringBuffer();
        sb.append(b);
        sb.append(a);
        return new String(sb);
    }

    public static String toHexString(byte[] b, String separator) {
        if (b == null || b.length == 0 || separator == null || separator.length() == 0) return "";
        int inputLength = b.length;
        if (b.length == 0) return "";
        StringBuffer buffer = new StringBuffer(b.length * (2 + separator.length()) -
                separator.length());
        for (int i = 0; i < b.length; i++) {
            if (i > 0) buffer.append(separator);
            buffer.append(byteToHex(b[i]));
        }
        return buffer.toString();
    }


    public static final String chr(int n) {
        return String.valueOf(new Character((char) n).charValue());
/*
    String s = Integer.toHexString(n);
    int len = s.length();
    if (len == 1) return "\\" + "u000" + s;
    else if (len == 2) return "\\" + "u00" + s;
    else if (len == 3) return "\\" + "u0" + s;
    else return "\\" + "u" + s;
*/
    }

    public static final String[] HTML_SYMBOLS = new String[]{
            "lt", "<", "gt", ">", "amp", "&",
            "quot", "\"", "nbsp", " ", "iexcl", chr(161),
            "cent", chr(162), "pound", chr(163), "curren", chr(164),
            "yen", chr(165), "brvbar", chr(166), "sect", chr(167),
            "uml", chr(168), "copy", chr(169), "ordf", chr(170),
            "laquo", chr(171), "not", chr(172), "shy", chr(173),
            "reg", chr(174), "macr", chr(175), "deg", chr(176),
            "plusmn", chr(177), "sup2", chr(178), "sup3", chr(179),
            "acute", chr(180), "micro", chr(181), "para", chr(182),
            "middot", chr(183), "cedil", chr(184), "sup1", chr(185),
            "ordm", chr(186), "raquo", chr(187), "frac14", chr(188),
            "frac12", chr(189), "frac34", chr(190), "iquest", chr(191),
            "Agrave", chr(192), "Aacute", chr(193), "Acirc", chr(194),
            "Atilde", chr(195), "Auml", chr(196), "Aring", chr(197),
            "AElig", chr(198), "Ccedil", chr(199), "Egrave", chr(200),
            "Eacute", chr(201), "Ecirc", chr(202), "Euml", chr(203),
            "Igrave", chr(204), "Iacute", chr(205), "Icirc", chr(206),
            "Iuml", chr(207), "ETH", chr(208), "Ntilde", chr(209),
            "Ograve", chr(210), "Oacute", chr(211), "Ocirc", chr(212),
            "Otilde", chr(213), "Ouml", chr(214), "times", chr(215),
            "Oslash", chr(216), "Ugrave", chr(217), "Uacute", chr(218),
            "Ucirc", chr(219), "Uuml", chr(220), "Yacute", chr(221),
            "THORN", chr(222), "szlig", chr(223), "agrave", chr(224),
            "aacute", chr(225), "acirc", chr(226), "atilde", chr(227),
            "auml", chr(228), "aring", chr(229), "aelig", chr(230),
            "ccedil", chr(231), "egrave", chr(232), "eacute", chr(233),
            "ecirc", chr(234), "euml", chr(235), "igrave", chr(236),
            "iacute", chr(237), "icirc", chr(238), "iuml", chr(239),
            "eth", chr(240), "ntilde", chr(241), "ograve", chr(242),
            "oacute", chr(243), "ocirc", chr(244), "otilde", chr(245),
            "ouml", chr(246), "divide", chr(247), "oslash", chr(248),
            "ugrave", chr(249), "uacute", chr(250), "ucirc", chr(251),
            "uuml", chr(252), "yacute", chr(253), "thorn", chr(254),
            "yuml", chr(255)
    };

    public static void printBytes(byte[] b, String s) throws Exception {
        for (int i = 0; i < b.length; i++) {
            System.out.println("\t" + s + "[" + i + "] " + b[i] + " " +
                    Integer.toHexString(b[i]) + " " + Integer.toBinaryString(b[i]));
        }
    }

    protected static final String SPECIAL = "`~!@#$%^&*()-_=+\\|]}[{'\";:/?.>,<";

    public static byte[] toBytes16(String s) throws UnsupportedEncodingException {
        ArrayList list = new ArrayList();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int type = Character.getType(c);
            boolean unicode = true;
            switch (type) {
                case Character.LETTER_NUMBER:
                case Character.LINE_SEPARATOR:
                case Character.LOWERCASE_LETTER:
                case Character.TITLECASE_LETTER:
                case Character.UPPERCASE_LETTER:
                case Character.INITIAL_QUOTE_PUNCTUATION:
                case Character.FINAL_QUOTE_PUNCTUATION:
                    //case Character.OTHER_LETTER:
                    //case Character.OTHER_NUMBER:
                    //case Character.OTHER_PUNCTUATION:
                case Character.SPACE_SEPARATOR:
                case Character.DASH_PUNCTUATION:
                case Character.START_PUNCTUATION:
                case Character.END_PUNCTUATION:
                    unicode = false;
                    break;
                case Character.COMBINING_SPACING_MARK:
                case Character.CONNECTOR_PUNCTUATION:
                case Character.CONTROL:
                case Character.CURRENCY_SYMBOL:
                case Character.DECIMAL_DIGIT_NUMBER:
                case Character.ENCLOSING_MARK:
                case Character.FORMAT:
                case Character.MATH_SYMBOL:
                case Character.MODIFIER_LETTER:
                case Character.MODIFIER_SYMBOL:
                case Character.NON_SPACING_MARK:
                case Character.OTHER_SYMBOL:
                case Character.PARAGRAPH_SEPARATOR:
                case Character.PRIVATE_USE:
                case Character.SURROGATE:
                case Character.UNASSIGNED:
                    break;
            }
            unicode = !(Character.isDigit(c) || Character.isUpperCase(c) || Character.isLowerCase(c) || Character.isWhitespace(c) || SPECIAL.indexOf(c) != -1);
            //int n = Character.getNumericValue(c);
            int n = new Character(c).hashCode();
            if (n > 0 && n <= Byte.MAX_VALUE) {
                list.add(new Byte((byte) c));
            } else {
                //int j = i* 2;
                //byte[] x = String.valueOf(c).getBytes("UTF-8");
                //byte[] x = new byte[] {(byte)(c >>>8), (byte)c};
                //list.add(new Byte(x[0]));
                //list.add(new Byte(x[1]));
                byte[] x = String.valueOf(c).getBytes("UTF-16");
                list.add(new Byte(x[3]));
            }
        }
        byte[] byteArr = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Byte b = (Byte) list.get(i);
            byteArr[i] = b.byteValue();
        }
        return byteArr;
/*
    byte byteArr [] = new byte [2 * s.length()];
    for (int i=0;i<s.length();i++) {
      char c = s.charAt(i);
      int j = i* 2; 
      byteArr[j] = (byte)(c >>>8);
      byteArr[j+1] = (byte)c; 
    }
    return byteArr;
*/
    }

    public static int[] htmlXlat(String html, int i) {
        if (html == null || i >= html.length()) return new int[]{0, 0};
        char ch = html.charAt(i);
        if (!Character.isUpperCase(ch) && !Character.isLowerCase(ch)) {
            return new int[]{0, 0};
        }
        // we used to walk till we got a ';', but to be compatible
        // with c, we won't check for ';'
        StringBuffer r_tag = new StringBuffer();
        for (; i < html.length(); i++) {
            ch = html.charAt(i);
            if (!Character.isUpperCase(ch) && !Character.isLowerCase(ch)) break;
            r_tag.append(ch);
        }
        int len = r_tag.length(); // do not include ;
        if (i < html.length() && html.charAt(i) == ';') len++;

        String tag = r_tag.toString();

        String val = null;
        for (i = 0; i < HTML_SYMBOLS.length - 1; i += 2) {
            if (tag.equalsIgnoreCase(HTML_SYMBOLS[i])) {
                val = HTML_SYMBOLS[i + 1];
                break;
            }
        }
        if (val == null) {
            return new int[]{0, 0};
        }
        return new int[]{len, val.charAt(0)};
    }

    public static String unfilterHtmlTags(String text) {
        if (text == null || text.length() == 0) return text;
        int n = 0;
        for (int i = 0; i < HTML_SYMBOLS.length - 1; i += 2) {
            String symbol = "&" + HTML_SYMBOLS[i] + ";";
            while ((n = text.indexOf(symbol)) != -1) {
                if (n > 0) {
                    text = text.substring(0, n) + HTML_SYMBOLS[i + 1] +
                            text.substring(n + symbol.length());
                } else {
                    text = HTML_SYMBOLS[i + 1] + text.substring(n + symbol.length());
                }
            }
        }
        return text;
    }


    public static String filterHtmlTags(String text) {
        if (text == null || text.length() == 0) return text;
        int n = 0;
        for (int i = 0; i < HTML_SYMBOLS.length - 1; i += 2) {
            String symbol = "&" + HTML_SYMBOLS[i] + ";";
            while ((n = text.indexOf(HTML_SYMBOLS[i + 1])) != -1) {
                if (n > 0) {
                    text = text.substring(0, n) + symbol +
                            text.substring(n + HTML_SYMBOLS[i + 1].length());
                } else {
                    text = symbol +
                            text.substring(n + HTML_SYMBOLS[i + 1].length());
                }
            }
        }
        return text;
/*
    StringBuffer sb = new StringBuffer();
    for (int n=0; n<text.length(); n++) {
      char c = text.charAt(n);
      if (c == '<') sb.append("&lt;");
      else if (c == '>') sb.append("&gt;");
      else if (c == '&') sb.append("&amp;");
      else if (c == '"') sb.append("&quot;");
      else sb.append(c);
    }
    return sb.toString();
*/
    }


    public static String toHex(String str) {
        if (str == null || str.length() == 0) return "";
        return toHex(str.getBytes());
    }

    public static String toHex(byte[] data) {
        if (data == null || data.length == 0) return "";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            //if (i != 0) sb.append(":");
            int b = data[i] & 0xff;
            String hex = Integer.toHexString(b);
            if (hex.length() == 1) sb.append("0");
            sb.append(hex);
        }
        return sb.toString();
    }


    /**
     * Returns true if argument is either <code>null</code> or is full of
     * whitespace. The argument is full of whitespace if after calling <code>String.trim()</code>
     * on it, it equals(""). In other words, whitespace determination is done by
     * <code>String.trim()</code>.
     *
     * @param s the String to be tested.
     * @see java.lang.String#trim()
     */
    public static boolean isNullOrWhiteSpace(String s) {
        if (s == null || "".equals(s.trim())) {
            return (true);
        }

        return (false);
    }

    /**
     * Returns the String obtained by removing any whitespace at both ends of the argument.
     * Whitespace is defined as stated by <code>Character.isWhitespace(char)</code>. It is
     * different from <code>String.trim()</code> which also removes any control characters.
     *
     * @param s the String to be trimmed.
     * @return the trimmed String.
     * @see java.lang.Character#isWhitespace(char)
     * @see java.lang.String#trim()
     */
    public static String trimWhitespace(String s) {
        if (s == null) {
            return (s);
        }

        return (trimRightWS(trimLeftWS(s)));
    }

    /**
     * Returns the String obtained by removing any whitespace at the left hand side of the argument.
     * Whitespace is defined as stated by <code>Character.isWhitespace(char)</code>.
     *
     * @param s the String to be trimmed.
     * @return the trimmed String.
     * @see java.lang.Character#isWhitespace(char)
     */
    public static String trimLeftWS(String s) {
        if (s == null) {
            return (s);
        }

        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return (s.substring(i));
            }
        }

        return (s);
    }

    /**
     * Returns the String obtained by removing any whitespace at the right hand side of the argument.
     * Whitespace is defined as stated by <code>Character.isWhitespace(char)</code>.
     *
     * @param s the String to be trimmed.
     * @return the trimmed String.
     * @see java.lang.Character#isWhitespace(char)
     */
    public static String trimRightWS(String s) {
        if (s == null) {
            return (s);
        }

        for (int i = s.length() - 1; i >= 0; i--) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return (s.substring(0, i + 1));
            }
        }

        return (s);
    }

    /**
     * Convenience synonym for <code>trimWhitespace(String)</code>.
     *
     * @param s the String to be trimmed.
     * @return the trimmed String.
     * @see #trimWhitespace(String)
     */
    public static String trimWS(String s) {
        return (trimWhitespace(s));
    }

    /**
     * Tokenizes the argument String into several constituent Strings, separated by commas,
     * and returns the tokens as an array.
     *
     * @param s the String to be tokenized.
     * @return the array of token Strings.
     * @see #delimitedStringToArray(String, String)
     */
    public static String[] csvStringToArray(String s) {
        return (delimitedStringToArray(s, ","));
    }

    /**
     * Tokenizes the argument String into several constituent Strings, separated by specified
     * delimiters, and returns the tokens as an array. Every character of the delimiter String is
     * treated as a token-separator individually.
     *
     * @param s          the String to be tokenized.
     * @param delimiters the String of delimiters.
     * @return the array of token Strings.
     */
    public static String[] delimitedStringToArray(String s, String delimiters) {
        if (isNullOrWhiteSpace(s)) {
            return (null);
        }

        delimiters = (delimiters == null) ? ",;" : delimiters;

        StringTokenizer st = new StringTokenizer(s, delimiters);
        int num;

        if ((num = st.countTokens()) == 0) {
            return (null);
        }

        String array[] = new String[num];

        for (int i = 0; i < num; i++) {
            array[i] = st.nextToken();
        }

        return (array);
    }

    /**
     * Checks whether the specified Object exists in the given array. The <code>Object.equals(Object)</code>
     * method is used to test for equality.
     *
     * @param obj   the Object to be searched for.
     * @param array the array which has to be searched.
     * @return <code>true</code> if the Object exists in the array; <code>false</code> if the Object
     * does not exist in the array, or the object, or the array is <code>null</code>.
     * @see java.lang.Object#equals(Object)
     */
    public static boolean isInArray(Object obj, Object[] array) {
        if (obj == null || array == null) {
            return (false);
        }

        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(obj)) {
                return (true);
            }
        }

        return (false);
    }

    /**
     * Counts the number of times the specified character appears in the input String. For example,
     * <code>countInstances("abcabcdabcde",'a')</code> returns 3.
     *
     * @param s the String to be counted in.
     * @param c the character to be counted.
     * @return the number of occurrences of <code>c</code> in <code>s</code> ; -1 if <code>s</code> is <code>null</code>.
     */
    public static int countInstances(String s, char c) {
        if (isNullOrWhiteSpace(s)) {
            return (-1);
        }

        int count = 0;

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                count++;
            }
        }

        return (count);
    }

    /**
     * Counts the number of times the specified String appears in the input String. For example,
     * <code>countInstances("abcabcdabcde","cd")</code> returns 2.
     *
     * @param s the String to be counted in.
     * @param c the String to be counted.
     * @return the number of occurrences of <code>c</code> in <code>s</code> ; -1 if <code>s</code> or <code>c</code> is <code>null</code>.
     */
    public static int countInstances(String s, String c) {
        if (isNullOrWhiteSpace(s) || isNullOrWhiteSpace(c)) {
            return (-1);
        }

        int count = 0;
        int len = c.length();

        for (int i = 0; i < s.length(); i += len) {
            if ((i = s.indexOf(c, i)) != -1) {
                count++;
            } else {
                break;
            }
        }

        return (count);
    }

    /**
     * Checks whether the input String is a valid IP address (quad).
     *
     * @param s the String to be checked.
     * @return <code>true</code> if the String IS an IP address ; <code>false</code> if
     * it is not, or <code>s</code> is <code>null</code>.
     * @see java.net.InetAddress
     */
    public static boolean isIPAddress(String s) {
        if (isNullOrWhiteSpace(s) || s.indexOf('.') == -1) {
            return (false);
        }

        s = s.trim();

        int index, dotCount = 0, dotPos[] = new int[5];
        char ch;

        dotPos[0] = -1;
        dotPos[4] = s.length();

        for (index = 0; index < s.length() && dotCount < 4; index++) {
            ch = s.charAt(index);
            if (!Character.isDigit(ch) && ch != '.') {
                return (false);
            } else if (ch == '.') {
                dotPos[++dotCount] = index;
            }
        }

        if (dotCount != 3) {
            return (false);
        }

        try {
            String num;
            int number;

            for (dotCount = 1; dotCount < 5; dotCount++) {
                index = dotPos[dotCount - 1] + 1;
                num = s.substring(index, dotPos[dotCount]);
                number = Integer.valueOf(num).intValue();

                if (number < 0 || number > 255) {
                    return (false);
                }
            }
        } catch (Exception e) {
            return (false);
        }

        return (true);
    }

    /**
     * Converts the input Date into a String formatted as specified by RFC 1123 of the IETF.
     * The output String is in the format "Sun, 06 Nov 1994 08:49:37 GMT".
     *
     * @param d the Date to be converted.
     * @return the String representation of the date as specified by RFC1123 ; <code>null</code>
     * if input is <code>null</code>.
     * @see java.util.Date
     */
    public static String convertToHttpDate(Date d) {
        if (d == null) {
            return (null);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
        return (sdf.format(d));
    }

    /**
     * Parses the input date String into a Date object. The input date String is
     * expected to be in one of the following formats (note the spaces) :-<p>
     * <ul>
     * <li>"Sun, 06 Nov 1994 08:49:37 GMT"  ; RFC 822, updated by RFC 1123</li>
     * <li>"Sunday, 06-Nov-94 08:49:37 GMT"   ; RFC 850, obsoleted by RFC 1036</li>
     * <li>"Sunday, 06-Nov-1994 08:49:37 GMT" ; RFC 1036</li>
     * <li>"Sun Nov  6 08:49:37 1994"   ; ANSI C's asctime() format</li>
     * </ul>
     *
     * @param date the HTTP date String to be converted.
     * @return the parsed Date object ; <code>null</code> if
     * parsing was unsuccessful or input was <code>null</code>.
     */
    public static Date parseHttpDateStringToDate(String date) {
        if (date == null) {
            return (null);
        }

        StringTokenizer st = new StringTokenizer(date.trim(), " ");
        int iNumTokens = st.countTokens();
        String format = null;

        if (iNumTokens == 5)   // Its an asctime date ... wkday SP month SP ( 2DIGIT | ( SP 1DIGIT )) SP time SP 4DIGIT
        {       //             ; month day (e.g., Jun  2)
            // Sun Nov  6 08:49:37 1994
            format = "EEE MMM dd HH:mm:ss yyyy";
        } else if (iNumTokens == 4) // Its an RFC850 date ... weekday "," SP 2DIGIT "-" month "-" 2DIGIT SP time SP "GMT"
        {      //           ; day-month-year (e.g., 02-Jun-82)
            String dtok = null; // may have 2 or 4-digit year
            st.nextToken();
            dtok = st.nextToken().trim();
            if (dtok.length() == 9) // has 2-digit year
            {
                // Sunday, 06-Nov-94 08:49:37 GMT
                format = "EEEE, dd-MMM-yy HH:mm:ss zzz";
            } else if (dtok.length() == 11) // has 4-digit year
            {
                // Sunday, 06-Nov-1994 08:49:37 GMT
                format = "EEEE, dd-MMM-yyyy HH:mm:ss zzz";
            } else {
                return (null);
            }
        } else if (iNumTokens == 6) // Its an RFC1123 date ... wkday "," SP 2DIGIT SP month SP 4DIGIT SP time SP "GMT"
        {      //          ; day-month-year (e.g., 02 Jun 1982)
            //Sun, 06 Nov 1994 08:49:37 GMT
            format = "EEE, dd MMM yyyy HH:mm:ss zzz";
        } else {
            return (null);
        }

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
        Date d = sdf.parse(date, new ParsePosition(0));

        return (d);
    }

    /**
     * Returns true if argument is either <code>null</code> or it equals("").
     * It does not trim the input, unlike <code>isNullOrWhiteSpace(String)</code>.
     *
     * @param s the String to be tested.
     * @see #isNullOrWhiteSpace(String)
     */
    public static boolean isEmpty(String s) {
        return (s == null || s.equals(""));
    }

    /**
     * Replaces all occurrences of a String in the input String with another String.
     *
     * @param source      the String in which the replacements are to be carried out.
     * @param find        the String which must be searched for.
     * @param replace     the String which replaces the search String.
     * @param bIgnoreCase determines whether case-sensitive search is done.
     * @return the processed String if any replacements were made ; the original String if
     * search String was not found or search String is empty.
     * @throws IllegalArgumentException Thrown if source String is empty.
     */
    public static String replaceAll(String source, String find, String replace, boolean bIgnoreCase) throws IllegalArgumentException {
        if (isEmpty(source)) {
            throw new IllegalArgumentException("Empty source String");
        } else if (isEmpty(find)) {
            return (source);
        }

        if (replace == null) {
            replace = "";
        }

        StringBuffer sb = new StringBuffer(source);
        StringBuffer mod;
        boolean bDone = false;
        int prevIndex = 0, currIndex = 0, i = 0;

        if (bIgnoreCase) {
            source = source.toLowerCase();
            find = find.toLowerCase();
        }

        mod = new StringBuffer(source);

        while (!bDone) {
            if ((currIndex = mod.toString().indexOf(find, prevIndex)) != -1) {
                sb = sb.replace(currIndex, currIndex + find.length(), replace);
                mod = mod.replace(currIndex, currIndex + find.length(), replace);
                prevIndex = currIndex + replace.length();
            } else {
                bDone = true;
            }
        }

        return (sb.toString());
    }

    /**
     * Determines whether every double-quote has its closing partner in the input String.
     *
     * @param s the String to be tested.
     * @return <code>true</code> if quotes match or no quotes exist ; <code>false</code> otherwise.
     */
    public static boolean matchQuotes(String s) {
        int numQuotes = 0, numEscapedQuotes = 0;

        numQuotes = countInstances(s, '\"');
        numEscapedQuotes = countInstances(s, "\\\"");

        if (numQuotes == -1) {
            return (true);
        }

        if (numEscapedQuotes == -1) {
            numEscapedQuotes = 0;
        }

        return (((numQuotes - numEscapedQuotes) % 2) == 0);
    }

    /**
     * Removes the outermost double-quotes pair from the input String. Trims the input String
     * before processing.
     *
     * @param s the String to be stripped.
     * @return the stripped String ; the original String if it is not quoted or quotes don't match.
     */
    public static String stripQuotes(String s) {
        if (isNullOrWhiteSpace(s) || !matchQuotes(s)) {
            return (s);
        }

        String s2 = trimWhitespace(s);

        if (isQuoted(s2)) {
            return (s2.substring(1, s2.length() - 1));
        }

        return (s);
    }

    /**
     * Determines whether the input String is enclosed in double-quotes. Trims the input String
     * first.
     *
     * @param s the String to be tested.
     * @return <code>true</code> if input String is quoted ;
     * <code>false</code> if it isn't or it is empty.
     * @see #trimWhitespace(String)
     */
    public static boolean isQuoted(String s) {
        if (isNullOrWhiteSpace(s) || !matchQuotes(s)) {
            return (false);
        }

        String s2 = trimWhitespace(s);

        return (s2.charAt(0) == '\"' && s.charAt(s2.length() - 1) == '\"');
    }

    /**
     * Converts the input integer into a more readable comma-separated String. A
     * comma is inserted after every three digits, starting from the unit's place. For
     * example, <p><pre>
     *  12345     is converted to   12,345
     *  987654321   is converted to   987,654,321</pre>
     *
     * @param i the integer to be converted.
     * @return the String representation of the formatted integer ; if the input
     * lies between -1000 & 1000 (not inclusive) , the returned String is
     * the same as <code>String.valueOf(int)</code>
     * @see java.lang.String#valueOf(int)
     */
    public static String commaFormatInteger(int i) {
        boolean bNegative = (i < 0);

        if (i == 0 || (i > 0 && i < 1000) || (i < 0 && i > -1000)) {
            return (String.valueOf(i));
        }

        i = Math.abs(i);

        StringBuffer sb = new StringBuffer(String.valueOf(i));
        sb.reverse();

        int l = sb.length();
        int iter = l / 3;

        if (l % 3 == 0) {
            iter--;
        }

        for (int c = 1; c <= iter; c++) {
            l = 3 * c;
            sb.insert(l + c - 1, ',');
        }

        if (bNegative) {
            sb.append('-');
        }

        return (sb.reverse().toString());
    }

    /**
     * Returns the number which would be in the n<sup>th</sup> place if the input array were sorted
     * in descending order. For arrays with distinct elements, this returns the n<sup>th</sup> largest
     * number. For example, in the array {1,2,3,4,5,6}, <code>findOrderedMax(list, 3)</code> would return 4.
     * However, the result is quite different in arrays with repeated elements.
     * For example, in the array {1,2,6,2}, both <code>findOrderedMax(list,2)</code>
     * and <code>findOrderedMax(list,3)</code> return 2.
     *
     * @param inputlist the array of numbers.
     * @param n         the desired position of sorted array. For example, 1 indicates first from top of sorted array; 2 indicates second from top, and so on.
     * @throws IllegalArgumentException Thrown when the number of elements in the input array is less than
     *                                  the desired order, that is, when <code>n</code> exceeds <code>inputlist.length</code>.
     *                                  Or, when the order <code>n</code> is less than or equal to 0.
     * @see #findMax(int[], int)
     */
    public static int findOrderedMax(int inputlist[], int n) {
        if (inputlist.length < n) {
            throw new IllegalArgumentException("Input array should have atleast input order numbers.");
        }

        if (n <= 0) {
            throw new IllegalArgumentException("Order cannot be less than or equal to zero.");
        }

        int numIters, i;
        int maxIndex, temp;
        int list[] = new int[inputlist.length];

        System.arraycopy(inputlist, 0, list, 0, list.length);

        for (numIters = 0; numIters < n; numIters++) {
            maxIndex = 0;

            for (i = 0; i < (list.length - numIters); i++) {
                if (list[i] > list[maxIndex]) {
                    maxIndex = i;
                }
            }

            temp = list[i - 1];
            list[i - 1] = list[maxIndex];
            list[maxIndex] = temp;
        }

        return (list[list.length - n]);
    }

    /**
     * Returns the n<sup>th</sup> largest number in the input array. This method disregards
     * duplicate elements unlike the <code>findOrderedMax</code> method. For example, in the array {1,2,3,4,5,6},
     * <code>findMax(list, 3)</code> would return 4. In the array {1,2,6,2}, <code>findMax(list,2)</code> would return 2, and
     * and <code>findMax(list,3)</code> would return 1. <p> Note that in an array with repeating elements, it is possible that
     * the required order may not exist. For example, in the array {1,2,2}, <code>findMax(list,3)</code>, there is no third-largest
     * number. In such cases, this method returns the number with highest order less than or equal to <code>n</code>. In other words,
     * in the above example, this method would return 1, which is the same value as would be returned by <code>findMax(list,2)</code>.
     *
     * @param inputlist the array of numbers.
     * @param n         the desired order. For example, 1 indicates largest number; 2 indicates second-largest number, and so on.
     * @throws IllegalArgumentException Thrown when the number of elements in the input array is less than
     *                                  the desired order, that is, when <code>n</code> exceeds <code>inputlist.length</code>.
     *                                  Or, when the order <code>n</code> is less than or equal to 0.
     * @see #findOrderedMax(int[], int)
     */
    public static int findMax(int inputlist[], int n) {
        if (inputlist.length < n) {
            throw new IllegalArgumentException("Input array should have atleast input order numbers.");
        }

        if (n <= 0) {
            throw new IllegalArgumentException("Order cannot be less than or equal to zero.");
        }

        int numIters, i;
        int maxIndex, temp;
        int order[] = new int[n];
        int oindex = 0;
        int currIter = 0;
        int list[] = new int[inputlist.length];

        System.arraycopy(inputlist, 0, list, 0, list.length);

        for (numIters = 0; numIters < list.length; numIters++) {
            maxIndex = 0;

            for (i = 0; i < (list.length - numIters); i++) {
                if (list[i] > list[maxIndex]) {
                    maxIndex = i;
                }
            }

            temp = list[maxIndex];
            list[maxIndex] = list[i - 1];
            list[i - 1] = temp;

            if (oindex == 0 || order[oindex - 1] != temp) {
                order[oindex++] = temp;
                currIter++;
            }
            if (currIter == n) {
                break;
            }
        }

        return (order[oindex - 1]);
    }

    /**
     * Returns the number which would be in the n<sup>th</sup> place if the input array were sorted
     * in ascending order. For arrays with distinct elements, this returns the n<sup>th</sup> smallest
     * number. For example, in the array {1,2,3,4,5,6}, <code>findOrderedMin(list, 3)</code> would return 3.
     * However, the result is quite different in arrays with repeated elements.
     * For example, in the array {1,2,6,2}, both <code>findOrderedMin(list,2)</code>
     * and <code>findOrderedMin(list,3)</code> return 2.
     *
     * @param inputlist the array of numbers.
     * @param n         the desired position of sorted array. For example, 1 indicates first from top of sorted array; 2 indicates second from top, and so on.
     * @throws IllegalArgumentException Thrown when the number of elements in the input array is less than
     *                                  the desired order, that is, when <code>n</code> exceeds <code>inputlist.length</code>.
     *                                  Or, when the order <code>n</code> is less than or equal to 0.
     * @see #findMin(int[], int)
     */
    public static int findOrderedMin(int inputlist[], int n) {
        if (inputlist.length < n) {
            throw new IllegalArgumentException("Input array should have atleast input order numbers.");
        }

        if (n <= 0) {
            throw new IllegalArgumentException("Order cannot be less than or equal to zero.");
        }

        int numIters, i;
        int minIndex, temp;
        int list[] = new int[inputlist.length];

        System.arraycopy(inputlist, 0, list, 0, list.length);

        for (numIters = 0; numIters < n; numIters++) {
            minIndex = 0;

            for (i = 0; i < (list.length - numIters); i++) {
                if (list[i] < list[minIndex]) {
                    minIndex = i;
                }
            }

            temp = list[i - 1];
            list[i - 1] = list[minIndex];
            list[minIndex] = temp;
        }

        return (list[list.length - n]);
    }

    /**
     * Returns the n<sup>th</sup> smallest number in the input array. This method disregards
     * duplicate elements unlike the <code>findOrderedMin</code> method. For example, in the array {1,2,3,4,5,6},
     * <code>findMin(list, 3)</code> would return 3. In the array {1,2,6,2}, <code>findMin(list,2)</code> would return 2, and
     * and <code>findMin(list,3)</code> would return 6. <p> Note that in an array with repeating elements, it is possible that
     * the required order may not exist. For example, in the array {1,2,2}, <code>findMin(list,3)</code>, there is no third-smallest
     * number. In such cases, this method returns the number with highest order less than or equal to <code>n</code>. In other words,
     * in the above example, this method would return 2, which is the same value as would be returned by <code>findMax(list,2)</code>.
     *
     * @param inputlist the array of numbers.
     * @param n         the desired order. For example, 1 indicates smallest number; 2 indicates second-smallest number, and so on.
     * @throws IllegalArgumentException Thrown when the number of elements in the input array is less than
     *                                  the desired order, that is, when <code>n</code> exceeds <code>inputlist.length</code>.
     *                                  Or, when the order <code>n</code> is less than or equal to 0.
     * @see #findOrderedMin(int[], int)
     */
    public static int findMin(int inputlist[], int n) {
        if (inputlist.length < n) {
            throw new IllegalArgumentException("Input array should have atleast input order numbers.");
        }

        if (n <= 0) {
            throw new IllegalArgumentException("Order cannot be less than or equal to zero.");
        }

        int numIters, i;
        int minIndex, temp;
        int order[] = new int[n];
        int oindex = 0;
        int currIter = 0;
        int list[] = new int[inputlist.length];

        System.arraycopy(inputlist, 0, list, 0, list.length);

        for (numIters = 0; numIters < list.length; numIters++) {
            minIndex = 0;

            for (i = 0; i < (list.length - numIters); i++) {
                if (list[i] < list[minIndex]) {
                    minIndex = i;
                }
            }

            temp = list[minIndex];
            list[minIndex] = list[i - 1];
            list[i - 1] = temp;

            if (oindex == 0 || order[oindex - 1] != temp) {
                order[oindex++] = temp;
                currIter++;
            }

            if (currIter == n) {
                break;
            }
        }

        return (order[oindex - 1]);
    }

    /**
     * Returns the object which would be in the n<sup>th</sup> place if the input array were sorted
     * in descending order. This method allows comparison of <code>Object</code>s. It follows similar
     * semantics as the <code>findOrderedMax(int[], int)</code> method.
     *
     * @param inputlist the array of <code>Comparable</code> objects. All elements must be <i>mutually Comparable</i> (that is, e1.compareTo(e2) must not throw a ClassCastException for any elements e1 and e2 in the array).
     * @param n         the desired position of sorted array. For example, 1 indicates first from top of sorted array; 2 indicates second from top, and so on.
     * @throws IllegalArgumentException Thrown when the number of elements in the input array is less than
     *                                  the desired order, that is, when <code>n</code> exceeds <code>inputlist.length</code>.
     *                                  Or, when the order <code>n</code> is less than or equal to 0.
     * @see #findMax(Comparable[], int)
     * @see #findOrderedMax(int[], int)
     */
    public static Object findOrderedMax(Comparable inputlist[], int n) {
        if (inputlist.length < n) {
            throw new IllegalArgumentException("Input array should have atleast input order numbers.");
        }

        if (n <= 0) {
            throw new IllegalArgumentException("Order cannot be less than or equal to zero.");
        }

        int numIters, i;
        int maxIndex;
        Comparable temp;
        Comparable list[] = new Comparable[inputlist.length];

        System.arraycopy(inputlist, 0, list, 0, list.length);

        for (numIters = 0; numIters < n; numIters++) {
            maxIndex = 0;

            for (i = 0; i < (list.length - numIters); i++) {
                if (list[i].compareTo(list[maxIndex]) > 0) {
                    maxIndex = i;
                }
            }

            temp = list[i - 1];
            list[i - 1] = list[maxIndex];
            list[maxIndex] = temp;
        }

        return (list[list.length - n]);
    }

    /**
     * Returns the n<sup>th</sup> largest object in the input array. This method allows comparison of <code>Object</code>s. It follows similar
     * semantics as the <code>findMax(int[], int)</code> method.
     *
     * @param inputlist the array of <code>Comparable</code> objects. All elements must be <i>mutually Comparable</i> (that is, e1.compareTo(e2) must not throw a ClassCastException for any elements e1 and e2 in the array).
     * @param n         the desired order. For example, 1 indicates largest object; 2 indicates second-largest object, and so on.
     * @throws IllegalArgumentException Thrown when the number of elements in the input array is less than
     *                                  the desired order, that is, when <code>n</code> exceeds <code>inputlist.length</code>.
     *                                  Or, when the order <code>n</code> is less than or equal to 0.
     * @see #findOrderedMax(Comparable[], int)
     * @see #findMax(int[], int)
     */
    public static Object findMax(Comparable inputlist[], int n) {
        if (inputlist.length < n) {
            throw new IllegalArgumentException("Input array should have atleast input order numbers.");
        }

        if (n <= 0) {
            throw new IllegalArgumentException("Order cannot be less than or equal to zero.");
        }

        int numIters, i;
        int maxIndex;
        Comparable temp;
        Comparable order[] = new Comparable[n];
        int oindex = 0;
        int currIter = 0;
        Comparable list[] = new Comparable[inputlist.length];

        System.arraycopy(inputlist, 0, list, 0, list.length);

        for (numIters = 0; numIters < list.length; numIters++) {
            maxIndex = 0;

            for (i = 0; i < (list.length - numIters); i++) {
                if (list[i].compareTo(list[maxIndex]) > 0) {
                    maxIndex = i;
                }
            }

            temp = list[maxIndex];
            list[maxIndex] = list[i - 1];
            list[i - 1] = temp;

            if (oindex == 0 || !order[oindex - 1].equals(temp)) {
                order[oindex++] = temp;
                currIter++;
            }

            if (currIter == n) {
                break;
            }
        }

        return (order[oindex - 1]);
    }

    /**
     * Returns the object which would be in the n<sup>th</sup> place if the input array were sorted
     * in ascending order. This method allows comparison of <code>Object</code>s. It follows similar
     * semantics as the <code>findOrderedMin(int[], int)</code> method.
     *
     * @param inputlist the array of <code>Comparable</code> objects. All elements must be <i>mutually Comparable</i> (that is, e1.compareTo(e2) must not throw a ClassCastException for any elements e1 and e2 in the array).
     * @param n         the desired position of sorted array. For example, 1 indicates first from top of sorted array; 2 indicates second from top, and so on.
     * @throws IllegalArgumentException Thrown when the number of elements in the input array is less than
     *                                  the desired order, that is, when <code>n</code> exceeds <code>inputlist.length</code>.
     *                                  Or, when the order <code>n</code> is less than or equal to 0.
     * @see #findMin(Comparable[], int)
     * @see #findOrderedMin(int[], int)
     */
    public static Object findOrderedMin(Comparable inputlist[], int n) {
        if (inputlist.length < n) {
            throw new IllegalArgumentException("Input array should have atleast input order numbers.");
        }

        if (n <= 0) {
            throw new IllegalArgumentException("Order cannot be less than or equal to zero.");
        }

        int numIters, i;
        int minIndex;
        Comparable temp;
        Comparable list[] = new Comparable[inputlist.length];

        System.arraycopy(inputlist, 0, list, 0, list.length);

        for (numIters = 0; numIters < n; numIters++) {
            minIndex = 0;

            for (i = 0; i < (list.length - numIters); i++) {
                if (list[i].compareTo(list[minIndex]) < 0) {
                    minIndex = i;
                }
            }

            temp = list[i - 1];
            list[i - 1] = list[minIndex];
            list[minIndex] = temp;
        }

        return (list[list.length - n]);
    }

    /**
     * Returns the n<sup>th</sup> smallest object in the input array. This method allows comparison of <code>Object</code>s. It follows similar
     * semantics as the <code>findMin(int[], int)</code> method.
     *
     * @param inputlist the array of <code>Comparable</code> objects. All elements must be <i>mutually Comparable</i> (that is, e1.compareTo(e2) must not throw a ClassCastException for any elements e1 and e2 in the array).
     * @param n         the desired order. For example, 1 indicates smallest object; 2 indicates second-smallest object, and so on.
     * @throws IllegalArgumentException Thrown when the number of elements in the input array is less than
     *                                  the desired order, that is, when <code>n</code> exceeds <code>inputlist.length</code>.
     *                                  Or, when the order <code>n</code> is less than or equal to 0.
     * @see #findOrderedMin(Comparable[], int)
     * @see #findMin(int[], int)
     */
    public static Object findMin(Comparable inputlist[], int n) {
        if (inputlist.length < n) {
            throw new IllegalArgumentException("Input array should have atleast input order numbers.");
        }

        if (n <= 0) {
            throw new IllegalArgumentException("Order cannot be less than or equal to zero.");
        }

        int numIters, i;
        int minIndex;
        Comparable temp;
        Comparable order[] = new Comparable[n];
        int oindex = 0;
        int currIter = 0;
        Comparable list[] = new Comparable[inputlist.length];

        System.arraycopy(inputlist, 0, list, 0, list.length);

        for (numIters = 0; numIters < list.length; numIters++) {
            minIndex = 0;

            for (i = 0; i < (list.length - numIters); i++) {
                if (list[i].compareTo(list[minIndex]) < 0) {
                    minIndex = i;
                }
            }

            temp = list[minIndex];
            list[minIndex] = list[i - 1];
            list[i - 1] = temp;

            if (oindex == 0 || !order[oindex - 1].equals(temp)) {
                order[oindex++] = temp;
                currIter++;
            }

            if (currIter == n) {
                break;
            }
        }

        return (order[oindex - 1]);
    }

    /**
     * Returns the object which would be in the n<sup>th</sup> place if the input array were sorted
     * in descending order. This method allows comparison of <code>Object</code>s. The comparison logic
     * is provided by the <code>Comparator</code> argument. It follows similar
     * semantics as the <code>findOrderedMax(int[], int)</code> method.
     *
     * @param c         the <code>Comparator</code> that provides the ordering logic.
     * @param inputlist the array of <code>Object</code>s.
     * @param n         the desired position of sorted array. For example, 1 indicates first from top of sorted array; 2 indicates second from top, and so on.
     * @throws IllegalArgumentException Thrown when the number of elements in the input array is less than
     *                                  the desired order, that is, when <code>n</code> exceeds <code>inputlist.length</code>.
     *                                  Or, when the order <code>n</code> is less than or equal to 0.
     * @see #findMax(Object[], int, Comparator)
     * @see #findOrderedMax(int[], int)
     */
    public static Object findOrderedMax(Object inputlist[], int n, Comparator c) {
        if (inputlist.length < n) {
            throw new IllegalArgumentException("Input array should have atleast input order numbers.");
        }

        if (n <= 0) {
            throw new IllegalArgumentException("Order cannot be less than or equal to zero.");
        }

        int numIters, i;
        int maxIndex;
        Object temp;
        Object list[] = new Object[inputlist.length];

        System.arraycopy(inputlist, 0, list, 0, list.length);

        for (numIters = 0; numIters < n; numIters++) {
            maxIndex = 0;

            for (i = 0; i < (list.length - numIters); i++) {
                if (c.compare(list[i], list[maxIndex]) > 0) {
                    maxIndex = i;
                }
            }

            temp = list[i - 1];
            list[i - 1] = list[maxIndex];
            list[maxIndex] = temp;
        }

        return (list[list.length - n]);
    }

    /**
     * Returns the n<sup>th</sup> largest object in the input array. This method allows comparison of <code>Object</code>s.
     * The comparison logic is provided by the <code>Comparator</code> argument. It follows similar
     * semantics as the <code>findMax(int[], int)</code> method.
     *
     * @param c         the <code>Comparator</code> that provides the ordering logic.
     * @param inputlist the array of <code>Object</code>s.
     * @param n         the desired order. For example, 1 indicates largest object; 2 indicates second-largest object, and so on.
     * @throws IllegalArgumentException Thrown when the number of elements in the input array is less than
     *                                  the desired order, that is, when <code>n</code> exceeds <code>inputlist.length</code>.
     *                                  Or, when the order <code>n</code> is less than or equal to 0.
     * @see #findOrderedMax(Object[], int, Comparator)
     * @see #findMax(int[], int)
     */
    public static Object findMax(Object inputlist[], int n, Comparator c) {
        if (inputlist.length < n) {
            throw new IllegalArgumentException("Input array should have atleast input order numbers.");
        }

        if (n <= 0) {
            throw new IllegalArgumentException("Order cannot be less than or equal to zero.");
        }

        int numIters, i;
        int maxIndex;
        Object temp;
        Object order[] = new Object[n];
        int oindex = 0;
        int currIter = 0;
        Object list[] = new Object[inputlist.length];

        System.arraycopy(inputlist, 0, list, 0, list.length);

        for (numIters = 0; numIters < list.length; numIters++) {
            maxIndex = 0;

            for (i = 0; i < (list.length - numIters); i++) {
                if (c.compare(list[i], list[maxIndex]) > 0) {
                    maxIndex = i;
                }
            }

            temp = list[maxIndex];
            list[maxIndex] = list[i - 1];
            list[i - 1] = temp;

            if (oindex == 0 || !(c.compare(order[oindex - 1], temp) == 0)) {
                order[oindex++] = temp;
                currIter++;
            }

            if (currIter == n) {
                break;
            }
        }

        return (order[oindex - 1]);
    }

    /**
     * Returns the object which would be in the n<sup>th</sup> place if the input array were sorted
     * in ascending order. This method allows comparison of <code>Object</code>s. The comparison logic
     * is provided by the <code>Comparator</code> argument. It follows similar
     * semantics as the <code>findOrderedMin(int[], int)</code> method.
     *
     * @param c         the <code>Comparator</code> that provides the ordering logic.
     * @param inputlist the array of <code>Object</code>s.
     * @param n         the desired position of sorted array. For example, 1 indicates first from top of sorted array; 2 indicates second from top, and so on.
     * @throws IllegalArgumentException Thrown when the number of elements in the input array is less than
     *                                  the desired order, that is, when <code>n</code> exceeds <code>inputlist.length</code>.
     *                                  Or, when the order <code>n</code> is less than or equal to 0.
     * @see #findMin(Object[], int, Comparator)
     * @see #findOrderedMin(int[], int)
     */
    public static Object findOrderedMin(Object inputlist[], int n, Comparator c) {
        if (inputlist.length < n) {
            throw new IllegalArgumentException("Input array should have atleast input order numbers.");
        }

        if (n <= 0) {
            throw new IllegalArgumentException("Order cannot be less than or equal to zero.");
        }

        int numIters, i;
        int minIndex;
        Object temp;
        Object list[] = new Object[inputlist.length];

        System.arraycopy(inputlist, 0, list, 0, list.length);

        for (numIters = 0; numIters < n; numIters++) {
            minIndex = 0;

            for (i = 0; i < (list.length - numIters); i++) {
                if (c.compare(list[i], list[minIndex]) < 0) {
                    minIndex = i;
                }
            }

            temp = list[i - 1];
            list[i - 1] = list[minIndex];
            list[minIndex] = temp;
        }

        return (list[list.length - n]);
    }

    /**
     * Returns the n<sup>th</sup> smallest object in the input array. This method allows comparison of <code>Object</code>s.
     * The comparison logic is provided by the <code>Comparator</code> argument. It follows similar
     * semantics as the <code>findMin(int[], int)</code> method.
     *
     * @param c         the <code>Comparator</code> that provides the ordering logic.
     * @param inputlist the array of <code>Objects</code>.
     * @param n         the desired order. For example, 1 indicates smallest object; 2 indicates second-smallest object, and so on.
     * @throws IllegalArgumentException Thrown when the number of elements in the input array is less than
     *                                  the desired order, that is, when <code>n</code> exceeds <code>inputlist.length</code>.
     *                                  Or, when the order <code>n</code> is less than or equal to 0.
     * @see #findOrderedMin(Object[], int, Comparator)
     * @see #findMin(int[], int)
     */
    public static Object findMin(Object inputlist[], int n, Comparator c) {
        if (inputlist.length < n) {
            throw new IllegalArgumentException("Input array should have atleast input order numbers.");
        }

        if (n <= 0) {
            throw new IllegalArgumentException("Order cannot be less than or equal to zero.");
        }

        int numIters, i;
        int minIndex;
        Object temp;
        Object order[] = new Object[n];
        int oindex = 0;
        int currIter = 0;
        Object list[] = new Object[inputlist.length];

        System.arraycopy(inputlist, 0, list, 0, list.length);

        for (numIters = 0; numIters < list.length; numIters++) {
            minIndex = 0;

            for (i = 0; i < (list.length - numIters); i++) {
                if (c.compare(list[i], list[minIndex]) < 0) {
                    minIndex = i;
                }
            }

            temp = list[minIndex];
            list[minIndex] = list[i - 1];
            list[i - 1] = temp;

            if (oindex == 0 || !(c.compare(order[oindex - 1], temp) == 0)) {
                order[oindex++] = temp;
                currIter++;
            }

            if (currIter == n) {
                break;
            }
        }

        return (order[oindex - 1]);
    }

    public static String wrap(String str, int width) {
        //output
        StringBuffer sbuf = new StringBuffer();
        //temporary word buffer
        StringBuffer word = new StringBuffer();

        int linelen = 0;

        //loop string
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            boolean isSpace = Character.isWhitespace(c);
            linelen++;

            //reset on hardwrap, adding wordbuffer
            if (c == '\n') {
                linelen = 0;
                sbuf.append(word.toString());
                word.delete(0, word.length());
                continue;
            }

            if (linelen > width) {
                //check if character is a space
                if (isSpace) {
                    //append word and wrap output
                    sbuf.append(word.toString());
                    word.delete(0, word.length());
                    sbuf.append('\n');
                    //FIXME: swallows a space
                    linelen = 0;
                    continue;
                } else {
                    sbuf.append('\n');
                    word.append(c);
                    linelen = 0;
                    continue;
                }
            } else {
                //check if space
                if (isSpace) {
                    //add word
                    sbuf.append(word.toString());
                    word.delete(0, word.length());
                    sbuf.append(c);
                } else {
                    word.append(c);
                }
            }
        }
        return sbuf.toString();
    }

    public static long crc(String s) {
        CRC32 checksum = new CRC32();
        checksum.update(s.getBytes());
        return checksum.getValue();
    }

    public static long adler(String s) {
        Adler32 checksum = new Adler32();
        checksum.update(s.getBytes());
        return checksum.getValue();
    }

    public static InputStream wrap(InputStream in, int width) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(in.available());

        boolean done = false;
        int c = -1;
        while (!done) {
            c = in.read();
            if (c == -1) {
                done = true;
                continue;
            } else {
                bout.write(c);
            }
        }
        return new ByteArrayInputStream(
                wrap(bout.toString(), width).getBytes()
        );

    }
}


