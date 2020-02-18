package xyz.ottr.lutra.cli;

/*-
 * #%L
 * lutra-cli
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

import java.io.PrintStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;

import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

import xyz.ottr.lutra.TemplateManager;
import xyz.ottr.lutra.io.Format;
import xyz.ottr.lutra.io.FormatManager;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.MessageHandler;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.wottr.io.RDFFileReader;

public class CLI {

    private final Settings settings;
    private final PrintStream outStream;
    //private final PrintStream errStream;
    private final MessageHandler messageHandler;
    private final FormatUtils formatUtils;
    private final TemplateManager templateManager;

    public CLI(PrintStream outStream, PrintStream errStream) {
        this.settings = new Settings();
        this.outStream = outStream;
        //this.errStream = errStream;
        this.messageHandler = new MessageHandler(errStream);

        // TODO: Fix this (almost) cyclical dependency between
        //       FormatManager, TemplateManager and FormatUtils (prefixes)
        FormatManager fm = new FormatManager();
        this.templateManager = new TemplateManager(fm);
        this.templateManager.setDeepTrace(this.settings.deepTrace);
        this.templateManager.setHaltOn(this.settings.haltOn);
        this.templateManager.setFetchMissingDependencies(this.settings.fetchMissingDependencies);
        this.templateManager.setExtensions(this.settings.extensions);
        this.templateManager.setIgnoreExtensions(this.settings.ignoreExtensions);

        this.formatUtils = new FormatUtils(this.templateManager.getPrefixes());
        fm.register(this.formatUtils.getFormats());
    }

    public CLI() {
        this(System.out, System.err);
    }

    public static void main(String[] args) {
        new CLI().run(args);
    }

    public void run(String[] args) {

        CommandLine cli = new CommandLine(this.settings);
        try {
            cli.parse(args);
        } catch (ParameterException ex) {
            Message err = Message.error(ex.getMessage());
            this.messageHandler.printMessage(err);
            return;
        }

        this.messageHandler.setQuiet(this.settings.quiet);

        if (cli.isUsageHelpRequested()) {
            cli.usage(this.outStream);
        } else if (cli.isVersionHelpRequested()) {
            cli.printVersionHelp(this.outStream);
        } else if (checkOptions()) {
            execute();
        }
    }

    /**
     * Checks that the provided options form a meaningful execution,
     * otherwise prints an error message.
     */
    private boolean checkOptions() {

        if (this.settings.inputs.isEmpty()
            && (this.settings.mode == Settings.Mode.expand
                || this.settings.mode == Settings.Mode.format)) {

            this.messageHandler.printMessage(Message.error("Must provide one or more input files. "
                + "For help on usage, use the --help option."));
            return false;
        } else if (this.settings.library == null
            && (this.settings.mode == Settings.Mode.expandLibrary
                || this.settings.mode == Settings.Mode.formatLibrary
                || this.settings.mode == Settings.Mode.lint)) {

            this.messageHandler.printMessage(Message.error("Must provide a library. "
                + "For help on usage, use the --help option."));
            return false;
        }
        return true;
    }


    ////////////////////////////////////////////////////////////
    /// MAIN EXECUTION                                       ///
    ////////////////////////////////////////////////////////////

    private void execute() {

        if (this.settings.library != null && this.settings.library.length != 0) {
            Format libraryFormat = this.formatUtils.getFormat(this.settings.libraryFormat);
            this.templateManager.parseLibraryInto(libraryFormat, this.settings.library);
        }

        if (StringUtils.isNotBlank(this.settings.prefixes)) {
            Result<Model> userPrefixes = new RDFFileReader().parse(this.settings.prefixes);
            this.messageHandler.use(userPrefixes, up -> this.templateManager.addPrefifxes(up));
        }

        executeMode();
    }

    private void executeExpand() {

        ResultStream<Instance> ins = parseAndExpandInstances();
        Format outFormat = this.formatUtils.getFormat(this.settings.outputFormat);

        if (shouldPrintOutput()) {
            // TODO: Print to stdout
            this.outStream.println();
        }
        MessageHandler msgs = this.templateManager.writeInstances(ins, outFormat, this.settings.out);

        if (!this.settings.quiet) {
            msgs.printMessages();
        }
    }

    public ResultStream<Instance> parseInstances() {
        Format inFormat = this.formatUtils.getFormat(this.settings.inputFormat);
        return this.templateManager.parseInstances(inFormat, this.settings.inputs);
    }

    public ResultStream<Instance> parseAndExpandInstances() {
        return parseInstances().innerFlatMap(this.templateManager.makeExpander());
    }

    private void executeExpandLibrary() {

        this.messageHandler.use(this.templateManager.expandStore(),
            expanded -> {

                Format outFormat = this.formatUtils.getFormat(this.settings.outputFormat);

                if (shouldPrintOutput()) {
                    // TODO: Print to stdout
                    this.outStream.println();
                }
                expanded.writeTemplates(outFormat, this.settings.out);
            }
        );
    }

    private void executeFormatLibrary() {

        Format outFormat = this.formatUtils.getFormat(this.settings.outputFormat);
        this.templateManager.writeTemplates(outFormat, this.settings.out);
    }

    private void executeFormat() {

        Format outFormat = this.formatUtils.getFormat(this.settings.outputFormat);
        this.templateManager.writeInstances(parseInstances(), outFormat, this.settings.out);
    }

    private void executeMode() {

        MessageHandler msgs = this.templateManager.checkTemplates();
        int severity = this.settings.quiet ? msgs.getMostSevere() : msgs.printMessages();

        if (Message.moreSevere(severity, this.settings.haltOn)) {
            return;
        }

        switch (this.settings.mode) {
            case expand:
                executeExpand();
                break;
            case expandLibrary:
                executeExpandLibrary();
                break;
            case formatLibrary:
                executeFormatLibrary();
                break;
            case format:
                executeFormat();
                break;
            case lint:
                // Simply load templates and check for messages, as done before the switch
                if (!this.settings.quiet && Message.moreSevere(Message.WARNING, severity)) {
                    this.outStream.println("No errors found.");
                }
                break;
            default:
                Message err = Message.error("The mode " + this.settings.mode + " is not yet supported.");
                this.messageHandler.printMessage(err);
        }
    }

    private boolean shouldPrintOutput() {
        return this.settings.stdout || this.settings.out == null;
    }
}
