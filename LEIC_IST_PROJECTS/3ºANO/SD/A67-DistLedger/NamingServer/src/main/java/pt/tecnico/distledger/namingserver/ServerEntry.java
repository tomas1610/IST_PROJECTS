package pt.tecnico.distledger.namingserver;

import java.net.*;
import java.util.*;

/* This class contains information about each server (host and qualificator) */

public class ServerEntry {
    
    private String target;
    private String qualificator;

    public ServerEntry(String target, String qualificator) {
        this.target = target;
        this.qualificator = qualificator;
    }

    public String getTarget() {
        return target;
    }

    public void setHost(String target) {
        this.target = target;
    }

    public String getQualificator() {
        return qualificator;
    }

    public void setQualificator(String qualificator) {
        this.qualificator = qualificator;
    }

    @Override
    public String toString() {
        return "target = " + target +
            "   qualificator = " + qualificator +
            "\n";
    }
}
