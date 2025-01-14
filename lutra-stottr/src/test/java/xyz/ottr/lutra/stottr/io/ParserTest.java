package xyz.ottr.lutra.stottr.io;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.jena.vocabulary.XSD;
import org.junit.Test;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.ListExpander;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.LiteralTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.stottr.parser.SInstanceParser;
import xyz.ottr.lutra.stottr.parser.SParserUtils;
import xyz.ottr.lutra.stottr.parser.SPrefixParser;
import xyz.ottr.lutra.stottr.parser.STemplateParser;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultConsumer;

public class ParserTest {

    ///
    /// PREFIXES
    ///

    private Result<Map<String, String>> parsePrefixes() {

        SPrefixParser prefixParser = new SPrefixParser();
        
        String prefixes = "@prefix ex: <http://example.org/> .\n"
            + "PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "@prefix ottr:  <http://ns.ottr.xyz/0.4/> .\n"
            + "@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> . \n"
            + "@prefix :      <http://base.org/> .";

        return SParserUtils.parseString(prefixes, prefixParser);
    }
        
    private Map<String, String> makePrefixes() {

        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("ex", "http://example.org/");
        prefixes.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        prefixes.put("ottr", "http://ns.ottr.xyz/0.4/");
        prefixes.put("xsd", "http://www.w3.org/2001/XMLSchema#");
        prefixes.put("", "http://base.org/");

        return prefixes;
    }

    @Test
    public void prefixParserTest() {
        Result<Map<String, String>> parsed = parsePrefixes();
        assertTrue(parsed.isPresent());
        assertEquals(makePrefixes(), parsed.get());
    }

    ///
    /// Terms
    ///

    private List<Result<Instance>> parseInstances() {

        SInstanceParser parser = new SInstanceParser(makePrefixes(), new HashMap<>());

        String instances = ":T1(true, none, rdf:type, <http://some.uri/with#part>) . "
            + "cross | ex:T2(\"hello\"@no, ++ (\"one\", \"two\", \"three\")) . "
            + ":T3(42, 42.01, \"42.02\"^^xsd:int) . ";

        return parser.parseString(instances).getStream().collect(Collectors.toList());
    }

