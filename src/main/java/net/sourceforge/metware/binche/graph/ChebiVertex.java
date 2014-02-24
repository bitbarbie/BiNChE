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

/**
 * Vertex of the {@link ChebiGraph}, which is used to store the result of the enrichment analysis
 * in a graph based structure.
 *
 * @author Stephan Beisken
 * @author Pablo Moreno
 */
public class ChebiVertex {

    private int id;
    private String chebiId;
    private Color color;
    private String chebiName;
    private Boolean isMolecule=false;
    private Double pValue;
    private Double corrPValue;
    private Double foldOfEnrichment;
    private Double samplePercentage;

    /**
     * Retrieves the non-corrected (FDR) p-value.
     *
     * @return the p-value as Double.
     */
    public Double getpValue() {
        return pValue;
    }

    /**
     * Sets the pValue
     *
     * @param pValue
     */
    public void setpValue(Double pValue) {
        this.pValue = pValue;
    }

    /**
     * Initializes the vertex with an internal id, the ChEBI ID and the ChEBI Name of the entity represented
     * by the vertex.
     *
     * @param id normally the integer part of the ChEBI ID, but could be something different.
     * @param chebiId the usual ChEBI:integer
     * @param chebiName the name of the ChEBI Entity
     */
    public ChebiVertex(int id, String chebiId, String chebiName) {
        this.id = id;
        this.chebiId = chebiId;
        this.chebiName = chebiName;

        color = new Color(255, 255, 255, 128);
    }

    /**
     * Initializes the vertex with an internal id, the ChEBI ID and the ChEBI Name of the entity represented
     * by the vertex, additionally setting whether the entity is a well defined molecule or a class of molecules.
     *
     * @param id
     * @param chebiId
     * @param chebiName
     * @param molecule true if the entity is a molecule and not a molecule class.
     */
    public ChebiVertex(int id, String chebiId, String chebiName, Boolean molecule) {
        this(id, chebiId, chebiName);
        this.isMolecule = molecule;
    }

    /**
     * Gets the integer identifier.
     *
     * @return the identifier.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the ChEBI ID of the entity.
     *
     * @return the ChEBI ID.
     */
    public String getChebiId() {
        return chebiId;
    }

    /**
     * Gets the ChEBI name of the entity represented.
     *
     * @return the ChEBI name of the entity.
     */
    public String getChebiName() {
        return chebiName;
    }

    /**
     * Sets the color to be used for the entity when the graph is drawn.
     *
     * @param color
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Gets the color assigned to the entity.
     *
     * @return
     */
    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        return (chebiName);
    }

    /**
     * Returns true if the entity represents a molecule and not a molecule class.
     *
     * @return true if entity is a molecule.
     */
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

    /**
     * @return the corrected p-value
     */
    public Double getCorrPValue() {
        return corrPValue;
    }

    /**
     * Sets the corrected p-value
     *
     * @param corrPValue the corrected p-value to set
     */
    public void setCorrPValue(Double corrPValue) {
        this.corrPValue = corrPValue;
    }

    /**
     *
     * @return the fold of enrichment for this node.
     */
    public Double getFoldOfEnrichment() {
        return foldOfEnrichment;
    }

    /**
     * Sets the fold of enrichment for the entity represented by this node.
     *
     * @param foldOfEnrichment the fold of enrichment to set.
     */
    public void setFoldOfEnrichment(Double foldOfEnrichment) {
        this.foldOfEnrichment = foldOfEnrichment;
    }

    /**
     * Gets the percentage of the sample that is covered by this entity.
     *
     * @return the samplePercentage
     */
    public Double getSamplePercentage() {
        return samplePercentage;
    }

    /**
     * Sets the sample percentage.
     *
     * @param samplePercentage the samplePercentage to set
     */
    public void setSamplePercentage(Double samplePercentage) {
        this.samplePercentage = samplePercentage;
    }

    
    
}
