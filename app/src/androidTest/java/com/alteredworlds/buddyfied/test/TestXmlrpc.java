package com.alteredworlds.buddyfied.test;

import com.alteredworlds.buddyfied.Settings;
import com.alteredworlds.buddyfied.service.BuddyQueryService;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

/**
 * Created by twcgilbert on 21/08/2014.
 */
public class TestXmlrpc extends UtilsTestCase {
    public static final String LOG_TAG = TestXmlrpc.class.getSimpleName();

    public void testXMLRPCClient() {
        String uri = Settings.getBuddySite(getContext()) + BuddyQueryService.BuddyXmlRpcRoot;
        XMLRPCClient client = new XMLRPCClient(uri);
        try {
            client.call(BuddyQueryService.VerifyConnection, "guest", "buddyfied");
        } catch (XMLRPCException e) {
            e.printStackTrace();
        }
    }

}
