package com.skrumaz.app;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paito Anderson on 2013-09-21.
 */
public class Group {
    public String string;
    public final List<String> children = new ArrayList<String>();

    public Group(String string) {
        this.string = string;
    }
}
