/**
 * ChEBIOntologyTerm.java
 *
 * 2012.10.19
 *
 * This file is part of the CheMet library
 * 
 * The CheMet library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * CheMet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with CheMet.  If not, see <http://www.gnu.org/licenses/>.
 */

package cytoscape.data.annotation;


import org.apache.log4j.Logger;

/**
 * Extends the OntologyTerm to accommodate to a ChEBI Term, which can be either a defined small molecule or a chemical/</br>
 * role class.
 *
 * @author Pablo Moreno
 */
public class ChEBIOntologyTerm extends OntologyTerm{

    private boolean molecule;

    /**
     * Ontology term used to represent ChEBI entities. The name is the label name to be used in the tabular and graphical
     * outputs, and should correspond to the main name of the ChEBI entity represented. The id correspond to the numeric
     * part of the ChEBI ID (number 29101 in CHEBI:29101).
     *
     * @param name the main name of the ChEBI entity represented.
     * @param id the numeric part of the ChEBI id.
     */
    public ChEBIOntologyTerm(String name, int id) {
        super(name, id);
    }

    /**
     *
     * @return true if the chemical entity is a defined molecule (ie. can have an InChI computed, has no variable parts.)
     */
    public boolean isMolecule() {
        return molecule;
    }

    /**
     * Sets whether the chemical entity represented by this ChEBI term is a molecule or not.
     *
     * @param molecule true if the entity is a molecule (has a defined chemical structure, ie. can have an InChI computed).
     */
    public void setMolecule(boolean molecule) {
        this.molecule = molecule;
    }
}
