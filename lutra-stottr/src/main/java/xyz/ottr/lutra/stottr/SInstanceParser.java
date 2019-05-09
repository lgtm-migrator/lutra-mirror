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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;

import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.stottr.antlr.stOTTRBaseVisitor;
import xyz.ottr.lutra.stottr.antlr.stOTTRLexer;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;

public class SInstanceParser extends stOTTRBaseVisitor<Result<Instance>> {

    // TODO: Should first parse all prefixes and make a PrefixMapping
    //       Maybe need to make a SInstanceFileParser that parses all instances
    //       and prefixes?

    private Map<String, String> prefixes = new HashMap<>();

    public ResultStream<Instance> parseString(String str) {
        return parseStream(CharStreams.fromString(str));
    }

    public ResultStream<Instance> parseStream(CharStream in) {
        stOTTRLexer lexer = new stOTTRLexer(in);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        stOTTRParser parser = new stOTTRParser(commonTokenStream);

        SPrefixParser prefixParser = new SPrefixParser();
        stOTTRParser.StOTTRDocContext document = parser.stOTTRDoc();
        Result<Map<String, String>> prefixRes = prefixParser.visit(document);

        this.prefixes = prefixRes.get();
        // Below code will not be executed if prefixes are not present
        return prefixRes.mapToStream(_ignore -> {

            Stream<Result<Instance>> results = document
                .statement() // List of statments
                .stream()
                .map(stmt -> {
                    System.err.println(stmt.toString());
                    System.err.println(stmt.instance().toString());
                    return visitStatement(stmt);
                });
            
            return new ResultStream<>(results);
        });
    }

    @Override
    public Result<Instance> visitStatement(stOTTRParser.StatementContext ctx) {

        if (ctx.instance() == null) { // Not an instance
            return Result.empty(); // TODO: Decide on error or ignore?
        }

        return visitInstance(ctx.instance());
    }

    @Override
    public Result<Instance> visitInstance(stOTTRParser.InstanceContext ctx) {

        stOTTRParser.IriContext iriCtx = ctx.templateName().iri();
        String iri;

        // TODO: Move to STermParser
        stOTTRParser.PrefixedNameContext prefixCtx = iriCtx.prefixedName();
        if (prefixCtx != null) {
            // TODO: Simplify code
            String toSplit;

            TerminalNode onlyNS = prefixCtx.PNAME_NS();
            if (onlyNS != null) {
                toSplit = onlyNS.getSymbol().getText();
            } else {
                toSplit = prefixCtx.PNAME_LN().getSymbol().getText();
            }

            String[] prefixAndLocal = toSplit.split(":");
            String prefix = this.prefixes.get(prefixAndLocal[0]);
            iri = "<" + prefix + prefixAndLocal[1] + ">";
        } else {
            iri = iriCtx.IRIREF().getSymbol().getText();
        }

        if (iri == null) {
            return Result.empty(Message.error(ctx.toString()));
        } else {
            return Result.empty(Message.error(iri));
        }
    }
}
