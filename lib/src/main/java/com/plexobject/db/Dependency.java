package com.plexobject.db;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import java.io.Serializable;


@Entity
public class Dependency implements Serializable {
    private static final long serialVersionUID = 1L;
    @PrimaryKey
    public String id;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    public String from;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    public String to;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    public String shortFrom;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    public String shortTo;

    public boolean springDI;

    public Dependency() {
    }

    public Dependency(String from, String to) {
        this.id = from + ":" + to;
        this.from = from;
        this.to = to;
        shortFrom = getClassName(from);
        shortTo = getClassName(to);
    }

    static String getClassName(String str) {
        String[] t = str.split("\\.");
        return t[t.length - 1];
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getShortFrom() {
        return shortFrom;
    }

    public void setShortFrom(String shortFrom) {
        this.shortFrom = shortFrom;
    }

    public String getShortTo() {
        return shortTo;
    }

    public void setShortTo(String shortTo) {
        this.shortTo = shortTo;
    }

    public boolean isSpringDI() {
        return springDI;
    }

    public void setSpringDI(boolean springDI) {
        this.springDI = springDI;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Dependency other = (Dependency) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Dependency [from=" + from + ", to=" + to + ", springDI=" + springDI + "]";
    }
}

