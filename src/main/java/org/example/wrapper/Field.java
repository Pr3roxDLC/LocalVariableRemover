package org.example.wrapper;

public class Field {
    public int access = 0;
    public String name = "";
    public String descriptor = "";
    public String signature = "";
    public Object value = null;
    public Field(int access, String name, String descriptor, String signature, Object value){
    this.access = access;
    this.name = name;
    this.descriptor = descriptor;
    this.signature = signature;
    this.value = value;
    }
}
