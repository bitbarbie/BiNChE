/**
 * DefaultPreferenceSetter.java
 *
 * 2013.02.11
 *
 * This file is part of the CheMet library
 *
 * The CheMet library is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * CheMet is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with CheMet. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.metware.binche.loader;

import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import net.sourceforge.metware.binche.BiNChe;
import net.sourceforge.metware.binche.execs.PreProcessOboFile;
import org.apache.log4j.Logger;

/**
 * @name DefaultPreferenceSetter
 * @date 2013.02.11
 * @version $Rev$ : Last Changed $Date$
 * @author pmoreno
 * @author $Author$ (this version)
 * @brief Sets default values for the BiNChE file location preferences.
 *
 */
class DefaultPreferenceSetter {

    private static final Logger LOGGER = Logger.getLogger(DefaultPreferenceSetter.class);
    private Preferences prefsBiNChE;
    private final String bincheHome = System.getProperty("user.home") + File.separator + ".binche" + File.separator;
    

    public DefaultPreferenceSetter() throws BackingStoreException {
        prefsBiNChE = Preferences.userNodeForPackage(BiNChe.class);
System.out.println("Hier home: "+bincheHome);
        prefsBiNChE.put(BiNChEOntologyPrefs.StructureOntology.name(), bincheHome + "chebiInferred_chemEnt.obo");
        prefsBiNChE.put(BiNChEOntologyPrefs.RoleOntology.name(), bincheHome + "chebiInferred_roles.obo");
        prefsBiNChE.put(BiNChEOntologyPrefs.RoleAndStructOntology.name(), bincheHome + "chebiInferred_chemEnt_roles.obo");
        PreProcessOboFile ppof = new PreProcessOboFile();
        prefsBiNChE.put(BiNChEOntologyPrefs.RoleAnnot.name(), ppof.getAnnotationNameForOntology(bincheHome + "chebiInferred_roles.obo"));

        File theDir = new File(bincheHome);

        // if the directory does not exist, create it
        if (!theDir.exists()) {
            System.out.println("creating directory: " + bincheHome);
            boolean result = theDir.mkdir();
            if (result) {
                System.out.println("BiNChE directory created");
            }

        }
        prefsBiNChE.flush();
    }

    Preferences getDefaultSetPrefs() {
        return prefsBiNChE;
    }
}
