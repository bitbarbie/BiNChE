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
 * @name    ChEBIOntologyTerm
 * @date    2012.10.19
 * @version $Rev$ : Last Changed $Date$
 * @author  pmoreno
 * @author  $Author$ (this version)
 * @brief   ...class description...
 *
 */
public class ChEBIOntologyTerm extends OntologyTerm{

    private static final Logger LOGGER = Logger.getLogger( ChEBIOntologyTerm.class );

    private boolean molecule;
    
    public ChEBIOntologyTerm(String name, int id) {
        super(name, id);
    }

    public boolean isMolecule() {
        return molecule;
    }

    public void setMolecule(boolean molecule) {
        this.molecule = molecule;
    }
}
