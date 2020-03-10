package io.jenkins.plugins.analysis.warnings;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Wraps a resource found by {@link Class#getResource(String)}.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings("all")
@SuppressFBWarnings("DM")
public class Resource {
    public final URL url;

    public Resource(URL url) {
        this.url = url;
    }

    /**
     * Gets just the file name portion without any paths, like "foo.txt"
     */
    public String getName() {
        String s = url.toExternalForm();
        return s.substring(s.lastIndexOf('/')+1);
    }

    public InputStream asInputStream() {
        try {
            return url.openStream();
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public Reader asReader() {
        try {
            return new InputStreamReader(url.openStream());
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public byte[] asByteArray() throws IOException {
        try (InputStream is = asInputStream()) {
            return IOUtils.toByteArray(is);
        }
    }

    public File asFile() {
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new IOError(e);
        }
    }
}
