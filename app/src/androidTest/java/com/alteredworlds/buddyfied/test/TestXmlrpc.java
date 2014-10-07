package com.alteredworlds.buddyfied.test;

import com.alteredworlds.buddyfied.Settings;
import com.alteredworlds.buddyfied.service.BuddyBackgroundService;

import java.net.MalformedURLException;

import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;

/**
 * Created by twcgilbert on 21/08/2014.
 */
public class TestXmlrpc extends UtilsTestCase {
    public static final String LOG_TAG = TestXmlrpc.class.getSimpleName();

    public void testXMLRPCClient() {
        String uri = Settings.getBuddySite(getContext()) + BuddyBackgroundService.BuddyXmlRpcRoot;
        try {
            XMLRPCClient client = new XMLRPCClient(uri);
            client.call(BuddyBackgroundService.VerifyConnection, "guest", "buddyfied");
        } catch (XMLRPCException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

}
