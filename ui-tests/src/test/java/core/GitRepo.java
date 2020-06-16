package core;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import org.jenkinsci.test.acceptance.docker.fixtures.GitContainer;

import org.zeroturnaround.zip.ZipUtil;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import static java.lang.ProcessBuilder.Redirect.*;
import static java.nio.file.attribute.PosixFilePermission.*;
import static java.util.Collections.*;
import static org.jenkinsci.test.acceptance.docker.fixtures.GitContainer.*;

/**
 * Manipulates git repository locally.
 *
 * @author Kohsuke Kawaguchi
 */
public class GitRepo implements Closeable {
    public final File dir;

    /**
     * Path to the script that acts like SSH.
     */
    private File ssh;

    /**
     * Private key file that contains /ssh_keys/unsafe.
     */
    private File privateKey;

    public GitRepo() {
        dir = initDir();
        git("init");
        setIdentity(dir);
    }

    public GitRepo(final String url) {
        dir = initDir();
        git("clone", url, ".");
        setIdentity(dir);
    }

    /**
     * Configures and identity for the repo, just in case global config is not set.
     */
    private void setIdentity(File dir) {
        gitDir(dir, "config", "user.name", "Jenkins-ATH");
        gitDir(dir, "config", "user.email", "jenkins-ath@example.org");
    }

    public void setIdentity(final String username, final String userMail) {
        gitDir(dir, "config", "user.name", username);
        gitDir(dir, "config", "user.email", userMail);
    }

    private File initDir() {
        try {
            // FIXME: perhaps this logic that makes it use a separate key should be moved elsewhere?
            privateKey = File.createTempFile("ssh", "key");
            FileUtils.copyURLToFile(GitContainer.class.getResource("GitContainer/unsafe"), privateKey);
            Files.setPosixFilePermissions(privateKey.toPath(), singleton(OWNER_READ));

            ssh = File.createTempFile("jenkins", "ssh");
            FileUtils.writeStringToFile(ssh,
                    "#!/bin/sh\n" +
                            "exec ssh -o StrictHostKeyChecking=no -i " + privateKey.getAbsolutePath() + " \"$@\"");
            Files.setPosixFilePermissions(ssh.toPath(), new HashSet<>(Arrays.asList(OWNER_READ, OWNER_EXECUTE)));

            return createTempDir("git");
        }
        catch (IOException e) {
            throw new AssertionError("Can't initialize git directory", e);
        }
    }

    public String git(Object... args) {
        return gitDir(this.dir, args);
    }