    private List<Instance> makeInstances() {


        var i1 = Instance.builder()
            .iri("http://base.org/T1")
            .arguments(Argument.listOf(
                LiteralTerm.createTypedLiteral("true", XSD.xboolean.getURI()),
                new NoneTerm(),
                new IRITerm("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                new IRITerm("http://some.uri/with#part"))
            ).build();

        var i2 = Instance.builder()
            .iri("http://example.org/T2")
            .argument(Argument.builder().term(new ListTerm(
                    LiteralTerm.createLanguageTagLiteral("hello", "no")))
                .build())
            .argument(Argument.builder().term(new ListTerm(
                    LiteralTerm.createPlainLiteral("one"),
                    LiteralTerm.createPlainLiteral("two"),
                    LiteralTerm.createPlainLiteral("three")))
                .listExpander(true)
                .build())
            .listExpander(ListExpander.cross)
            .build();

        var i3 = Instance.builder()
            .iri("http://base.org/T3")
            .arguments(Argument.listOf(
                LiteralTerm.createTypedLiteral("42", XSD.integer.getURI()),
                LiteralTerm.createTypedLiteral("42.01", XSD.decimal.getURI()),
                LiteralTerm.createTypedLiteral("42.02", XSD.decimal.getURI()))
            ).build();

        return List.of(i1, i2, i3);

    }

    @Test
    public void termParserTest() {

        List<Result<Instance>> parsed = parseInstances();
        List<Instance> made = makeInstances();

        for (int i = 0; i < parsed.size(); i++) {
            Result<Instance> insRes = parsed.get(i);
            assertTrue(insRes.isPresent());
            Instance pins = insRes.get();
            Instance mins = made.get(i);

            //assertEquals(mins, pins); // Fails for lists
            // Thus, need to check structural equality:
            // TODO: Should be implemented on objects in future

            assertEquals(mins.getIri(), pins.getIri());
            List<Term> pterms = pins.getArguments().stream().map(Argument::getTerm).collect(Collectors.toList());
            List<Term> mterms = mins.getArguments().stream().map(Argument::getTerm).collect(Collectors.toList());
            assertEquals(mterms.size(), pterms.size());

            // TODO: Check equality of terms
        }
    }


    private void testSignatureParsing(String signatureString) {

        STemplateParser parser = new STemplateParser();

        ResultConsumer<Signature> consumer = new ResultConsumer<>();
        parser.parseString(signatureString).forEach(consumer);

        Assertions.noErrors(consumer);
    }

    @Test
    public void testSignature1() {
        testSignatureParsing("<http://example.com#T1> [ ?s ] :: BASE .");
    }

    @Test
    public void testSignature2() {
        testSignatureParsing("<http://example.com#T1> [ ?s ] @@<http://example.com#T2>(true) :: BASE .");
    }

    @Test
    public void testSignature3() {
        testSignatureParsing("<http://example.com#T1> [ ?s ] "
            + "@@<http://example.com#T2>(true), "
            + "@@<http://example.com#T2>(true)"
            + " :: BASE .");
    }

    @Test
    public void testSignature4() {
        testSignatureParsing("<http://example.com#T1> [ ??s ].");
    }

    @Test
    public void testSignature5() {
        testSignatureParsing("<http://example.com#T1> [ !?s ].");
    }

    @Test
    public void testSignature6() {
        testSignatureParsing("<http://example.com#T1> [ ?!?s ].");
    }

    @Test
    public void testDefaultValueClass() {
        testSignatureParsing("@prefix ex:     <http://example.com/ns#> . "
                + "@prefix p: <http://tpl.ottr.xyz/pizza/0.1/> ."
                + "ex:NamedPizza [ ?pizza = p:pizza] .");
    }

    @Test
    public void testDefaultValueInt() {
        testSignatureParsing("@prefix ex:     <http://example.com/ns#> . "
                + "ex:NamedPizza [ ?pizza = 2] .");
    }

    @Test
    public void testDefaultValueString() {
        testSignatureParsing("@prefix ex:     <http://example.com/ns#> . "
                + "ex:NamedPizza [ ?pizza = 'pizza'] .");
    }

    @Test
    public void testLists() {
        testSignatureParsing("@prefix ex:     <http://example.com/ns#> . "
                + "ex:NamedPizza ["
                + "  ?pizza = 'pizza' ,"
                + "  ?country = ('Italy', 'Spain') ,  "
                + "  ?toppings = ((()))] .");
    }

    @Test
    public void testTemplate() {

        String template = "@prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
            + "@prefix foaf:   <http://xmlns.com/foaf/0.1/> . "
            + "@prefix ex:     <http://example.com/ns#> . "
            + "@prefix ottr:   <http://ns.ottr.xyz/0.4/> . "
            + " ex:Person[ ?firstName, ?lastName, ?email ] :: { "
            + "  ottr:Triple (_:person, rdf:type, foaf:Person ), "
            + "  ottr:Triple (_:person, foaf:firstName, ?firstName ), "
            + "  ottr:Triple (_:person, foaf:lastName, ?lastName ), "
            + "  ottr:Triple (_:person, foaf:mbox, ?email ) "
            + "} .";

        testSignatureParsing(template);
    }

    @Test
    public void testTemplateDefaultList() {

        String template = "@prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
                + "@prefix foaf:   <http://xmlns.com/foaf/0.1/> . "
                + "@prefix ex:     <http://example.com/ns#> . "
                + "@prefix ottr:   <http://ns.ottr.xyz/0.4/> . "
                + " ex:Types[ ?ins, ?class = (ex:Person, ex:Employee) ] :: { "
                + "  cross | ottr:Triple(?ins, rdf:Type, ++?class) "
                + "} .";

        testSignatureParsing(template);
    }

}

