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

import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Model;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.wottr.io.RDFIO;

@RunWith(Parameterized.class)
public class RDFtoOTTRtoRDFParserTest {

    @Parameters(name = "{index}: {0}")
    public static List<String> data() throws IOException {
        Path folder = Paths.get("src",  "test", "resources", "w3c-rdf-tests");   

        return Files.walk(folder)
                .filter(Files::isRegularFile)
                .map(Path::toString)
                .sorted()
                .collect(Collectors.toList());
    }

    private final String filename;

    public RDFtoOTTRtoRDFParserTest(String filename) {
        this.filename = filename;
    }

    @Test
    public void test() {

        // exclude test files which are bad by design
        assumeFalse(this.filename.contains("-bad-"));
        assumeFalse(this.filename.contains("/error"));

        // Try parse file with Jena.
        var rdfModel = RDFIO.fileReader().parse(this.filename);

        // exclude remaining test files that are not accepted by the RDF parser.
        assumeTrue(rdfModel.isPresent());

        Model ottrModel = ModelUtils.getOTTRParsedRDFModel(this.filename);

        ModelUtils.testIsomorphicModels(ottrModel, rdfModel.get());
    }
}
