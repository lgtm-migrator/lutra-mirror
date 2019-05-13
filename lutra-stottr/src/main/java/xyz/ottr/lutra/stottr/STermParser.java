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

import org.antlr.v4.runtime.tree.TerminalNode;

import org.apache.jena.vocabulary.XSD;

import xyz.ottr.lutra.model.BlankNodeTerm;
import xyz.ottr.lutra.model.IRITerm;
import xyz.ottr.lutra.model.LiteralTerm;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.stottr.antlr.stOTTRBaseVisitor;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;

public class STermParser extends stOTTRBaseVisitor<Result<Term>> {

    private Map<String, String> prefixes = new HashMap<>();
    private Map<String, Term> blanks;

    public STermParser(Map<String, String> prefixes) {
        this.prefixes = prefixes;
        this.blanks = new HashMap<>();
    }

    @Override
    public Result<Term> visitTerm(stOTTRParser.TermContext ctx) {

        if (ctx.Variable() != null) {
            return Result.of(new BlankNodeTerm(ctx.Variable().getSymbol().getText()));
        }

        Result<Term> trm = visitChildren(ctx);
        return trm != null // TODO: Should never happen once all methods are implemented?
            ? trm
            : Result.empty(Message.error("Expected term but found: " + ctx.toString()));
    }
    
    @Override
    public Result<Term> visitLiteral(stOTTRParser.LiteralContext ctx) {

        if (ctx.BooleanLiteral() != null) {
            String litVal = ctx.BooleanLiteral().getSymbol().getText();
            return Result.of(new LiteralTerm(litVal, XSD.xboolean.getURI()));
        }
        return visitChildren(ctx);
    }
    
    // TODO: Finish below methods
    
    @Override
    public Result<Term> visitList(stOTTRParser.ListContext ctx) {
        return visitChildren(ctx);
    }
    
    @Override
    public Result<Term> visitNumericLiteral(stOTTRParser.NumericLiteralContext ctx) {
        return visitChildren(ctx);
    }
    
    @Override
    public Result<Term> visitRdfLiteral(stOTTRParser.RdfLiteralContext ctx) {
        return visitChildren(ctx);
    }
    
    @Override
    public Result<Term> visitIri(stOTTRParser.IriContext ctx) {

        stOTTRParser.PrefixedNameContext prefixCtx = ctx.prefixedName();

        if (prefixCtx != null) {
            return visitPrefixedName(prefixCtx);
        } else {
            return Result.of(new IRITerm(ctx.IRIREF().getSymbol().getText()));
        }
    }
    
    @Override
    public Result<Term> visitPrefixedName(stOTTRParser.PrefixedNameContext ctx) {
        String toSplit;

        TerminalNode onlyNS = ctx.PNAME_NS();
        if (onlyNS != null) {
            toSplit = onlyNS.getSymbol().getText();
        } else {
            toSplit = ctx.PNAME_LN().getSymbol().getText();
        }

        String[] prefixAndLocal = toSplit.split(":");
        String prefix = this.prefixes.get(prefixAndLocal[0]);

        if (prefix == null) {
            return Result.empty(Message.error("Unrecognized prefix in qname " + toSplit));
        }

        String iri = "<" + prefix + prefixAndLocal[1] + ">";
        return Result.of(new IRITerm(iri));
    }
    
    @Override
    public Result<Term> visitBlankNode(stOTTRParser.BlankNodeContext ctx) {

        if (ctx.anon() != null) {
            return visitAnon(ctx.anon());
        }

        String label = ctx.BLANK_NODE_LABEL().getSymbol().getText();
        if (!this.blanks.containsKey(label)) {
            Term newBlank = new BlankNodeTerm();
            this.blanks.put(label, newBlank);
            return Result.of(newBlank);
        } else {
            return Result.of(this.blanks.get(label));
        }
    }
    
    @Override
    public Result<Term> visitAnon(stOTTRParser.AnonContext ctx) {
        return Result.of(new BlankNodeTerm());
    }

}
