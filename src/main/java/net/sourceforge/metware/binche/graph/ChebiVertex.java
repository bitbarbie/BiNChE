/*
 * Copyright (c) 2012, Stephan Beisken. All rights reserved.
 *
 * This file is part of BiNChe.
 *
 * BiNChe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BiNChe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BiNChe. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.metware.binche.graph;

import java.awt.*;
import java.text.DecimalFormat;

public class ChebiVertex {

    private int id;
    private String chebiId;
    private Color color;
    private String chebiName;
    private Boolean isMolecule=false;
    private Double pValue;

    public Double getpValue() {
        return pValue;
    }

    public void setpValue(Double pValue) {
        this.pValue = pValue;
    }

    public ChebiVertex(int id, String chebiId, String chebiName) {

        this.id = id;
        this.chebiId = chebiId;
        this.chebiName = chebiName;

        color = new Color(255, 255, 255, 128);
    }
    
    public ChebiVertex(int id, String chebiId, String chebiName, Boolean molecule) {
        this(id, chebiId, chebiName);
        this.isMolecule = molecule;
    }

    public int getId() {

        return id;
    }

    public String getChebiId() {

        return chebiId;
    }

    public String getChebiName() {

        return chebiName;
    }

    public void setColor(Color color) {

        this.color = color;
    }

    public Color getColor() {

        return color;
    }

    @Override
    public String toString() {
        return (chebiName);
    }

    public boolean isMolecule() {
        return isMolecule;
    }

    /**
     * @param isMolecule true if the node represents a molecule.
     */
    public void setIsMolecule(Boolean isMolecule) {
        this.isMolecule = isMolecule;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ChebiVertex other = (ChebiVertex) obj;
        if ((this.chebiId == null) ? (other.chebiId != null) : !this.chebiId.equals(other.chebiId)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.chebiId != null ? this.chebiId.hashCode() : 0);
        return hash;
    }

    
    
}
