package xyz.ottr.lutra.wottr;

/*-
 * #%L
 * lutra-wottr
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

import org.apache.jena.shared.PrefixMapping;

import xyz.ottr.lutra.io.Format;
import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.io.InstanceWriter;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.io.TemplateWriter;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.wottr.io.RDFFileReader;
import xyz.ottr.lutra.wottr.parser.v04.WInstanceParser;
import xyz.ottr.lutra.wottr.parser.v04.WTemplateParser;
import xyz.ottr.lutra.wottr.writer.v04.WInstanceWriter;
import xyz.ottr.lutra.wottr.writer.v04.WTemplateWriter;

public class WottrFormat implements Format {

    private PrefixMapping prefixes;

    public WottrFormat() {
        this(PrefixMapping.Factory.create());
    }
    
    public WottrFormat(PrefixMapping prefixes) {
        this.prefixes = prefixes;
    }

    @Override
    public Result<TemplateReader> getTemplateReader() {
        return Result.of(new TemplateReader(new RDFFileReader(), new WTemplateParser()));
    }

    @Override
    public Result<TemplateWriter> getTemplateWriter() {
        return Result.of(new WTemplateWriter(this.prefixes));
    }

    @Override
    public Result<InstanceReader> getInstanceReader() {
        return Result.of(new InstanceReader(new RDFFileReader(), new WInstanceParser()));
    }

    @Override
    public Result<InstanceWriter> getInstanceWriter() {
        return Result.of(new WInstanceWriter(this.prefixes));
    }

    @Override
    public boolean supports(Format.Operation op, Format.ObjectType ot) {
        return true;
    }

    @Override
    public String getDefaultFileSuffix() {
        return ".ttl";
    }

    @Override
    public String getFormatName() {
        return "WOTTR";
    }

    @Override
    public void setPrefixMapping(PrefixMapping prefixes) {
        this.prefixes = prefixes;
    }
}