package xyz.ottr.lutra.wottr.legacy.io;

/*-
 * #%L
 * lutra-wottr
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import org.dyreriket.gaupa.rdf.ModelSelector;
import org.dyreriket.gaupa.rdf.ModelSelectorException;

import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.NoneTerm;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.wottr.legacy.WOTTR;
import xyz.ottr.lutra.wottr.legacy.WTermFactory;

public class WArgumentParser implements Function<Resource, Result<Term>> {

    private final Model model;
    private final WTermFactory rdfTermFactory;
    private final Set<Term> expanderValues;
    private ArgumentList.Expander listExpander;

    public WArgumentParser(Model model) {
        this.model = model;
        this.rdfTermFactory = new WTermFactory();
        this.expanderValues = new HashSet<>();
    }

    public Result<Term> apply(Resource p) {

        Result<Term> resultTerm;

        try {
            // Property type;
            
            // Must have a variable/value:
            Collection<Property> valueProperties = Arrays.asList(new Property[] { WOTTR.value, WOTTR.eachValue });
            Statement varAssignment = ModelSelector.getOptionalStatementWithProperties(model, p,
                    valueProperties);
            // type = varAssignment.getPredicate();

            resultTerm = varAssignment != null
                ? rdfTermFactory.apply(varAssignment.getObject())
                : Result.of(new NoneTerm());

            // Add to eachValue if necessary
            if (varAssignment != null && varAssignment.getPredicate().equals(WOTTR.eachValue)) {
                resultTerm.ifPresent(term -> this.expanderValues.add(term));
                if (this.listExpander == null || this.listExpander.equals(ArgumentList.Expander.CROSS)) {
                    this.listExpander = ArgumentList.Expander.CROSS;
                } else {
                    resultTerm = Result.empty(new Message(Message.ERROR,
                                "Error parsing instance " + p.toString()
                                + ": An instance cannot have two different list expanders set. "));
                }
            }
        } catch (ModelSelectorException ex) {
            // TODO: Correct lvl and good message?
            resultTerm = Result.empty(new Message(Message.ERROR, "Error parsing argument. " + ex.getMessage()));
        }

        return resultTerm;
    }

    public Set<Term> getExpanderValues() {
        return this.expanderValues;
    }

    public ArgumentList.Expander getListExpander() {
        return this.listExpander;
    }
}