package com.ivangr.tennispartner.models;

public class Game {

    private long id;
    private String place;
    private long time;
    private long duration;
    private String score;

    public Game() {
    }

    public Game(long id, String place, long time, long duration, String score) {
        this.id = id;
        this.place = place;
        this.time = time;
        this.duration = duration;
        this.score = score;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}