    public String gitDir(File dir, Object... args) {
        List<String> cmds = new ArrayList<>();
        cmds.add("git");
        for (Object a : args) {
            if (a != null) {
                cmds.add(a.toString());
            }
        }
        ProcessBuilder pb = new ProcessBuilder(cmds);
        pb.environment().put("GIT_SSH", ssh.getAbsolutePath());

        String errorMessage = cmds + " failed";
        try {
            Process p = pb.directory(dir)
                    .redirectInput(INHERIT)
                    .redirectError(INHERIT)
                    .start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }

            int r = p.waitFor();
            if (r != 0) {
                throw new AssertionError(errorMessage);
            }

            return builder.toString();

        }
        catch (InterruptedException | IOException e) {
            throw new AssertionError(errorMessage, e);
        }
    }

    /**
     * Appends the string "more" to the file "foo", adds it to the repository and commits it.
     *
     * @param message
     *         commit message
     */
    public void changeAndCommitFoo(final String message) {
        try {
            String fileName = "foo";
            try (FileWriter o = new FileWriter(new File(dir, fileName), true)) {
                o.write("more");
            }
            git("add", fileName);
            commit(message);
        }
        catch (IOException e) {
            throw new AssertionError("Can't append line to file foo", e);
        }
    }

    public void commitFileWithMessage(final String message, final String fileName, final String fileContent) {
        try {
            try (FileWriter o = new FileWriter(new File(dir, fileName), true)) {
                o.write(fileContent);
            }
            git("add", fileName);
            commit(message);
        }
        catch (IOException e) {
            throw new AssertionError("Can't append line to file foo", e);
        }
    }

    /**
     * Records all changes to the repository.
     *
     * @param message
     *         commit message
     */
    public void commit(final String message) {
        git("commit", "-m", message);
    }

    public void touch(final String fileName) {
        try {
            FileUtils.writeStringToFile(file(fileName), "");
        }
        catch (IOException e) {
            throw new AssertionError("Can't change file " + fileName, e);
        }
    }

    /**
     * Get sha1 hash of the most recent commit.
     *
     * @return Hash value
     */
    public String getLastSha1() {
        return git("rev-parse", "HEAD").trim();
    }

    public void checkout(String name) {
        git("checkout", name);
    }

    public File file(String name) {
        return new File(dir, name);
    }

    @Override
    public void close() throws IOException {
        FileUtils.deleteDirectory(dir);
        ssh.delete();
        privateKey.delete();
    }

    /**
     * Add a submodule to the main repository.
     *
     * @param submoduleName
     *         name of the submodule
     * @return gitRepo
     *         mocked git repo
     */
    public GitRepo addSubmodule(String submoduleName) {
        try {
            File submoduleDir = new File(createTempDir(submoduleName).getAbsolutePath() + "/" + submoduleName);
            submoduleDir.delete();
            submoduleDir.mkdir();

            gitDir(submoduleDir, "init");
            setIdentity(submoduleDir);
            try (FileWriter o = new FileWriter(new File(submoduleDir, "foo"), true)) {
                o.write("more");
            }

            gitDir(submoduleDir, "add", "foo");
            gitDir(submoduleDir, "commit", "-m", "Initial commit");

            git("submodule", "add", submoduleDir.getAbsolutePath());
            git("commit", "-am", "Added submodule");

            return this;
        }
        catch (IOException e) {
            throw new AssertionError("Can't create submodule " + submoduleName, e);
        }
    }

    private File createTempDir(String name) throws IOException {
        File tmp = File.createTempFile("jenkins", name);
        tmp.delete();
        tmp.mkdir();
        return tmp;
    }

    /**
     * Zip bare repository, copy to Docker container using sftp, then unzip. The repo is now accessible over
     * "ssh://git@ip:port/home/git/gitRepo.git"
     *
     * @param host
     *         IP of Docker container
     * @param port
     *         SSH port of Docker container
     */
    public void transferToDockerContainer(String host, int port) {
        try {
            Path zipPath = Files.createTempFile("git", "zip");
            File zippedRepo = zipPath.toFile();
            String zippedFilename = zipPath.getFileName().toString();
            ZipUtil.pack(new File(dir.getPath()), zippedRepo);

            Properties props = new Properties();
            props.put("StrictHostKeyChecking", "no");

            JSch jSch = new JSch();
            jSch.addIdentity(privateKey.getAbsolutePath());

            Session session = jSch.getSession("git", host, port);
            session.setConfig(props);
            session.connect();

            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            channel.cd("/home/git");
            channel.put(new FileInputStream(zippedRepo), zippedFilename);

            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            InputStream in = channelExec.getInputStream();
            channelExec.setCommand("unzip " + zippedFilename + " -d " + REPO_NAME);
            channelExec.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            int index = 0;
            while ((line = reader.readLine()) != null) {
                System.out.println(++index + " : " + line);
            }

            channelExec.disconnect();
            channel.disconnect();
            session.disconnect();
            // Files.delete(zipPath);
        }
        catch (IOException | JSchException | SftpException e) {
            throw new AssertionError("Can't transfer git repository to docker container", e);
        }
    }

    private Path path(Path path) {
        return dir.toPath().resolve(path);
    }

    /**
     * Copies all files of the specified folder to the root folder of this git repository and adds the copied files
     * using 'git add.
     *
     * @param sourceFolder
     *         the folder with the files to copy
     */
    public void addFilesIn(final String sourceFolder) {
        addFilesIn(getResource(sourceFolder));
    }

    private URL getResource(final String sourceFolder) {
        URL resource = getClass().getResource(sourceFolder);
        if (resource == null) {
            throw new IllegalArgumentException("No such directory: " + sourceFolder);
        }
        return resource;
    }

    /**
     * Copies all files of the specified directory to the {@code destinationFolder} of this git repository and adds the
     * copied files using git add.
     *
     * @param sourceFolder
     *         the folder with the files to copy
     * @param destinationFolder
     *         the destination folder for the copied files
     */
    public void addFilesIn(final String sourceFolder, final String destinationFolder) {
        addFilesIn(getResource(sourceFolder), Paths.get(destinationFolder));
    }

    /**
     * Copies all files of the specified folder to the root folder of this git repository and adds the copied files
     * using 'git add.
     *
     * @param sourceFolder
     *         the folder with the files to copy
     */
    public void addFilesIn(final URL sourceFolder) {
        addFilesIn(sourceFolder, dir.toPath());
    }

    /**
     * Copies all files of the specified directory to the {@code destinationFolder} of this git repository and adds the
     * copied files using git add.
     *
     * @param sourceFolder
     *         the folder with the files to copy
     * @param destinationFolder
     *         the destination folder for the copied files
     */
    public void addFilesIn(final URL sourceFolder, final Path destinationFolder) {
        Path gitPath;
        if (destinationFolder.isAbsolute()) {
            gitPath = destinationFolder;
        }
        else {
            gitPath = dir.toPath().resolve(destinationFolder);
        }
        try {
            Path source = Paths.get(sourceFolder.toURI());

            try (DirectoryStream<Path> paths = Files.newDirectoryStream(source, entry -> !Files.isDirectory(entry))) {
                for (Path path : paths) {
                    Files.copy(path, gitPath.resolve(path.getFileName()));
                }
            }
            git("add", "*");
        }
        catch (URISyntaxException | IOException e) {
            throw new AssertionError(String.format("Can't copy files from %s", sourceFolder), e);
        }
    }

    /**
     * Creates the specified branch in this repository.
     *
     * @param name
     *         the name of the branch
     */
    public void createBranch(final String name) {
        git("branch", name);
    }

    public Path mkdir(String path) {
        try {
            return Files.createDirectories(dir.toPath().resolve(path));
        }
        catch (IOException e) {
            throw new AssertionError(String.format("Can't created directories %s", path), e);
        }
    }
}
