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

public class LUBType implements ComplexType {

    private BasicType inner;

    public LUBType(BasicType inner) {
        this.inner = inner;
    }

    public BasicType getInner() {
        return this.inner;
    }

    @Override
    public boolean isSubTypeOf(TermType other) {

        if (other instanceof LUBType) {
            // For other LUB-types, LUB<P> is only subtype of itself
            return this.inner.equals(((LUBType) other).getInner());
        } else {
            // LUB<P> subtype of P, and thus all subtypes of P
            return inner.isSubTypeOf(other);
        }
    }

    @Override
    public boolean isCompatibleWith(TermType other) {
        return isSubTypeOf(other) || other.isSubTypeOf(this.inner);
    }
    
    @Override
    public String toString() {
        return "LUB<" + inner.toString() + ">";
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof LUBType
            && this.inner.equals(((LUBType) other).getInner());
    }

    @Override
    public int hashCode() {
        return 3 * this.inner.hashCode();
    }
}