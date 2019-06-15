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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.shared.PrefixMapping;

import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.tabottr.model.Instruction;
import xyz.ottr.lutra.tabottr.model.PrefixInstruction;
import xyz.ottr.lutra.tabottr.model.Table;
import xyz.ottr.lutra.tabottr.model.TemplateInstruction;

public class TableParser {


    /**
     * Parses the list of tables into a ResultStream of Instances, but returns
     * empty Results if conflicts of defined prefixes or errors in terms occur.
     */
    public static ResultStream<Instance> processInstructions(List<Table> tables) {

        // collect all instructions
        List<Instruction> instructions = tables.stream()
            .flatMap(table -> table.getInstructions().stream())
            .collect(Collectors.toList());

        Result<PrefixMapping> prefixes = new PrefixParser().processPrefixInstructions(instructions);

        // process instances, with prefixes as input
        ResultStream<Instance> instances = prefixes.mapToStream(pfs ->
            new ResultStream<>(
                instructions.stream()
                    .filter(ins -> ins instanceof TemplateInstruction)
                    .map(ins -> (TemplateInstruction) ins)
                    .flatMap(ins -> processTemplateInstruction(ins, pfs))));

        return instances;
    }

    private static Stream<Result<Instance>> processTemplateInstruction(TemplateInstruction instruction, PrefixMapping prefixes) {
            TemplateInstanceFactory factory = new TemplateInstanceFactory(
                prefixes,
                instruction.getTemplateIRI(),
                instruction.getArgumentTypes());

        return instruction.getTemplateInstanceRows().stream()
                .map(factory::createTemplateInstance);
    }

}
