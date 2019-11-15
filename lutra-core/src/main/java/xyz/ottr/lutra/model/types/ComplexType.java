package xyz.ottr.lutra.model.types;

/*-
 * #%L
 * lutra-core
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

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public abstract class ComplexType implements TermType {

    protected final TermType inner;

    protected ComplexType(TermType inner) {
        this.inner = inner;
    }

    /**
     * @return the IRI of the (outer) term.
     */
    public abstract String getOuterIRI();

    /**
     * Get the level of nesting of complex types. Example List List X has depth 2.
     * @return
     */
    public int getDepth() {
        return getInner() instanceof ComplexType
            ? 1 + ((ComplexType) getInner()).getDepth()
            : 1;
    }

    public BasicType getInnermost() {
        return getInner() instanceof ComplexType
            ? ((ComplexType)getInner()).getInnermost()
            : (BasicType)getInner();
    }
}
