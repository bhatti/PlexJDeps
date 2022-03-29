package com.plexobject.deps;
import java.util.*;

public class BeanInfo {
    final String id;
    final String className;
    BeanInfo parent;
    final List<BeanInfo> children = new ArrayList<>();
    public BeanInfo(String id, String className) {
        this.id = id;
        this.className = className;
    }
    public void addChild(BeanInfo info) {
        if (this == info) {
            return;
        }
        if (!children.contains(info)) {
            children.add(info);
        }
        info.parent = this;
    }
    @Override 
    public int hashCode() {
        return id.hashCode();
    }
    @Override 
    public boolean equals(Object obj) {
        if (obj instanceof BeanInfo) {
            BeanInfo other = (BeanInfo) obj;
            return id.equals(other.id);
        }
        return false;
    }

    @Override 
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb, 0);
        return sb.toString();
    }
    //
    private void toString(StringBuilder sb, int indent) {
        for (int i=0; i<indent; i++) {
            sb.append(" ");
        }
        sb.append(id + ":" + className + "\n");
        for (BeanInfo info : children) {
            info.toString(sb, indent+2);
        }
    }
}
