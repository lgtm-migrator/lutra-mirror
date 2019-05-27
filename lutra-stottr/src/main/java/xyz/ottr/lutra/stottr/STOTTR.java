package xyz.ottr.lutra.stottr;

/*-
 * #%L
 * lutra-stottr
 * %%
 * Copyright (C) 2018 - 2019 University of Oslo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public class STOTTR {

    public static final String indent = "\t";

    public static final String none = "none";
    public static final String listStart = "(";
    public static final String listEnd = ")";
    public static final String variablePrefix = "?";

    public static class Expanders {
        public static final String cross = "cross";
        public static final String zipMin = "zipMin";
        public static final String zipMax = "zipMax";
        public static final String expanderSep = "|";
        public static final String expander = "++";
    }

    public static class ParameterMode {
        public static final String optional = "?";
        public static final String nonBlank = "!";
    }
}
