package xyz.ottr.lutra.model.terms;

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

import java.util.Optional;

import lombok.Getter;
import lombok.NonNull;
import xyz.ottr.lutra.model.types.LUBType;
import xyz.ottr.lutra.model.types.TypeRegistry;

@Getter
public class IRITerm extends Term {

    @NonNull private final String iri;

    public IRITerm(String iri) {
        super(new LUBType(TypeRegistry.IRI), false);
        this.iri = iri;
    }

    @Override 
    public IRITerm shallowClone() {
        IRITerm term = new IRITerm(this.iri);
        term.setVariable(isVariable());
        return term;
    }

    @Override
    public Optional<Term> unify(Term other) {

        if ((other instanceof TermList 
             || other instanceof IRITerm
             || other instanceof BlankNodeTerm
             || other instanceof NoneTerm)
            && (isVariable() || !other.isVariable() && equals(other))) { 
            return Optional.of(other);
        }

        return Optional.empty();
    }

    @Override
    public boolean isBlank() {
        return false;
    }

    @Override
    public String getIdentifier() {
        return getIri();
    }
}
