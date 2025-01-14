/*-
 * #%L
 * lutra-api
 * %%
 * Copyright (C) 2018 - 2020 University of Oslo
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import xyz.ottr.lutra.api.StandardFormat;
import xyz.ottr.lutra.api.StandardTemplateManager;
import xyz.ottr.lutra.io.Format;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;

@RunWith(Parameterized.class)
public class FormatEquivalenceTest {

    private Format format;
    private Signature signature;

    public FormatEquivalenceTest(Signature signature, String uri, Format format, String formatName) {
        this.format = format;
        this.signature = signature;
    }

    @Parameterized.Parameters(name = "{index}: {3}: {1} ")
    public static List<Object[]> data() {

        List<Object[]> data = new ArrayList<>();

        // collect formats relevant for templates
        var formats = Arrays.stream(StandardFormat.values())
            .map(std -> std.format)
            .filter(Format::supportsTemplateReader)
            .filter(Format::supportsTemplateWriter)
            .collect(Collectors.toList());

        var stdLib = new StandardTemplateManager();
        stdLib.loadStandardTemplateLibrary();
        // collect signatures
        var signatures = stdLib.getStandardLibrary()
            .getAllSignatures()
            .getStream()
            .map(Result::get)
            .collect(Collectors.toList());

        // combine collected templates with collected formats
        for (Format f : formats) {
            for (Signature s : signatures) {
                data.add(new Object[] { s, s.getIri(), f, f.getFormatName() });
            }
        }
        return data;
    }

    @Test
    public void test() throws Exception {

        assumeTrue(this.format.supportsTemplateReader());
        assumeTrue(this.format.supportsTemplateWriter());
       
        var writer = this.format.getTemplateWriter().get();
        String folderPath = "src/test/resources/FormatEquivalanceTest/";
                
        BiFunction<String, String, Optional<Message>> writerFunc = (iri, str) -> {
            return xyz.ottr.lutra.io.Files
                    .writeTemplatesTo(iri, str, folderPath, this.format.getDefaultFileSuffix());
        };
        
        writer.setWriterFunction(writerFunc);
        writer.accept(this.signature); //write file
        
        // read file
        String iriFilePath = xyz.ottr.lutra.io.Files.iriToPath(this.signature.getIri()) + "" + this.format.getDefaultFileSuffix();
        String absFilePath = Path.of(folderPath + iriFilePath).toAbsolutePath().toString();
        
        var reader = this.format.getTemplateReader().get();
        var ioSignatures = reader.apply(absFilePath)
            .getStream()
            .collect(Collectors.toList());
                
        assertThat(ioSignatures.size(), is(1));
        assertThat(ioSignatures.get(0).get(), is(this.signature));
        
        deleteDirectory(new File(folderPath));

    }
    
    private void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }
}
