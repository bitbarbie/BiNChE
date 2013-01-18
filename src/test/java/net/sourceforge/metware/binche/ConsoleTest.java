/*
 * Copyright (c) 2012, Stephan Beisken. All rights reserved.
 *
 * This file is part of MassCascade.
 *
 * MassCascade is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MassCascade is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MassCascade. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.metware.binche;

import net.sourceforge.metware.binche.execs.BiNCheExec;
import org.junit.Test;

import java.io.File;

public class ConsoleTest {

    private final String input = "/BiNGO/data/7852_input.tsv";
    private final String output = System.getProperty("user.dir") + File.separator + "7852_result.svg";

    @Test
    public void testConsole() {

        new BiNCheExec(new String[] { "-i" + input, "-o" + output }).process();
    }
}