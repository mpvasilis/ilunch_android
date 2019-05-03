package com.vasilis.ilunch;

public class ratingsData {
    private String rating;
    private String date;

    public ratingsData(String rating, String date) {
        this.rating = rating;
        this.date = date;
    }

    public ratingsData() {
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
