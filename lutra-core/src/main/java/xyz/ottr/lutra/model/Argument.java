package xyz.ottr.lutra.model;

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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.types.ListType;
import xyz.ottr.lutra.system.Result;

@Getter
@EqualsAndHashCode
@Builder(toBuilder = true)
public class Argument implements ModelElement, HasGetTerm, HasApplySubstitution<Argument> {

    private final @NonNull Term term;
    private final boolean listExpander;

    public static List<Argument> listOf(Term... terms) {
        return Arrays.stream(terms)
            .map(t -> builder().term(t).build())
            .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return toString(OTTR.getDefaultPrefixes());
    }

    @Override
    public String toString(PrefixMapping prefixMapping) {
        StringBuilder str = new StringBuilder();

        if (this.listExpander) {
            str.append("++");
        }
        str.append(this.term.toString(prefixMapping));

        return str.toString();
    }

    @Override
    public Argument apply(Substitution substitution) {
        return this.toBuilder()
            .term(this.term.apply(substitution))
            .build();
    }

    @Override
    public Result<Argument> validate() {

        var result = Result.of(this);

        // Warning if IRIs are in the OTTR namespace
        if (this.term instanceof IRITerm && ((IRITerm) term).getIri().startsWith(OTTR.namespace)) {
            result.addWarning("Suspicious argument value: " + term
                    + ". The value is in the ottr namespace: " + OTTR.namespace);
        }

        // List expander arguments must be variables or lists
        if (this.listExpander && !this.term.isVariable() && !(term.getType() instanceof ListType)) {
            result.addError("Argument is marked for list expansion, "
                + "but argument value " + term + " is not a list, but of type: " + term.getType());
        }

        return result;
    }

}