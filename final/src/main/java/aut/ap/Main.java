package aut.ap;

import control.*;
import framework.SingletonSessionFactory;

public class Main {
    public static void main(String[] args) {
        SingletonSessionFactory.get().inTransaction(session -> {});
        new Manage().begin();
    }
}