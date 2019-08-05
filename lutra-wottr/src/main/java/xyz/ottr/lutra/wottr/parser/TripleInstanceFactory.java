package xyz.ottr.lutra.wottr.parser;

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

import java.util.function.Supplier;

import org.apache.jena.rdf.model.Model;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.wottr.parser.v04.WOTTR;

public class TripleInstanceFactory implements Supplier<ResultStream<Instance>> {

    private final Model model;

    public TripleInstanceFactory(Model model) {
        this.model = model;
    }

    @Override
    public ResultStream<Instance> get() {

        ExtendedIterator<Result<Instance>> parsedTriples = this.model.listStatements()
            .filterDrop(this::isPartOfRDFList)
            .mapWith(TripleInstanceFactory::createTripleInstance);
        return new ResultStream<>(parsedTriples.toSet());
    }

    /**
     * Returns true if the argument is a redundant list-triple, that is,
     * on one of the forms "(:a :b) rdf:first :a" or "(:a :b) rdf:rest (:b)".
     * These statements are redundant as they will be parsed as part of a list term.
     */
    private boolean isPartOfRDFList(Statement statement) {

        Resource subject = statement.getSubject();
        Property predicate = statement.getPredicate();

        // TODO: possible fix, add check to see that subject is "used", ie. is also an triple object.
        // there must be a rdf:rest for each rdf:first, and vice versa.
        return subject.canAs(RDFList.class)
            && (predicate.equals(RDF.first) && this.model.contains(subject, RDF.rest))
                || predicate.equals(RDF.rest) && this.model.contains(subject, RDF.first);
    }

    private static Result<Instance> createTripleInstance(Statement stmt) {

        TermFactory rdfTermFactory = new TermFactory(WOTTR.theInstance);
        Result<Term> sub = rdfTermFactory.apply(stmt.getSubject());
        Result<Term> pred = rdfTermFactory.apply(stmt.getPredicate());
        Result<Term> obj = rdfTermFactory.apply(stmt.getObject());

        ArgumentList as = sub.isPresent() && pred.isPresent() && obj.isPresent()
            ? new ArgumentList(sub.get(), pred.get(), obj.get()) : null;
        Result<ArgumentList> asRes = Result.ofNullable(as);
        asRes.addMessages(sub.getMessages());
        asRes.addMessages(pred.getMessages());
        asRes.addMessages(obj.getMessages());

        return asRes.map(asVal -> new Instance(OTTR.BaseURI.NullableTriple, asVal));
    }
}
