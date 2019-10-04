package xyz.ottr.lutra.bottr.parser;

/*-
 * #%L
 * lutra-bottr
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

import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.Resource;
import xyz.ottr.lutra.bottr.BOTTR;
import xyz.ottr.lutra.bottr.model.ArgumentMap;
import xyz.ottr.lutra.bottr.model.ArgumentMaps;
import xyz.ottr.lutra.bottr.model.Source;
import xyz.ottr.lutra.bottr.model.TranslationSettings;
import xyz.ottr.lutra.bottr.util.DataParser;
import xyz.ottr.lutra.model.types.TermType;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.wottr.parser.TermTypeFactory;
import xyz.ottr.lutra.wottr.util.ModelSelector;
import xyz.ottr.lutra.wottr.util.RDFNodes;

class BArgumentMapsParser implements Function<RDFList, Result<ArgumentMaps>> {

    private final Model model;
    private final Source<?> source;

    BArgumentMapsParser(Model model, Source<?> source) {
        this.model = model;
        this.source = source;
    }

    @Override
    public Result<ArgumentMaps> apply(RDFList list) {
        return ResultStream.innerOf(list.asJavaList())
            .mapFlatMap(node -> RDFNodes.cast(node, Resource.class))
            .mapFlatMap(new BArgumentMapParser())
            .aggregate()
            .map(stream -> stream.collect(Collectors.toList()))
            .map(maps -> new ArgumentMaps(this.model, this.source, maps));
    }

    class BArgumentMapParser implements Function<Resource, Result<ArgumentMap>> {

        @Override
        public Result<ArgumentMap> apply(Resource resource) {
            return getArgumentMap(resource);
        }

        public Result<ArgumentMap> getArgumentMap(Resource map) {

            Result<ArgumentMap> argumentMap = Result.of(BArgumentMapsParser.this.source.createArgumentMap(BArgumentMapsParser.this.model));

            Result<TermType> type = getType(map);
            Result<String> langTag = getLanguageTag(map);

            if (type.isPresent() && langTag.isPresent()) {
                argumentMap.addMessage(Message.error("An argument map cannot have both a type and a language tag."));
            }

            argumentMap.addResult(type, ArgumentMap::setType);
            argumentMap.addResult(langTag, ArgumentMap::setLiteralLangTag);
            argumentMap.addResult(getTranslationSettings(map), ArgumentMap::setTranslationSettings);
            //argumentMap.addResult(getTranslationTable(map), ArgumentMap::setTranslationTable);

            return argumentMap;
        }

        private Result<TermType> getType(Resource map) {
            return ModelSelector.getOptionalResourceObject(BArgumentMapsParser.this.model, map, BOTTR.type)
                .flatMap(new TermTypeFactory());
        }

        private Result<String> getLanguageTag(Resource map) {
            return ModelSelector.getOptionalLiteralObject(BArgumentMapsParser.this.model, map, BOTTR.languageTag)
                .map(Literal::getLexicalForm)
                .flatMap(DataParser::asLanguageTagString);
        }

        private Result<TranslationSettings> getTranslationSettings(Resource map) {
            return ModelSelector.getOptionalResourceObject(BArgumentMapsParser.this.model, map, BOTTR.translationSettings)
                .flatMap(r -> new BTranslationSettingsParser(r).get());
        }

        //private Result<TranslationTable> getTranslationTable(Resource map) {
        //    return ModelSelector.getOptionalResourceObject(BArgumentMapsParser.this.model, map, BOTTR.translationTable)
        //        .flatMap(r -> new BTranslationTableParser(r).get());
        //}
    }
}