package com.skrumaz.app.classes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paito Anderson on 2/3/2014.
 */
public class AllowedValue {
    private Long Oid;
    private String Name;

    @Override
    public String toString(){
        return String.valueOf(this.Name);
    }

    // Take list of AllowedValue and convert to ArrayList of string for adapter use.
    public static ArrayList<String> getOptionList(List<AllowedValue> inputList)
    {
        ArrayList<String> returnList = new ArrayList<String>();

        for (AllowedValue allowedValue : inputList)
        {
            returnList.add(allowedValue.getName());
        }

        return returnList;
    }

    public Long getOid() {
        return Oid;
    }

    public void setOid(Long oid) {
        Oid = oid;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }
}
