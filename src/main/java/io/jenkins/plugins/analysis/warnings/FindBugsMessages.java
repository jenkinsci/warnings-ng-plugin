package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import edu.hm.hafner.analysis.SecureDigester;

/**
 * Parses the FindBugs pattern descriptions and provides access to these HTML messages.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.DataClass")
public final class FindBugsMessages {
    private static final String NO_MESSAGE_FOUND = "no message found";

    /** Maps a key to HTML description. */
    private final Map<String, String> messages = new HashMap<>();
    private final Map<String, String> jaMessages = new HashMap<>();
    private final Map<String, String> frMessages = new HashMap<>();
    private final Map<String, String> shortMessages = new HashMap<>();
    private final Map<String, String> jaShortMessages = new HashMap<>();
    private final Map<String, String> frShortMessages = new HashMap<>();

    /**
     * Initializes the messages map.
     */
    @SuppressWarnings("all")
    public void initialize() {
        try {
            loadMessages("messages.xml", messages, shortMessages);
            loadMessages("fb-contrib-messages.xml", messages, shortMessages);
            loadMessages("find-sec-bugs-messages.xml", messages, shortMessages);
            loadMessages("messages_fr.xml", frMessages, frShortMessages);
            loadMessages("messages_ja.xml", jaMessages, jaShortMessages);
        }
        catch (Exception ignored) {
            // ignore failures
        }
    }

    private void loadMessages(final String fileName, final Map<String, String> messagesCache,
            final Map<String, String> shortMessagesCache) throws IOException, SAXException {
        try (InputStream file = FindBugsMessages.class.getResourceAsStream("findbugs-messages/" + fileName)) {
            List<Pattern> patterns = parse(file);
            for (Pattern pattern : patterns) {
                messagesCache.put(pattern.getType(), pattern.getDescription());
                shortMessagesCache.put(pattern.getType(), pattern.getShortDescription());
            }
        }
    }

    /**
     * Parses the FindBugs pattern description.
     *
     * @param file
     *         XML file with the messages
     *
     * @return a list of parsed patterns
     * @throws SAXException
     *         if we can't parse the file
     * @throws IOException
     *         if we can't read the file
     */
    public List<Pattern> parse(final InputStream file) throws IOException, SAXException {
        SecureDigester digester = new SecureDigester(FindBugsMessages.class);
        List<Pattern> patterns = new ArrayList<>();
        digester.push(patterns);

        String startPattern = "*/BugPattern";
        digester.addObjectCreate(startPattern, Pattern.class);
        digester.addSetProperties(startPattern);
        digester.addCallMethod("*/BugPattern/Details", "setDescription", 0);
        digester.addCallMethod("*/BugPattern/ShortDescription", "setShortDescription", 0);
        digester.addSetNext(startPattern, "add");

        digester.parse(file);

        return patterns;
    }

    /**
     * Returns a HTML description for the specified bug.
     *
     * @param name
     *         name of the bug
     * @param locale
     *         the locale of the user
     *
     * @return a HTML description
     */
    public String getMessage(final String name, final Locale locale) {
        String localizedMessage = getLocalizedMessage(name, locale, messages, jaMessages, frMessages);
        return StringUtils.defaultIfEmpty(localizedMessage, NO_MESSAGE_FOUND);
    }

    /**
     * Returns a short description for the specified bug.
     *
     * @param name
     *         name of the bug
     * @param locale
     *         the locale of the user
     *
     * @return a HTML description for the specified bug.
     */
    public String getShortMessage(final String name, final Locale locale) {
        String localizedMessage = getLocalizedMessage(name, locale, shortMessages, jaShortMessages, frShortMessages);
        return StringUtils.defaultIfEmpty(localizedMessage, NO_MESSAGE_FOUND);
    }

    private String getLocalizedMessage(final String name, final Locale locale,
            final Map<String, String> en, final Map<String, String> ja, final Map<String, String> fr) {
        String country = locale.getLanguage();
        String localizedMessage;
        if ("ja".equalsIgnoreCase(country)) {
            localizedMessage = ja.get(name);
        }
        else if ("fr".equalsIgnoreCase(country)) {
            localizedMessage = fr.get(name);
        }
        else {
            localizedMessage = en.get(name);
        }
        return localizedMessage;
    }

    /**
     * Returns the size of this messages cache.
     *
     * @return the number of stored messages (English locale)
     */
    public int size() {
        return messages.size();
    }

    /**
     * Bug pattern describing a bug type.
     *
     * @author Ullrich Hafner
     */
    public static class Pattern {
        private String type;
        private String description;
        private String shortDescription;

        /**
         * Returns the type.
         *
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the type to the specified value.
         *
         * @param type
         *         the value to set
         */
        public void setType(final String type) {
            this.type = type;
        }

        /**
         * Returns the description.
         *
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Sets the description to the specified value.
         *
         * @param description
         *         the value to set
         */
        public void setDescription(final String description) {
            this.description = description;
        }

        /**
         * Returns the shortDescription.
         *
         * @return the shortDescription
         */
        public String getShortDescription() {
            return shortDescription;
        }

        /**
         * Sets the shortDescription to the specified value.
         *
         * @param shortDescription
         *         the value to set
         */
        public void setShortDescription(final String shortDescription) {
            this.shortDescription = shortDescription;
        }
    }
}

