package com.lazhoff.mule.secureprops.adapter;

import com.mulesoft.tools.SecurePropertiesTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

public class SecurePropertiesToolAdapter implements SecurePropertiesToolRunner {

    private static final Logger logger = LogManager.getLogger(SecurePropertiesToolAdapter.class);

    public static class ExecutionResult {
        public final int exitCode;
        public final String output;
        public final String error;

        public ExecutionResult(int exitCode, String output, String error) {
            this.exitCode = exitCode;
            this.output = output;
            this.error = error;
        }
    }

    @Override
    public ExecutionResult run(SecurePropertiesConfig config) {
        logger.info("Running SecurePropertiesTool with type={}, action={}, algorithm={}, mode={}",
                config.getType(), config.getAction(), config.getAlgorithm(), config.getMode());

        logger.debug("Configuration details:\n" +
                        "  key           = {}\n" +
                        "  useRandomIV   = {}\n" +
                        "  inputString   = {}\n" +
                        "  inputFile     = {}\n" +
                        "  outputFile    = {}",
                config.getKey(), config.isUseRandomIV(),
                config.getInputString(), config.getInputFile(), config.getOutputFile());

        try {
            switch (config.getType()) {
                case STRING:
                    return processString(config);

                case FILE:
                case WHOLE_FILE:
                    return processFile(config);

                default:
                    String msg = "Unsupported config type: " + config.getType();
                    logger.error(msg);
                    return new ExecutionResult(-1, "", msg);
            }
        } catch (Throwable t) {
            logger.error("Execution failed: {}", t.toString(), t);
            return new ExecutionResult(-2, "", t.getMessage());
        }
    }


    private ExecutionResult processString(SecurePropertiesConfig config) throws Exception {
        logger.debug("Executing string operation...");

        String result = SecurePropertiesTool.applyOverString(
                config.getAction().name().toLowerCase(),
                config.getAlgorithm(),
                config.getMode(),
                config.getKey(),
                config.isUseRandomIV(),
                config.getInputString()
        );

        logger.debug("String result: {}", result);
        logger.info("Operation completed successfully (string).");

        return new ExecutionResult(0, result, "");
    }

    private ExecutionResult processFile(SecurePropertiesConfig config) {
        logger.debug("Executing file/wholeFile operation...");

        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();

        try (PrintStream out = new PrintStream(outBuffer);
             PrintStream err = new PrintStream(errBuffer)) {

            //System.setOut(out);
            //System.setErr(err);

            String[] args = new String[]{
                    config.getType().toString(),
                    config.getAction().toString(),
                    config.getAlgorithm(),
                    config.getMode(),
                    config.getKey(),
                    config.getInputFile(),
                    config.getOutputFile()
                  //  "--use-random-iv " + config.isUseRandomIV()
            };
            logger.debug("args:\n{}", Arrays.toString(args));
            SecurePropertiesTool.main(args);

            logger.debug("File stdout:\n{}", outBuffer.toString());
            logger.debug("File stderr:\n{}", errBuffer.toString());
            logger.info("Operation completed successfully (file).");

            return new ExecutionResult(0, outBuffer.toString(), errBuffer.toString());

        } catch (Throwable t) {
            logger.error("File operation failed: {}", t.toString(), t);
            return new ExecutionResult(-2, outBuffer.toString(), errBuffer.toString());
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
            logger.debug("System.out and System.err restored.");
        }
    }


}
