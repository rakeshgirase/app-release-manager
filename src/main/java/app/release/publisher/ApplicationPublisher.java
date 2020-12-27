package app.release.publisher;

import java.util.Locale;

import app.release.model.CommandLineArguments;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Localizable;

/**
 * Uploads application bundle to Google Play Store.
 */
@Slf4j
public class ApplicationPublisher {

    public static void main(String... args) {
        try {
            CommandLineArguments arguments = toCommandLineArguments(args);
            Publisher publisher = PublisherFactory.buildPublisher(arguments);
            publisher.publish();
        } catch (Exception e) {
            log.error("ERROR: [{}]", e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static CommandLineArguments toCommandLineArguments(String[] args) throws CmdLineException {
        CommandLineArguments arguments = new CommandLineArguments();
        CmdLineParser parser = new CmdLineParser(arguments);
        // must have args
        if (args == null || args.length < 1) {
            String msg = "No arguments given";
            throw new CmdLineException(parser, localize(msg), msg);
        }
        parser.parseArgument(args);
        return arguments;
    }

    /**
     * Parse process arguments.
     *
     * @param args process arguments
     * @return {@link ApplicationPublisher} instance
     * @throws CmdLineException arguments error
     */
    private Publisher parseArgs(String... args) throws CmdLineException {
        // init parser
        CommandLineArguments arguments = new CommandLineArguments();
        CmdLineParser parser = new CmdLineParser(arguments);

        try {
            // must have args
            if (args == null || args.length < 1) {
                String msg = "No arguments given";
                throw new CmdLineException(parser, this.localize(msg), msg);
            }

            // parse args
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            // print usage and forward error
            log.error("Invalid arguments.");
            log.error("Options:");
            parser.printUsage(System.err);
            throw e;
        }

        return PublisherFactory.buildPublisher(arguments);
    }

    /**
     * Construct localized version on message
     *
     * @param message message
     * @return localized version
     */
    private static Localizable localize(String message) {
        return new Localizable() {

            @Override
            public String formatWithLocale(Locale locale, Object... args) {
                return String.format(locale, message, args);
            }

            @Override
            public String format(Object... args) {
                return String.format(message, args);
            }
        };
    }
}