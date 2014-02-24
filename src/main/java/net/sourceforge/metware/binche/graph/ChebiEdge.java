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


/**
 * Edge of the {@link ChebiGraph}, which is used to store the result of the enrichment analysis
 * in a graph based structure.
 *
 * @author Stephan Beisken
 * @author Pablo Moreno
 */
public class ChebiEdge {

    private String id;
    /**
     * @deprecated use {@link #ChebiEdge(String,String)} instead.
     * @param id
     * @param pValue
     */
    public ChebiEdge(String id, double pValue) {
        this.id = id;
    }

    /**
     * Initializes the edge with an id produced by concatenating the two nodes id in the given order.
     *
     * @param previousID of the from or previous node.
     * @param nextID of the to or next node.
     */
    public ChebiEdge(String previousID, String nextID) {
        this.id = previousID+"-"+nextID;
    }

    /**
     * Gets the ID of the edge.
     *
     * @return the id of the node.
     */
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null || !(obj instanceof ChebiEdge)) return false;

        ChebiEdge edge = (ChebiEdge) obj;
        if (edge.getId().equals(this.getId())) return true;

        return false;
    }
}