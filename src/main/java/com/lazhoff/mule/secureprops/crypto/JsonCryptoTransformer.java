package com.lazhoff.mule.secureprops.crypto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import com.mulesoft.tools.SecurePropertiesTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Pattern;

public class JsonCryptoTransformer {

    private static final Logger logger = LogManager.getLogger(JsonCryptoTransformer.class);

    private final String algorithm;
    private final String mode;
    private final String key;
    private final boolean useRandomIV;
    private final boolean encrypt;
    private final Pattern secureAttributePattern;

    public JsonCryptoTransformer(
            String algorithm,
            String mode,
            String key,
            boolean useRandomIV,
            boolean encrypt,
            String secureAttributeNameRegex
    ) {
        this.algorithm = algorithm;
        this.mode = mode;
        this.key = key;
        this.useRandomIV = useRandomIV;
        this.encrypt = encrypt;
        this.secureAttributePattern = Pattern.compile(secureAttributeNameRegex);
    }

    public JsonNode transform(JsonNode node) {
        if (node instanceof ObjectNode objectNode) {
            ObjectNode copy = objectNode.deepCopy();
            objectNode.fieldNames().forEachRemaining(field -> {
                JsonNode value = objectNode.get(field);
                copy.set(field, transformWithKey(field, value));
            });
            return copy;

        } else if (node instanceof ArrayNode arrayNode) {
            ArrayNode copy = arrayNode.deepCopy();
            for (int i = 0; i < copy.size(); i++) {
                copy.set(i, transform(arrayNode.get(i)));
            }
            return copy;

        } else {
            return node; // number, boolean, null — untouched
        }
    }

    private JsonNode transformWithKey(String fieldName, JsonNode valueNode) {
        if (valueNode instanceof TextNode textNode) {
            String input = textNode.textValue();
            try {
                if (secureAttributePattern.matcher(fieldName).matches()) {
                    String result = encrypt ? encryptValue(input) : decryptValue(input);
                    return new TextNode(result);
                } else {
                    return textNode;
                }
            } catch (Exception e) {
                logger.warn("Failed to {} value '{}': {}", encrypt ? "encrypt" : "decrypt", input, e.getMessage());
                return textNode;
            }

        } else if (valueNode instanceof ObjectNode || valueNode instanceof ArrayNode) {
            return transform(valueNode); // continue recursion
        }

        return valueNode; // other primitives
    }


    private String encryptValue(String plainText) throws Exception {
        String encrypted = SecurePropertiesTool.applyOverString(
                "encrypt",
                algorithm,
                mode,
                key,
                useRandomIV,
                plainText
        );
        return "![" + encrypted + "]";
    }

    private String decryptValue(String input) throws Exception {
        if (input != null && input.startsWith("![") && input.endsWith("]")) {
            String encrypted = input.substring(2, input.length() - 1);
            return SecurePropertiesTool.applyOverString(
                    "decrypt",
                    algorithm,
                    mode,
                    key,
                    useRandomIV,
                    encrypted
            );
        } else {
            logger.debug("Skipping non-encrypted value: {}", input);
            return input;
        }
    }
}
