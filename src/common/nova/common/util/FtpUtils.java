package nova.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import sun.net.TelnetInputStream;
import sun.net.ftp.FtpClient;

/**
 * Helper functions for ftp connection.
 * 
 * @author santa
 * 
 */
public class FtpUtils {

    /**
     * Log4j logger.
     */
    static Logger logger = Logger.getLogger(FtpUtils.class);

    /**
     * Connect to an ftp server.
     * 
     * @param host
     *            Ftp server host address.
     * @param port
     *            Ftp server port.
     * @param username
     *            User name.
     * @param password
     *            User password.
     * @return An {@link sun.net.ftp.FtpClient} object.
     * @throws IOException
     *             If connection fails.
     */
    public static FtpClient connect(String host, int port, String username,
            String password) throws IOException {
        try {
            FtpClient fc = new FtpClient(host, port);
            fc.openServer(host, port);
            fc.login(username, password);

            return fc;
        } catch (IOException e) {
            // log error & re-throw
            logger.error("Failed to connect to ftp server @" + host + ":"
                    + port, e);
            throw e;
        }
    }

    /**
     * Connect to an ftp server, as anonymous user.
     * 
     * @param host
     *            Ftp server host address.
     * @param port
     *            Ftp server port.
     * @return An {@link sun.net.ftp.FtpClient} object.
     * @throws IOException
     *             If connection fails.
     */
    public static FtpClient connect(String host, int port) throws IOException {
        return FtpUtils.connect(host, port, "anonymous",
                "nova-develop@googlegroups.com");
    }

    /**
     * Download a file from remote site. The file must in cwd.
     * 
     * @param fClient
     *            The ftp client.
     * @param remoteFileName
     *            Remote side filename.
     * @param localPath
     *            Local file path, could be absolute path. New dirs will be made
     *            if necessary.
     * @param cancelFlag
     *            A cancel flag to indicate whether need to quit downloading.
     * @throws IOException
     *             If connection failed.
     */
    private static void downloadFileInCwd(FtpClient fClient,
            String remoteFileName, String localPath, Cancellable cancelFlag)
            throws IOException {
        fClient.binary();
        TelnetInputStream is = fClient.get(remoteFileName);

        File localFile = new File(localPath);

        Utils.mkdirs(localFile.getParentFile().getAbsolutePath());
        FileOutputStream os = new FileOutputStream(localFile);
        final int bufSize = 32 * 1024;
        byte[] bytes = new byte[bufSize];
        int c = -1;
        while ((c = is.read(bytes)) != -1) {
            os.write(bytes, 0, c);
            if (shouldStop(cancelFlag)) {
                break;
            }
        }
        is.close();
        os.close();
    }

    /**
     * Download a file from remote site. It behaves like "mv" cmd.
     * 
     * @param fClient
     *            The ftp client.
     * @param remotePath
     *            Remote side path, could be absolute path. The ftp cwd won't be
     *            changed.
     * @param localPath
     *            Local file path, could be absolute path. New dirs will be made
     *            if necessary.
     * @param cancelFlag
     *            A cancel flag to indicate whether need to quit downloading.
     * @throws IOException
     *             If connection failed.
     */
    public static void downloadFile(FtpClient fClient, String remotePath,
            String localPath, Cancellable cancelFlag) throws IOException {
        String[] splt = Utils.pathSplit(remotePath);
        String oldCwd = fClient.pwd();
        fClient.cd(splt[0]);
        FtpUtils.downloadFileInCwd(fClient, splt[1], localPath, cancelFlag);
        // get back to old working dir
        fClient.cd(oldCwd);
    }

    public static void downloadFile(FtpClient fClient, String remotePath,
            String localPath) throws IOException {
        FtpUtils.downloadFile(fClient, remotePath, localPath, null);
    }

    public static void downloadDir(FtpClient fc, String remotePath,
            String localPath, Cancellable cancelFlag) throws IOException {
        if (shouldStop(cancelFlag) == false) {
            String oldCwd = fc.pwd();
            fc.cd(remotePath);
            Utils.mkdirs(localPath);

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    fc.list()));

            String ftpEntry = null;
            while ((ftpEntry = br.readLine()) != null
                    && shouldStop(cancelFlag) == false) {
                String entry = ftpEntry.substring(nthFieldStart(ftpEntry, 8));
                if ((nthField(ftpEntry, 0)).startsWith("d")) {
                    // d is directory
                    FtpUtils.downloadDir(fc, remotePath + "/" + entry,
                            Utils.pathJoin(localPath, entry), cancelFlag);
                } else {
                    FtpUtils.downloadFileInCwd(fc, entry,
                            Utils.pathJoin(localPath, entry), cancelFlag);
                }
            }
            br.close();

            fc.cd(oldCwd);
        }
    }

    private static boolean shouldStop(Cancellable cancelFlag) {
        if (cancelFlag != null) {
            return cancelFlag.isCancelled();
        }
        return false;
    }

    public static void downloadDir(FtpClient fc, String remotePath,
            String localPath) throws IOException {
        FtpUtils.downloadDir(fc, remotePath, localPath, null);
    }

    /**
     * Check if a char is whitespace.
     * 
     * @param ch
     * @return
     */
    private static boolean isWhiteSpace(byte ch) {
        return ch == ' ' || ch == '\t' || ch == '\n';
    }

    /**
     * Get the n-th field in an ftp file entry.
     * 
     * @param ftpItem
     * @param nth
     * @return
     */
    private static String nthField(String ftpItem, int nth) {
        int start = nthFieldStart(ftpItem, nth);
        int stop = nthFieldStop(ftpItem, nth);
        return ftpItem.substring(start, stop);
    }

    /**
     * Get the start offset of the n-th field in an ftp entry.
     * 
     * @param ftpItem
     * @param nth
     * @return
     */
    public static int nthFieldStart(String ftpItem, int nth) {
        int currentFieldId = -1;
        int idx = 0;
        boolean inWhiteSpace = true;
        byte[] ftpItemBytes = ftpItem.getBytes();
        for (;;) {
            byte ch = ftpItemBytes[idx];
            if (inWhiteSpace == true) {
                if (isWhiteSpace(ch)) {
                    // do nothing, pass along
                } else {
                    inWhiteSpace = false;
                    currentFieldId++;
                    if (currentFieldId >= nth)
                        break;
                }
            } else {
                if (isWhiteSpace(ch)) {
                    inWhiteSpace = true;
                } else {
                    // do nothing, pass along
                }
            }
            idx++;
        }
        return idx;
    }

    /**
     * Get the stop offset (surpass end) of the n-th field in an ftp entry.
     * 
     * @param ftpItem
     * @param nth
     * @return
     */
    public static int nthFieldStop(String ftpItem, int nth) {
        byte[] ftpItemBytes = ftpItem.getBytes();
        int idx = nthFieldStart(ftpItem, nth);
        for (;;) {
            if (idx >= ftpItemBytes.length)
                break;
            byte ch = ftpItemBytes[idx];
            if (isWhiteSpace(ch)) {
                break;
            }
            idx++;
        }
        return idx;
    }

}
