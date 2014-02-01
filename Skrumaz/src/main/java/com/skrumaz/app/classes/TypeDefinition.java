package com.skrumaz.app.classes;

import java.util.List;

/**
 * Created by Paito Anderson on 1/26/2014.
 */
public class TypeDefinition {
    private Long Oid;
    private String ElementName;

    @Override
    public String toString() {
        return this.ElementName;
    }

    /*
     * Find in position in List of Projects based on Oid
     */
    public static int findOid(List<TypeDefinition> typeDefs, Long Oid)
    {
        int i = 0;
        for (TypeDefinition typeDef : typeDefs) {
            if (typeDef.getOid().equals(Oid)) {
                return i;
            }
            i++;
        }
        return 0;
    }

    /*
     * Find in Project in List of Projects based on Oid
     */
    public static TypeDefinition find(List<TypeDefinition> typeDefs, Long Oid)
    {
        for (TypeDefinition typeDef : typeDefs) {
            if (typeDef.getOid().equals(Oid)) {
                return typeDef;
            }
        }
        return new TypeDefinition();
    }

    public Long getOid() {
        return Oid;
    }

    public void setOid(Long oid) {
        this.Oid = oid;
    }

    public String getElementName() {
        return ElementName;
    }

    public void setElementName(String ElementName) {
        this.ElementName = ElementName;
    }
}
