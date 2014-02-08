package com.skrumaz.app.utils;

import com.skrumaz.app.classes.AttributeType;

/**
 * Created by Paito Anderson on 1/26/2014.
 */
public class AttributeTypeLookup {

    public static AttributeType stringToAttributeType(String attributeType) {

        // When we move to Java SE 7 we can have our String Switch case ;)
        if (attributeType.equalsIgnoreCase("BOOLEAN")) {
            return AttributeType.BOOLEAN;
        } else if (attributeType.equalsIgnoreCase("INTEGER")) {
            return AttributeType.INTEGER;
        } else if (attributeType.equalsIgnoreCase("QUANTITY")) {
            return AttributeType.QUANTITY;
        } else if (attributeType.equalsIgnoreCase("STRING")) {
            return AttributeType.STRING;
        } else if (attributeType.equalsIgnoreCase("TEXT")) {
            return AttributeType.TEXT;
        } else if (attributeType.equalsIgnoreCase("DATE")) {
            return AttributeType.DATE;
        } else if (attributeType.equalsIgnoreCase("STATE")) {
            return AttributeType.STATE;
        } else if (attributeType.equalsIgnoreCase("RATING")) {
            return AttributeType.RATING;
        } else if (attributeType.equalsIgnoreCase("COLLECTION")) {
            return AttributeType.COLLECTION;
        } else if (attributeType.equalsIgnoreCase("OBJECT")) {
            return AttributeType.OBJECT;
        } else {
            return AttributeType.BOOLEAN;
        }
    }

    public static String attributeTypeToString(AttributeType attributeType) {

        if (attributeType == null) {
            return "BOOLEAN";
        }

        switch (attributeType)
        {
            case BOOLEAN:
                return "BOOLEAN";
            case INTEGER:
                return "INTEGER";
            case QUANTITY:
                return "QUANTITY";
            case STRING:
                return "STRING";
            case TEXT:
                return "TEXT";
            case DATE:
                return "DATE";
            case STATE:
                return "STATE";
            case RATING:
                return "RATING";
            case COLLECTION:
                return "COLLECTION";
            case OBJECT:
                return "OBJECT";
            default:
                return "BOOLEAN";
        }
    }
}
