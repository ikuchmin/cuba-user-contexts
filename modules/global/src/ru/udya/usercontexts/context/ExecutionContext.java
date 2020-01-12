package ru.udya.usercontexts.context;

public interface ExecutionContext {

    static ExecutionContext createContext() {
        throw new UnsupportedOperationException("ExecutionContext doesn't support createContext method");
    }
}
