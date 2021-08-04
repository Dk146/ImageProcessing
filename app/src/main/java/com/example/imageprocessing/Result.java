package com.example.imageprocessing;

import java.util.ArrayList;

public class Result {
    private ArrayList<ArrayList<String>> result;
    private ArrayList<String> uri;
    private Result(){
        result = null;
        uri = null;
    }

    public void setResult(ArrayList<ArrayList<String>> result) {
        this.result = result;
    }

    public ArrayList<String> getResult(int index) {
        return result.get(index);
    }

    public void setUri(ArrayList<String> uri) {
        this.uri = uri;
    }

    public String getUri(int index) {
        return uri.get(index);
    }

    public boolean noneResult(){
        return result==null;
    }

    private static Result instance = null;


    public static Result getInstance() {
        if(instance == null){
            instance = new Result();
        }
        return instance;
    }
}
