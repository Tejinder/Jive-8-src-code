package com.jivesoftware.community.browse.rest.impl;

import com.jivesoftware.community.rating.Rating;
import com.jivesoftware.community.rating.rest.WSRating;

import java.util.List;

public class ItemRatingBean {

    private double meanRating;
    private int availableRatingCount;

    public double getMeanRating() {
        return meanRating;
    }

    public void setMeanRating(double meanRating) {
        this.meanRating = meanRating;
    }

    public int getAvailableRatingCount() {
        return availableRatingCount;
    }

    public void setAvailableRatingCount(int availableRatingCount) {
        this.availableRatingCount = availableRatingCount;
    }
}
