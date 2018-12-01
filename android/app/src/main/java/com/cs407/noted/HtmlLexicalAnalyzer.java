package com.cs407.noted;

public class HtmlLexicalAnalyzer {
    private String html;

    private String bold = "<b>";
    private String boldEnd = "</b>";

    private String italic = "<i>";
    private String italicEnd = "</i>";

    private String underline = "<u>";
    private String underlineEnd = "</u>";

    private String strikethrough1 = "<s>";
    private String strikethrough1End = "</s>";

    private String strikethrough2 = "<del>";
    private String strikethrough2End = "</del>";

    private String strikethrough3 = "<strike>";
    private String strikethrough3End = "</strike>";

    private String unorderedList = "<ul>";
    private String unorderedListEnd = "</ul>";

    private String list = "<li>";
    private String listEnd = "</li>";

    private String quote = "<blockquote>";
    private String quoteEnd = "</blockquote>";

    private String br = "<br>";
    private String brEnd = "</br>";

    private String nbsp = "&nbsp;";

    private String link = "<a href=";  // go until '>'
    private String linkEnd = "</a>";

    private String image = "<img src";  // go until '>', no end




    private static String[] tokens = {

            /* strikethrough */ "<del>", "</del>",  "</s>", "<strike>", "</strike>",
            /* bullet */  "</ul>", "</li>",
            /* quote */ "<blockquote>", "</blockquote>",
            /* break */ "<br>", "</br>",
            /* space end of line */ "&nbsp;"
    };
    private static String[] sizeThreeTokens = {
            /* bold */ "<b>",
            /* italic */ "<i>",
            /* underline */ "<u>",
            /* strikethrough */ "<s>"
    };
    private static String[] sizeFourTokens = {
            /* bold */  "</b>",
            /* italic */ "</i>",
            /* underline */ "</u>",
            /* break */ "<br>",
            /* bullet */ "<ul> ", "<li>",
            /* strikethrough */ "</s>"
    };

    private static String[] specialTokens = {
            /* link */ "<a", "</a>",
            /* image */ "<img", "/>"
    };


    public HtmlLexicalAnalyzer(String html) {
        this.html = html;
    }

    public int convertPlainTextPositionToHtmlPosition(int pos) {
        // go through html, incrementing pos unless we encounter a token
        int i, curr = 0;
        for (i = 0; i < html.length() && curr < pos;) {
            char c = html.charAt(i);
            // see if c matches any of the tokens
            if (c == '<' || c == '&') {
                // look ahead to see if we get a match
                int skipSize = skipSize(i);
                if (skipSize == 0) {
                    // we didn't actually encounter an html element
                    i++;
                    if (curr == 0) {
                        curr++;
//                        if (pos == 1) break;
//                        curr++;
                    } else {
                        curr++;
                    }
                } else {
                    // if there's a newline, add a character
                    if (i + skipSize < html.length() && html.substring(i, i + skipSize).equals(br)) {
                        curr++;
                    }
                    // increment by 1 if we encounter an image
                    if (i + image.length() < html.length() && html.substring(i, i + image.length()).equals(image)) {
                        curr++;
                        i++;
                    }
                    i += skipSize; // subtract 1 since we increment i after this anyways
                }
            } else {
                i++;
                // get on the other side of the first letter
//                if (curr == 0) {
//                    curr++;
//                    // handle edge case where pos is after first letter
//                    if (pos == 1) {
//                        break;
//                    }
//                }
                curr++;
            }
        }
        return i;
    }

    private int skipSize(int i) {
        int limit = html.length() - i;

        if (limit >= 3) {
            String three = html.substring(i, i + 3);
            if (three.equals(bold) || three.equals(italic) || three.equals(underline)
                    || three.equals(strikethrough1)) {
                return 3;
            }
        }
        if (limit >= 4) {
            String four = html.substring(i, i + 4);
            if (four.equals(boldEnd) || four.equals(underlineEnd) || four.equals(strikethrough1End)
                    || four.equals(italicEnd) || four.equals(unorderedList) || four.equals(list)
                    || four.equals(br) || four.equals(linkEnd)) {
                return 4;
            }
        }
        if (limit >= 5) {
            String five = html.substring(i, i + 5);
            if (five.equals(strikethrough2) || five.equals(unorderedListEnd) || five.equals(listEnd)
                    || five.equals(brEnd)) {
                return 5;
            }
        }
        if (limit >=6) {
            String six = html.substring(i, i + 6);
            if (six.equals(nbsp) || six.equals(strikethrough2End)) {
                return 6;
            }
        }
        if (limit >= 8) {
            String eight = html.substring(i, i + 8);
            if (eight.equals(link) || eight.equals(image)) {
                return findEndOfElement(i);
            } else if (eight.equals(strikethrough3)) {
                return 8;
            }
        }
        if (limit >=9) {
            String nine = html.substring(i, i + 9);
            if (nine.equals(strikethrough3End)) {
                return 9;
            }
        }
        if (limit >= 12) {
            String twelve = html.substring(i, i + 12);
            if (twelve.equals(quote)) {
                return 12;
            }
        }
        if (limit >= 13) {
            String thirteen = html.substring(i, i + 13);
            if (thirteen.equals(quoteEnd)) {
                return 13;
            }
        }
        // we haven't encountered an actual html element
        return 0;

    }

    private int findEndOfElement(int pos) {
        for (int i = pos; i < html.length(); i++) {
            if (html.charAt(i) == '>') {
                // we found the end
                return i - pos;
            }
        }
        return html.length() - pos;
    }

    public static void main(String[] args) {
        String html1 = "\"<p>its<img src=\\\"https://firebasestorage.googleapis.com/v0/b/noted-a0a2a.appspot.com/o/uploadedImages%2F-LSCbFnlcSGdARSRfRjj%2Flight?alt=media&amp;token=a5bf7bcc-4562-4eb9-8538-f2f0b3a03744\\\" width=\\\"201\\\" style=\\\"\\\">me dude</p>\"";
        HtmlLexicalAnalyzer analyzer = new HtmlLexicalAnalyzer(html1);

    }
}



