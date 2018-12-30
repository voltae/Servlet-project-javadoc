/**
 * Dieses SIP-ServletProjekt wurde aus folgenden Quellen <br>
 * <br>
 * - Boulton, Grownovski,2009, "Understanding SIP-Servlets 1.1"<br>
 * - Zuidweg, 2015, Creating Value-Added Applications<br>
 * - Denuelle et al, 2011, SIP-Servlet User Guide<br>
 * <br>
 * und eigene Ideen generiert:
 * <br>
 * In diesem Beispiel kommuniziert der User Agent Server (UAS) mittels Messages
 * Er antwortet mit 200 OK auf jedes erhaltene INVITE oder BYE
 *
 * @author F.Kopica  <-- Name des Studenten
 */
package com.mycompany.app;

import java.io.IOException;

//import java.util.ArrayList;
import java.util.HashMap;
//import java.util.List;

import javax.annotation.Resource;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import javax.servlet.sip.Address;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@SuppressWarnings("serial")
public class HelloSipWorld extends SipServlet {


    @Resource
    SipFactory sipFactory;

    private static Log logger = LogFactory.getLog(HelloSipWorld.class);
    HashMap<SipSession, SipSession> sessions = new HashMap<SipSession, SipSession>();
    HashMap<String, Address> registeredUsersToIp = new HashMap<String, Address>();


    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        // LOGGER
        super.init(servletConfig);
        System.out.println("init!");
        System.out.println("Hello Spi World has been initialized");
    }


    @Override
    protected void doInvite(SipServletRequest request) throws ServletException,
            IOException {
        request.getSession().setAttribute("lastRequest", request);
        if (logger.isInfoEnabled()) {
            logger.info("LOGGER: ...............................\n"
                    + request.getMethod());
        }

        SipServletRequest outRequest = sipFactory.createRequest(request.getApplicationSession(),
                "INVITE", request.getFrom().getURI(), request.getTo().getURI());
        String user = ((SipURI) request.getTo().getURI()).getUser();
        Address calleeAddress = registeredUsersToIp.get(user);
        if (calleeAddress == null) {
            request.createResponse(SipServletResponse.SC_NOT_FOUND).send();
            return;
        }
        outRequest.setRequestURI(calleeAddress.getURI());
        if (request.getContent() != null) {
            outRequest.setContent(request.getContent(), request.getContentType());
        }
        outRequest.send();
        sessions.put(request.getSession(), outRequest.getSession());
        sessions.put(outRequest.getSession(), request.getSession());
    }


    /**
     * Die <code>doResponse</code> ist das Gegenstück zur <code>doRequest</code> - Methode. Genau wie diese verteilt sie eingehende SIP primitive Methoden bei eingehenden
     * Nachrichten. Diese hängen vom Status der SIP Nachrichten ab. In dem Fall setzt die Methode das Attribut der response "lastResponse" mit der aktuellen Response.
     * <li><code>doProvisionalResponse</code> wird von der “doResponse” Methode aufgerufen, wenn der Status Code der eingehenden Antwort zwischen
     * 101 und 199 liegt. (Provisional) Vorläufige Statusinformationen, dass der Server weitere Aktionen durchführt und deshalb noch keine endgültige Antwort senden kann. </li>
     * <li><code>doSuccessResponse</code> wird von der “doResponse” Methode aufgerufen, wenn der Status Code der eingehenden Antwort zwischen
     * 200 to 299. (Successful) Anfrage war erfolgreich</li>
     * <li><code>doRedirectResponse</code> wird von der “doResponse” Methode aufgerufen, wenn der Status Code der eingehenden Antwort zwischen
     * 300 to 399. (Redirection) Diese Nachrichten informieren über eine neue Kontaktadresse des Angerufenen oder über andere Dienste, die es ermöglichen die Verbindung erfolgreich aufzubauen.</li>
     * <li><code>doErrorResponse</code> wird von der “doResponse” Methode aufgerufen, wenn der Status Code der eingehenden Antwort zwischen
     * 400 to 699.(Failure) 4xx sind Request Failures, 5xx sind Server Failures und 6xx sind Global Failures</li>
     * <li><code>doBranchResponse</code> wird für jede SPI Verzweigung einmal aufgerufen. Diese entsteht durch einen Proxy, der neue Prozesse generiert.</li>
     * @param response aktiver Respond (Objekt implementiert das Interface @see package javax.servlet.sip) SipServletResponse ist dem
     *                 (@see HttpServletResponse) sehr verwandt, es erlaubt den Zugriff auf die SIP Header und deren Veränderung.
     *                 Mittels der Methode "createResponse" läßt sich ein neuer response erstellen.
     * @throws ServletException generisches Fehlerobjekt der Klasse Exception, wird geworfen wenn ein genereller
     *      * Fehler in einem Servlet auftritt. @see <a href=https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/ServletException.html</a>
     * @throws IOException generisches Fehlerobjekt der Klasse Exceptions, wird bei einem generellen Input/ Output Fehler
     *                     geworfen.
     */
    protected void doResponse(SipServletResponse response)
            throws ServletException, IOException {
        if (logger.isInfoEnabled()) {
            logger.info("LOGGER: ........................\n" + response);
        }
        response.getSession().setAttribute("lastResponse", response);
        SipServletRequest request = (SipServletRequest) sessions.get(response.getSession()).getAttribute("lastRequest");
        SipServletResponse resp = request.createResponse(response.getStatus());
        if (response.getContent() != null) {
            resp.setContent(response.getContent(), response.getContentType());
        }
        resp.send();
    }


    /**
     * Die <code>doRegister</code> - Methode untersucht den SPI "Contact" Header und extrahiert aus dem Header die Adresse als URI. Dabei wird die Adresse zuerst
     * in die Form SipURI type-gecastet Bei SipURI handelt es sich um ein Protokoll, welches das Protokoll @see URI erweitert. Diese wird dann
     * einer Collection, in diesem Fall eine HashMap, die als Klassen-Property definiert wurde, hinzugefügt. Die Methode erstellt eine neue Response mit dem Statuscode
     * <strong>"200 -> OK"</strong> und versendet diese.
     * Die <code>doRegister</code> - Methode wird von der <code>doRequest</code> Methode aufgerufen, wenn diese ein SPI REGISTER primitive empfängt.
     * @param request aktiver Request (Objekt implementiert Interface @see package javax.servlet.sip). Die Klasse SipServletRequest
     *                ist der Klasse HttpServletRequest sehr ähnlich, die für Web-Applikationen verwendet werden. Die Klasse erlaubt einen Zugriff
     *                auf die Header der SIP Nachricht und erlaubt eine Änderung derselben.
     * @throws ServletException aktiver Request (Objekt das das Interface @see package javax.servlet.sip) implementiert.
     * @throws ServletException generisches Fehlerobjekt der Klasse Exception, wird geworfen wenn ein genereller
     *                          Fehler in einem Servlet auftritt. @see <a href=https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/ServletException.html</a>
     * @throws IOException generisches Fehlerobjekt der Klasse Exceptions, wird bei einem generellen Input/ Output Fehler
     *                     geworfen.
     */
    protected void doRegister(SipServletRequest request) throws ServletException,
            IOException {
        logger.info("LOGGER: .................................:\n"
                + request.toString());

        Address addr = request.getAddressHeader("Contact");
        SipURI sipUri = (SipURI) addr.getURI();
        registeredUsersToIp.put(sipUri.getUser(), addr);
        if (logger.isInfoEnabled()) {
            logger.info("Address registered " + addr);
        }
        SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
        sipServletResponse.send();
    }


    /**
     * Eine bestehende Verbindung, die mit einem SPI INVITE request begonnen wurde, wird mit BYE primitive
     * terminiert. Vergleichbar mit dem Einhängen des Telephonhörers bei analogen Verbinungen. Wichtig dabei
     * ist, der Methodencall BYE differiert vom primitiven Methodencall CANCEL. Dieser unterbricht die Verbindung
     * bevor die Verbindung endgültig zustande kam.
     * In diesem Fall erstellt die <code>doBye</code> - Methode eine neue SipServletResponse mit dem Statuscode "200 -> OK" und versendet
     * diese.
     * Die <code>doBye</code> - Methode wird von der <code>doRequest</code> Methode aufgerufen, sobald diese Methode ein SPI BY Primitive erhält,
     * wie im RFC 3261 definiert.
     * In diesem Fall erzeugt die Methdoe einen neue <code>Respond</code> mit einem SPI Status 200 -> OK. Diese wird dann versandt.
     * @param request aktiver Request (Objekt implementiert Interface @see package javax.servlet.sip). Die Klasse SipServletRequest
     *                ist der Klasse HttpServletRequest sehr ähnlich, die für Web-Applikationen verwendet werden. Die Klasse erlaubt einen Zugriff
     *                auf die Header der SIP Nachricht und erlaubt eine Änderung derselben.
     * @throws ServletException generisches Fehlerobjekt der Klasse Exception, wird geworfen wenn ein genereller
     * Fehler in einem Servlet auftritt. @see <a href=https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/ServletException.html</a>
     * @throws IOException generisches Fehlerobjekt der Klasse Exceptions, wird bei einem generellen Input/ Output Fehler
     *                     geworfen.
     */

    @Override
    protected void doBye(SipServletRequest request) throws ServletException,
            IOException {
        //LOGGER
        SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
        sipServletResponse.send();
    }
}
