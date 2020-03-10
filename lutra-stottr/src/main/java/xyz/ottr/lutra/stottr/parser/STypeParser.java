package xyz.ottr.lutra.stottr.parser;

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

import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.types.BasicType;
import xyz.ottr.lutra.model.types.LUBType;
import xyz.ottr.lutra.model.types.ListType;
import xyz.ottr.lutra.model.types.NEListType;
import xyz.ottr.lutra.model.types.TermType;
import xyz.ottr.lutra.model.types.TypeRegistry;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;
import xyz.ottr.lutra.system.Result;

public class STypeParser extends SBaseParserVisitor<TermType> {

    private final STermParser termParser;

    STypeParser(STermParser termParser) {
        this.termParser = termParser;
    }

    public Result<TermType> visitType(stOTTRParser.TypeContext ctx) {
        return visitChildren(ctx);
    }

    public Result<TermType> visitListType(stOTTRParser.ListTypeContext ctx) {
        Result<TermType> innerRes = visitType(ctx.type());
        return innerRes.flatMap(inner -> Result.of(new ListType(inner)));
    }
    
    public Result<TermType> visitNeListType(stOTTRParser.NeListTypeContext ctx) {
        Result<TermType> innerRes = visitType(ctx.type());
        return innerRes.flatMap(inner -> Result.of(new NEListType(inner)));
    }
    
    public Result<TermType> visitLubType(stOTTRParser.LubTypeContext ctx) {
        Result<TermType> innerRes = visitBasicType(ctx.basicType());
        return innerRes.flatMap(inner -> Result.of(new LUBType((BasicType) inner)));
    }
    
    public Result<TermType> visitBasicType(stOTTRParser.BasicTypeContext ctx) {
        Result<String> iriRes = this.termParser.visit(ctx).map(term -> ((IRITerm) term).getIri());
        return iriRes.map(TypeRegistry::getType);
    }
}
