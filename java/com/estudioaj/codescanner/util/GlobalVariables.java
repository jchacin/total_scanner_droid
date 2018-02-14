package com.estudioaj.codescanner.util;

/**
 * Created by JOSE CHACIN on 09/09/2017.
 */

public class GlobalVariables {
    public GlobalVariables(){}

    public static Boolean initializedBrowser = false;

    public void setInitializedBrowser(Boolean initialized){
        initializedBrowser = initialized;
    }

    public Boolean getInitializedBrowser(){
        return initializedBrowser;
    }
}
