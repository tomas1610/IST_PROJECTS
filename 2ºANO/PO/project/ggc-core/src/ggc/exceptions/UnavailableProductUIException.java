package ggc.exceptions;

public class UnavailableProductUIException extends Exception{

    private static final long serialVersionUID = 202109091821L;

    private String _key;
    private int _requested;
    private int _available;

    public UnavailableProductUIException(String key,int requested, int available){
        _key = key;
        _requested = requested;
        _available = available;
    }

    public String getKey(){
        return _key;
    }

    public int getRequested(){
        return _requested;
    }

    public int getAvailable(){
        return _available;
    }
}