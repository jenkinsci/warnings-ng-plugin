package io.jenkins.plugins.analysis.core.scm.analyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {

    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    public static List<Path> getFilesInRepository(Git git) throws IOException {
        List<Path> files = Files.walk(Paths.get(git.getRepository().getWorkTree().getPath()))
                .filter(path -> Files.isRegularFile(path))
                .filter(path -> path.toString().contains(".java"))
                .collect(Collectors.toList());
        return files;
    }

    public static String getFileRelative(Git git, Path path) throws IOException {
        return new File(git.getRepository().getWorkTree().getAbsolutePath()).toURI()
                .relativize(new File(path.toString()).toURI()).getPath();
    }
}
