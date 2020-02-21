package io.jenkins.plugins.analysis.core.restapi;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import io.jenkins.plugins.forensics.blame.FileBlame;

/**
 * Remote API for a {@link FileBlame}. Simple Java Bean that exposes several methods of an {@link FileBlame} instance.
 *
 * @author Kezhi Xiong
 */
@ExportedBean
public class BlameApi {
    private FileBlame fileBlame;
    private int line;

    /**
     * Creates a new {@link IssueApi}.
     *
     * @param fileBlame
     *         the file blame to expose the properties from
     * @param line
     *         the line number of the blame in this file
     */
    public BlameApi(final FileBlame fileBlame, final int line) {
        this.fileBlame = fileBlame;
        this.line = line;
    }

    @Exported
    public int getLine() {
        return line;
    }

    @Exported
    public String getAuthor() {
        return fileBlame.getName(line);
    }

    @Exported
    public String getEmail() {
        return fileBlame.getEmail(line);
    }

    @Exported
    public String getCommit() {
        return fileBlame.getCommit(line);
    }

    @Exported
    public int getTime() {
        return fileBlame.getTime(line);
    }
}
