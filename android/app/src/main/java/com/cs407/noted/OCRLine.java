package com.cs407.noted;

public class OCRLine implements Comparable<OCRLine> {
    public String text;
    public int y;

    public OCRLine(String text, int y) {
        this.text = text;
        this.y = y;
    }

    @Override
    public int compareTo(OCRLine other) {
        if(this.y > other.y)
            return 1;
        else if(this.y < other.y)
            return -1;
        else
            return 0;
    }
}
