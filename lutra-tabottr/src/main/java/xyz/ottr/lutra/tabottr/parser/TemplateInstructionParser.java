package xyz.ottr.lutra.tabottr.parser;

/*-
 * #%L
 * lutra-tab
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

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PrefixMapping;

import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.tabottr.model.TemplateInstruction;
import xyz.ottr.lutra.wottr.parser.TermFactory;
import xyz.ottr.lutra.wottr.vocabulary.v04.WOTTR;

public class TemplateInstructionParser {

    private final RDFNodeFactory dataFactory;
    private final TermFactory termFactory;
    
    public TemplateInstructionParser(PrefixMapping prefixes) {
        this.dataFactory = new RDFNodeFactory(prefixes);
        this.termFactory = new TermFactory(WOTTR.theInstance);
    }
    
    private Result<Instance> createTemplateInstance(String templateIRI, List<String> arguments, List<String> argumentTypes) {
        List<Result<Term>> members = new LinkedList<>();
        for (int i = 0; i < arguments.size(); i += 1) {
            Result<RDFNode> rdfNode = this.dataFactory.toRDFNode(arguments.get(i), argumentTypes.get(i));
            members.add(rdfNode.flatMap(this.termFactory));
        }
        Result<List<Term>> rsArguments = Result.aggregate(members);
        return rsArguments.map(args ->
                new Instance(templateIRI, new ArgumentList(args)));
    }

    Stream<Result<Instance>> processTemplateInstruction(TemplateInstruction instruction) {

        String templateIRI = this.dataFactory.toResource(instruction.getTemplateIRI()).toString();
        List<String> argumentTypes = instruction.getArgumentTypes();

        return instruction.getTemplateInstanceRows().stream()
                .map(argList -> createTemplateInstance(templateIRI, argList, argumentTypes));
    }
}
