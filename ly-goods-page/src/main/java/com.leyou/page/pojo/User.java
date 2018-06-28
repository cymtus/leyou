package com.leyou.page.pojo;

public class User {
    String name;
    int age;
    User friend;// 对象类型属性

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public User() {
    }

    public User(String name, int age, User friend) {
        this.name = name;
        this.age = age;
        this.friend = friend;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public User getFriend() {
        return friend;
    }

    public void setFriend(User friend) {
        this.friend = friend;
    }
}