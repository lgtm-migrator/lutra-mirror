package xyz.ottr.lutra.stottr.writer;

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

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.stottr.STOTTR;
import xyz.ottr.lutra.writer.InstanceWriter;

public class SInstanceWriter implements InstanceWriter {

    protected final List<Instance> instances;
    private final STermWriter termWriter;
       
    protected SInstanceWriter(STermWriter termWriter) {
        this.instances = new LinkedList<>();
        this.termWriter = termWriter;
    }

    public SInstanceWriter(PrefixMapping prefixes) {
        this(new STermWriter(prefixes));
    }

    public SInstanceWriter() {
        this(OTTR.getDefaultPrefixes());
    }

    @Override
    public void accept(Instance instance) {
        this.instances.add(instance);
    }

    @Override
    public String write() {

        StringBuilder builder = new StringBuilder();

        builder
            .append(SPrefixWriter.write(this.termWriter.getPrefixes()))
            .append("\n\n");

        this.instances.forEach(instance ->
            builder
                .append(writeInstance(instance))
                .append(STOTTR.Statements.statementEnd)
                .append("\n"));

        return builder.toString();
    }

    protected StringBuilder writeInstance(Instance instance) {

        StringBuilder builder = new StringBuilder();

        if (instance.hasListExpander()) {
            builder
                .append(STOTTR.Expanders.map.inverseBidiMap().getKey(instance.getListExpander()))
                .append(" ")
                .append(STOTTR.Expanders.expanderSep)
                .append(" ");
        }

        builder.append(this.termWriter.writeIRI(instance.getIri()));
        builder.append(STOTTR.Terms.insArgStart)
            .append(writeArguments(instance.getArguments()))
            .append(STOTTR.Terms.insArgEnd);

        return builder;
    }

    private StringBuilder writeArguments(List<Argument> args) {

        StringBuilder builder = new StringBuilder();
        String sep = "";

        for (Argument arg : args) {
            builder.append(sep);
            if (arg.isListExpander()) {
                builder.append(STOTTR.Expanders.expander);
            }
            builder.append(this.termWriter.write(arg.getTerm()));
            sep = STOTTR.Terms.insArgSep + " ";
        }
        return builder;
    }
}
