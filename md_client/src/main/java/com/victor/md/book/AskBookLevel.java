package com.victor.md.book;

public class AskBookLevel extends BookLevel {

    @Override
    public int compareTo(BookLevel rhs) {
        if (rhs instanceof AskBookLevel) {
            if (this.price() > rhs.price())
                return 1;
            else if (this.price() < rhs.price())
                return -1;
            else
                return 0;
        } else {
            throw new IllegalArgumentException("Compare type is not AskBookLevel");
        }
    }

    @Override
    public int compareTo(long price) {
        if (this.price() > price)
            return 1;
        else if (this.price() < price)
            return -1;
        else return 0;
    }

    public int compareTo(AskBookLevel rhs) {
        if (this.price() > rhs.price())
            return 1;
        else if (this.price() < rhs.price())
            return -1;
        else
            return 0;
    }
}
