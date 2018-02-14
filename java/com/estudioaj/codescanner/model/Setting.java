package com.estudioaj.codescanner.model;

/**
 * Created by JOSE CHACIN on 24/08/2017.
 */

public class Setting {
    private String option;
    private Boolean switched;
    private String key;

    public Setting() {
    }

    public Setting(String option, Boolean switched, String key) {
        this.option = option;
        this.switched = switched;
        this.key = key;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public Boolean getSwitched() {
        return switched;
    }

    public void setSwitched(Boolean switched) {
        this.switched = switched;
    }

    public String getKey() {
        return key;
    }

    public void getKey(String key) {
        this.key = key;
    }
}
