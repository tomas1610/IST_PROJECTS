package ggc.exceptions;

public class DuplicateUIPartnerException extends Exception {

    private static final long serialVersionUID = 202109091821L;

    private String _key;

    public DuplicateUIPartnerException(String key){
        _key = key;
    }

    /** @return key */
    public String getKey(){
        return _key;
    }
}