package de.fhkn.in.uce.master.server.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import de.fhkn.in.uce.mediator.Mediator;
import de.fhkn.in.uce.relaying.server.RelayServer;
import de.fhkn.in.uce.stun.server.StunServer;

/**
 * Abstract Class which holds common functions needed by {@link FilePropertyReader}, {@link CmdReader} and {@link SystemPropertyReader}.
 *
 * @author Robert Danczak
 */
public abstract class AbstractReader {

    protected final Pattern ipPattern = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

    protected final Logger logger;

    // possible args.
    public static final String stunFirstIP = "StunFirstIP";
    public static final String stunSecondIP = "StunSecondIP";
    public static final String relayPort = "RelayPort";
    public static final String mediatorPort = "MediatorPort";
    public static final String mediatorIteration = "MediatorIteration";
    public static final String mediatorLifeTime = "MediatorLifeTime";

    public AbstractReader(Logger logger) {
        this.logger = logger;
    }

    /**
     * Abstract method for parsing arguments to {@link Mediator},
     * {@link StunServer} and {@link RelayServer}.
     *
     * @param stunArgs
     *            Arguments for the stun server.
     * @param relayArgs
     *            Arguments for the relay server.
     * @param mediatorArgs
     *            Arguments for the mediator server.
     */
    public abstract void readArguments(List<String> stunArgs, List<String> relayArgs, List<String> mediatorArgs);

    protected void logInfo(String msg) {
        System.out.println(msg);
        logger.info(msg);
    }

    protected boolean isIP(String toCheck) {
        toCheck = toCheck.trim();
        Matcher m = ipPattern.matcher(toCheck);
        return m.matches();
    }

    protected boolean isPort(String port) {
        port = port.trim();
        int result = Integer.parseInt(port);
        if ((result >= 1024) && (result < 65536)) {
            return true;
        }
        return false;
    }

    protected void processMediatorLifetime(List<String> mediatorArgs, String arg) {
        String[] splitted = arg.split(mediatorLifeTime + "=");
        String result = splitted[1];
        logInfo("added max lifetime \"" + result + "\" to mediator arguments");
        mediatorArgs.set(2, result);
    }

    protected void processMediatorIteration(List<String> mediatorArgs, String arg) {
        String[] splitted = arg.split(mediatorIteration + "=");
        String result = splitted[1];
        logInfo("added iteration time \"" + result + "\" to mediator arguments");
        mediatorArgs.set(1, result);
    }

    protected void processMediatorPort(List<String> mediatorArgs, String arg) {
        String[] splitted = arg.split(mediatorPort + "=");
        String result = splitted[1];
        if (!isPort(result)) {
            throw new IllegalArgumentException();
        }
        logInfo("added port \"" + result + "\" to mediator arguments");
        mediatorArgs.set(0, result);
    }

    protected void processRelayPort(List<String> relayArgs, String arg) {
        String[] splitted = arg.split(relayPort + "=");
        String result = splitted[1];
        if (!isPort(result)) {
            throw new IllegalArgumentException();
        }
        logInfo("added port \"" + result + "\" to relay arguments");
        relayArgs.set(0, result);
    }

    protected void processStunSecondIP(List<String> stunArgs, String arg) {
        String[] splitted = arg.split(stunSecondIP + "=");
        String result = splitted[1];
        if (!isIP(result)) {
            throw new IllegalArgumentException();
        }
        logInfo("added second IP \"" + result + "\" to stun arguments");
        stunArgs.set(1, result);
    }

    protected void processStunFirstIP(List<String> stunArgs, String arg) {
        String[] splitted = arg.split(stunFirstIP + "=");
        String result = splitted[1];
        if (!isIP(result)) {
            throw new IllegalArgumentException();
        }
        logInfo("added first IP \"" + result + "\" to stun arguments");
        stunArgs.set(0, result);
    }
}
