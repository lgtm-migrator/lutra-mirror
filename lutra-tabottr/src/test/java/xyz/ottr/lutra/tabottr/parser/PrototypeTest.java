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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import xyz.ottr.lutra.io.InstanceParser;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultConsumer;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.tabottr.model.Table;

public class PrototypeTest {

    private static final Path ROOT = Paths.get("src", "test", "resources");

    @Test 
    public void shouldParseToInstances1() {
        InstanceParser<String> parser = new ExcelReader();
        ResultStream<Instance> instances = parser.apply(ROOT.resolve("test1.xlsx").toString());
        ResultConsumer<Instance> consumer = new ResultConsumer<>();
        instances.forEach(consumer);
        assertFalse(Message.moreSevere(consumer.getMessageHandler().printMessages(), Message.ERROR));
    }

    @Test
    public void shouldParseToInstances2() {
        InstanceParser<String> parser = new ExcelReader();
        ResultStream<Instance> instances = parser.apply(ROOT.resolve("test-noPrefixes.xlsx").toString());
        ResultConsumer<Instance> consumer = new ResultConsumer<>();
        instances.forEach(consumer);
        assertFalse(Message.moreSevere(consumer.getMessageHandler().printMessages(), Message.ERROR));
    }

    @Test
    public void shouldParseToTables() {
        String filename = ROOT.resolve("test1.xlsx").toString();
        Result<List<Table>> tables = ExcelReader.parseTables(filename);
        ResultConsumer<List<Table>> consumer = new ResultConsumer<>();
        consumer.accept(tables);
        assertFalse(Message.moreSevere(consumer.getMessageHandler().printMessages(), Message.ERROR));
    }

    @Test
    public void prefixConflicts() {
        String filename = ROOT.resolve("testConflictingPrefixes.xlsx").toString();

        InstanceParser<String> parser = new ExcelReader();
        ResultStream<Instance> instances = parser.apply(filename);
        ResultConsumer<Instance> consumer = new ResultConsumer<>();
        instances.forEach(consumer);
        assertTrue(Message.moreSevere(consumer.getMessageHandler().printMessages(), Message.ERROR));
    }
}
