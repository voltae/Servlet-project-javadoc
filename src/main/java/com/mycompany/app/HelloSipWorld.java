/**
 * Dieses SIP-ServletProjekt wurde aus folgenden Quellen <br>
 * <br>
 *    - Boulton, Grownovski,2009, "Understanding SIP-Servlets 1.1"<br>
 *    - Zuidweg, 2015, Creating Value-Added Applications<br>
 *    - Denuelle et al, 2011, SIP-Servlet User Guide<br>
 * <br>
 * und eigene Ideen generiert:
 * <br>
 * In diesem Beispiel kommuniziert der User Agent Server (UAS) mittels Messages
 * Er antwortet mit 200 OK auf jedes erhaltene INVITE oder BYE
 *
 * @author Alexander Feldinger
 * @author Valentin Platzgummer
 * @version 1.1
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


    /**
     * Die <code>SipFactory</code> ist ein Interface, das verwendet wird um neue, verf&uuml;gbare, Instanzen der SIP Servlet API zu erstellen. Vorausgesetzt wird der Import der javax.servlet.sip.SipFactory.
     * Das Interface SipFactory erm&ouml;glicht das Nutzen der darin enthaltenen Methoden.
     */

    @Resource
    SipFactory sipFactory;

    /**
     * Der <code>logger</code> kann zum Loggen von verschiedenen Java-Applikationen verwendet werden. Die Interface <code>LogFactory</code> stellt die Instanz zur Verf&uuml;gung.
     */

    //Erstellt ein Objekt zum Loggen der SIP-Sitzung
    private static Log logger = LogFactory.getLog(HelloSipWorld.class);
    //Erstellt eine neue leere HashMap (key/value) f&uuml;rs Logging der aktuellen SIP-Sitzung (Sender ID)
    HashMap<SipSession, SipSession> sessions= new HashMap<SipSession, SipSession>();
    //Erstellt eine neue leere HashMap (key/value) f&uuml;r die aktuelle SIP-Sitzung zum Logging der Usernamen und IP-Addressen
    HashMap<String, Address> registeredUsersToIp = new HashMap<String, Address>();


    /**
     * Die <code>init</code> Methode wird aufgerufen, um dem Servlet anzuzeigen, dass es in Verwendung genommen wird. Der Servlet-Container ruft die <code>init</code> Methode genau einmal auf, nachdem das Servlet instaniiert wurde.
     * Die <code>init</code> Methode muss erfolgreich aufgerufen worden sein, bevor das Servlet Requests entgegen nehmen kann.
     * Der Servlet-Container kann das Servlet nicht verwenden, wenn eine ServletException ausgel&ouml;st wird, oder die <code>init</code> Methode nicht innerhalb einer vom Webserver definierten Zeit reagiert bzw zur&uuml;ckkehrt.
     * @param servletConfig Konfigurationsobjekt, welches Informationen enth&auml;lt, die dem Servlet w&auml;hrend dem Aufruf &uuml;bergeben werden.
     * @throws ServletException generisches Fehlerobjekt der Klasse Exception, wird geworfen wenn ein genereller
     * Fehler in einem Servlet auftritt. @see <a href="https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/ServletException.html">Servlett Exception</a>
     */

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        if(logger.isInfoEnabled()) {
            logger.info("###############LOGGER: SERVLETS WERDEN INITIALISERT\n#####################");
            System.out.println("#################CONSOLE: SERVLETS WERDEN INITIALISERT\n###################");
        }
        //speichert das Konfigurationsobjekt des Servlets-Containers
        super.init(servletConfig);
    }

    /**
     * Die <code>doInvite</code> Methode wird zum Initiieren und Aktualisieren einer Multimediasitzung mit einem anderen Client unter Verwendung des SIP verwendet und verarbeitet eingehende Anfragen. 
     * Die eingehende INVITE-Anforderung wird vom Servlet-Container mit der Methode <code>doInvite</code> an die Anwendung &uuml;bergeben.
     * Die Anwendung erstellt mithilfe des Objekts <code>SipServlet Request</code>ein neues SIP-Antwortobjekt, indem sie die Methode <code>createResponse</code> f&uuml;r das Interface <code>SipServletRequest</code> aufruft.
     * Die Anwendung &uuml;bergibt der Methode ein "200" als Parameter, was dazu f&uuml;hrt, dass eine SIP 200-Antwort generiert wird. Die Zahl wird andere Antworten entsprechend ge&auml;ndert.
     * @param request aktive Requestmessage 
     * @throws ServletException generisches Fehlerobjekt der Klasse Exception, wird geworfen wenn ein genereller
     * Fehler in einem Servlet auftritt. @see <a href="https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/ServletException.html">Servlett Exception</a>
     * @throws IOException generisches Fehlerobjekt der Klasse Exceptions, wird bei einem generellen Input/ Output Fehler
     * geworfen.
     */

    @Override
    protected void doInvite(SipServletRequest request) throws ServletException,
            IOException {
        // request Objekt, welches das Interface SipServletRequest implementiert. Die Methode getSession holt sich die Session, zu der die Anfrage geh&ouml;rt.
        // Das Feld der Hashmap lastRequest wird mit der eingehenden Request gesetzt. Damit wird sichergestellt, da&szlig; lastRequest immer die letzte Anfrage ist.
        request.getSession().setAttribute("lastRequest", request);
        if(logger.isInfoEnabled()) {
            logger.info("#################LOGGER: Initiiere/Update Multimedia Session\n###############" + request.getMethod());
            System.out.println("#################CONSOLE: Initiiere/Update Multimedia Session\n###############" + request.getMethod());
        }
        //erstellt ein neues Objekt des SipServletRequest Interface f&uuml;r die neue SIP-Session und bef&uuml;llt den "From" und "To" Header.
        SipServletRequest outRequest = sipFactory.createRequest(request.getApplicationSession(),
                "INVITE", request.getFrom().getURI(), request.getTo().getURI());
        //weist dem User die SIP spezifischen Informationen (To-Header, URI zur Addressierung und dem Userteil der SipURI) zu
        String user = ((SipURI) request.getTo().getURI()).getUser();
        //Sucht die IP-Adresse des Users und speichert diese
        Address calleeAddress = registeredUsersToIp.get(user);
        //Sollte die Addresse des Users nicht gefunden werden (Fehlermeldung "404"), wird eine entsprechende Nachricht gesendet und die Anwendung wird beendet
        if(calleeAddress == null) {
            request.createResponse(SipServletResponse.SC_NOT_FOUND).send();
            return;
        }
        //Weist der Request die URI (Uniform Resource Identifier) des Anfragestellers zu
        outRequest.setRequestURI(calleeAddress.getURI());
        //Gibt den Inhalt der Anfrage als Javaobjekt zur&uuml;ck
        //Der Typ des Objekts h&auml;ngt vom MIME Type des Inhalts ab
        if(request.getContent() != null) {
            outRequest.setContent(request.getContent(), request.getContentType());
        }
        //dieser Request wird gesendet
        outRequest.send();
        //speichert Anfrage und Antwort
        sessions.put(request.getSession(), outRequest.getSession());
        sessions.put(outRequest.getSession(), request.getSession());
    }


    /**
     * Die <code>doResponse</code> ist das Gegenst&uuml;ck zur <code>doRequest</code> - Methode. Genau wie diese verteilt sie eingehende SIP primitive Methoden bei eingehenden
     * Nachrichten. Diese h&auml;ngen vom Status der SIP Nachrichten ab. In dem Fall setzt die Methode das Attribut der response "lastResponse" mit der aktuellen Response.
     * <code>doProvisionalResponse</code> wird von der doResponse Methode aufgerufen, wenn der Status Code der eingehenden Antwort zwischen
     * 101 und 199 liegt. (Provisional) Vorl&auml;ufige Statusinformationen, dass der Server weitere Aktionen durchf&uuml;hrt und deshalb noch keine endg&uuml;ltige Antwort senden kann.
     * <code>doSuccessResponse</code> wird von der doResponse Methode aufgerufen, wenn der Status Code der eingehenden Antwort zwischen
     * 200 to 299. (Successful) Anfrage war erfolgreich
     * <code>doRedirectResponse</code> wird von der doResponse Methode aufgerufen, wenn der Status Code der eingehenden Antwort zwischen
     * 300 to 399. (Redirection) Diese Nachrichten informieren &uuml;ber eine neue Kontaktadresse des Angerufenen oder &uuml;ber andere Dienste, die es erm&ouml;glichen die Verbindung erfolgreich aufzubauen.
     * <code>doErrorResponse</code> wird von der doResponse Methode aufgerufen, wenn der Status Code der eingehenden Antwort zwischen
     * 400 to 699.(Failure) 4xx sind Request Failures, 5xx sind Server Failures und 6xx sind Global Failures
     * <code>doBranchResponse</code> wird f&uuml;r jede SPI Verzweigung einmal aufgerufen. Diese entsteht durch einen Proxy, der neue Prozesse generiert.
     * @param response aktiver Respond (Objekt implementiert das Interface @see package javax.servlet.sip) SipServletResponse ist der
     * (@see HttpServletResponse) sehr verwandt, es erlaubt den Zugriff auf die SIP Header und deren Ver&auml;nderung.
     * Mittels der Methode "createResponse" l&auml;&szlig;t sich ein neuer response erstellen.
     * @throws ServletException generisches Fehlerobjekt der Klasse Exception, wird geworfen wenn ein genereller
     * Fehler in einem Servlet auftritt. @see <a href="https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/ServletException.html">Servlett Exception</a>
     * @throws IOException generisches Fehlerobjekt der Klasse Exceptions, wird bei einem generellen Input/ Output Fehler
     * geworfen.
     */

    protected void doResponse(SipServletResponse response)
            throws ServletException, IOException {
        if(logger.isInfoEnabled()) {
            logger.info("###############LOGGER: Antwort wird &uuml;bermittelt\n###################" + response);
            System.out.println("###############CONSOLE: Antwort wird &uuml;bermittelt\n###################" + response);
        }
        // response Objekt das Interface SipServletResponse implementiert. Die methode getSession holt sich die Session, zu der die Nachricht geh&ouml;rt. Dise ist hier
        // Das Feld der Hashmap lastResponse wird mit der eingehenden Response gesetzt. Damit wird sichergestellt, da&szlig; lastRespose immer die letzte Antwort ist.
        response.getSession().setAttribute("lastResponse", response);
        // ein neuer ServletRequet wird erstellt. dazu wir aus dem Klassenproperty "session" (Hashmap) Erl&auml;uerung siehe oben, das Attribut
        // "lastRequest" extrahiert und typegecasted dem neu erstellten request &uuml;bergeben. Die Methode getSession returniert die Session zu welcher
        // die Nachricht, hier in dem Fall die letzte Nachricht geh&ouml;rt, anhand eines Integers. Damit wird aus der eingehenden response die letzte Anfrage 'lastRequest'
        // geholt. request ist damit die letzte Anfrage
        SipServletRequest request = (SipServletRequest) sessions.get(response.getSession()).getAttribute("lastRequest");
        // Ein neuer response wird erzeugt. Der Wert des Status (integer @see public interface SipServletResponse) des response ist dabei der Parameter
        // der Methode createResponse. Die Methode createResponse erzeugt aus dem request eine neue response mit dem Statuscode.
        SipServletResponse resp = request.createResponse(response.getStatus());
        // die Methode getContent retourniert den Inhalt als Java Objekt. Es h&auml;ngt davon ab, welcher MIME Typ das Objekt hat.
        // Hier wird untersucht, ob der Inhalt &uuml;berhaupt exisitiert.
        if(response.getContent() != null) {
            // falls ja wird der Content typ des neu erstellten response headers gleich wie der Content type der eingehenden response gesetzt.
            resp.setContent(response.getContent(), response.getContentType());
        }
        // die neu erstellte response wird versandt.
        resp.send();
    }

    /**
     * Die <code>doRegister</code> - Methode untersucht den SPI "Contact" Header und extrahiert aus dem Header die Adresse als UIR. Diese wird dann
     * einer Collction, in diesem Fall eine HashMap, die als Klassen-Property definiert wurde, hinzugef&uuml;gt. Die Methode erstellt eine neue
     * Response mit dem Statuscode <strong>"200 OK"</strong> und versendet diese.
     * Die <code>doRegister</code> - Methode wird von der <code>doRequest</code> Methode aufgerufen, wenn diese ein SPI REGISTER primitive empf&auml;ngt.
     * @param request aktiver Request (Objekt implementiert Interface @see package javax.servlet.sip). Die Klasse SipServletRequest
     * ist der Klasse HttpServletRequest sehr &auml;hnlich, die f&uuml;r Web-Applikationen verwendet werden. Die Klasse erlaubt einen Zugriff
     * auf die Header der SIP Nachricht und erlaubt eine &auml;nderung derselben.
     * @throws ServletException aktiver Request (Objekt das das Interface @see package javax.servlet.sip) implementiert.
     * @throws ServletException generisches Fehlerobjekt der Klasse Exception, wird geworfen wenn ein genereller
     * Fehler in einem Servlet auftritt. @see <a href="https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/ServletException.html">Servlett Exception</a>
     * @throws IOException generisches Fehlerobjekt der Klasse Exceptions, wird bei einem generellen Input/ Output Fehler
     * geworfen.
     */

    protected void doRegister(SipServletRequest request) throws ServletException,
            IOException {
        if(logger.isInfoEnabled()) {
            logger.info("###############LOGGER: Es wurde ein Request eines Users empfangen ##############\n" + request.toString());
            System.out.println("##########CONSOLE: Es wurde ein Request eines Users empfangen ####################\n" + request.toString());
        }
        // Extrahieren aus dem eingehenden request das Property mit Namen "contact"
        Address addr = request.getAddressHeader("Contact");
        // Type casten der retournierten URI-Adresse in das Format SpiURI (Unterklasse von URI)
        SipURI sipUri = (SipURI) addr.getURI();
        // Hinzuf&uuml;gen der URI zur KlassenHashmap Key ist der im Header gespeicherte Name, value die Adresse als URI
        registeredUsersToIp.put(sipUri.getUser(), addr);
        if(logger.isInfoEnabled()) {
            logger.info("################Address registered########################\n" + addr);
            System.out.println("##########CONSOLE: Addresse registriert ####################\n" + addr);
        }
        // Erstellen einer neuen SipServlet Response. Diese wird mit dem exit code "200 - OK" versehen. D.h. jee 'Register' Message wird mit '20' -'OK' best&auml;tigt.
        SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
        // neu erstellte Antwort wird versandt
        sipServletResponse.send();
    }


    /**
     * Eine bestehende Verbindung, die mit einem SPI INVITE request begonnen wurde, wird mit BYE primitive
     * terminiert. Vergleichbar mit dem Einh&auml;ngen des Telephonh&ouml;rers bei analogen Verbinungen. Wichtig dabei
     * ist, der Methodencall BYE differiert vom primitiven Methodencall CANCEL. Dieser unterbricht die Verbindung
     * bevor die Verbindung endg&uuml;ltig zustande kam.
     * In diesem Fall erstellt die <code>doBye</code> - Methode eine neue SipServletResponse mit dem Statuscode "200 OK" und versendet
     * diese.
     * Die <code>doBye</code> - Methode wird von der <code>doRequest</code> Methode aufgerufen, sobald diese Methode ein SPI BY Primitive erh&auml;lt,
     * wie im RFC 3261 definiert.
     * In diesem Fall erzeugt die Methdoe einen neue <code>Respond</code> mit einem SPI Status 200 OK. Diese wird dann versandt.
     * @param request aktiver Request (Objekt implementiert Interface @see package javax.servlet.sip). Die Klasse SipServletRequest
     * ist der Klasse HttpServletRequest sehr &auml;hnlich, die f&uuml;r Web-Applikationen verwendet werden. Die Klasse erlaubt einen Zugriff
     * auf die Header der SIP Nachricht und erlaubt eine &auml;nderung derselben.
     * @throws ServletException generisches Fehlerobjekt der Klasse Exception, wird geworfen wenn ein genereller
     * Fehler in einem Servlet auftritt. @see <a href="https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/ServletException.html">Servlett Exception</a>
     * @throws IOException generisches Fehlerobjekt der Klasse Exceptions, wird bei einem generellen Input/ Output Fehler
     * geworfen.
     */

    @Override
    protected void doBye(SipServletRequest request) throws ServletException,
            IOException {
        //LOGGER
        // Erstellen einer neuen SipServlet Response. Diese wird mit dem exit code "200 - OK" versehen. Das hei&szlig;t alle 'Bye'
        // nachrichten erhalten Statuscode '200'
        if(logger.isInfoEnabled()) {
            logger.info("###################LOGGER: Session wird beendet:\n################" + request.toString());
            System.out.println("###############CONSOLE: Session wird beendet\n###################" + request.toString());
        }
        SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
        // neu erstellte Antwort wird versandt
        sipServletResponse.send();
    }


    /**
     * Die <code>doAck</code> - Methode wird vom Server aufgerufen, um einen eingehenden ACK-Request des Three Way Handshakes zu bearbeiten.
     * In diesem Fall erzeugt die Methdoe einen neue <code>Respond</code> mit Acknoledgment aufgrund eines Invite Requests. Dieses Acknoledgement wird dann versandt.
     * @param request aktiver Request (Objekt implementiert Interface @see package javax.servlet.sip). 
     * @throws ServletException generisches Fehlerobjekt der Klasse Exception, wird geworfen wenn ein genereller
     * Fehler in einem Servlet auftritt. @see 
     * @throws IOException generisches Fehlerobjekt der Klasse Exceptions, wird bei einem generellen Input/ Output Fehler
     * geworfen.
     */

    //Mit Input aus Github, WebSocketB2BUASipServlet.java von Jean Deruelle
    @Override
    protected void doAck(SipServletRequest request) throws ServletException,
            IOException {
        if(logger.isInfoEnabled()) {
            logger.info("###################LOGGER: ACK wird gesendet:\n################" + request.toString());
            System.out.println("###############CONSOLE: ACK wird gesendet\n###################" + request.toString());
        }
        //Erstellen einer neuen SipServletResponse als Antwort auf den Invite-Request. Geantwortet wird mit einem Acknoledgement um den 3 Way Handshake abzuschlie&szlig;en.
        //Applikationen antworten mit einem Responsecode von 2xx und ACK nur auf Invite-Requests.
        SipServletResponse response = (SipServletResponse) sessions.get(request.getSession()).getAttribute("lastResponse");
        //ACK wird erstellt und gesendet;
        response.createAck().send();
    }

    //Mit Input aus Github, WebSocketB2BUASipServlet.java von Jean Deruelle
    // doMessage is invoked by the SIP Servlet ?doRequest? method on receiv-
    //ing a SIP MESSAGE primitive, as defined in RFC 3428 [5].
    /**
     * Die <code>doMessage</code> - Methode wird von der SIP Servlet <code>doRequest</code> Methode aufgerufen, sobald diese eine <strong>SIP MESSAGE</strong> erh&auml;lt
     * wie im <a href="https://www.ietf.org/rfc/rfc3428.txt"> RFC3428</a> definiert ist. Die Methode stellt sicher, da&szlig; der eingehende Request immer als "lastRequest" abgespeichert
     * bleibt. Zur Kontrolle wird eine Loggernachricht auf die Konsole ausgegeben.
     * Die Nachricht wird automatisch an den Gespr&auml;chspartner addressiert, d.h. die Methode sucht sich selbst die richtige IP Adresse, addressiert die Nachricht entsprechend.
     * Die Nachricht erh&auml;lt automatisch den aktuellen MIME Typ, der f&uuml;r die Sitzung voreingestellt ist, falls es keinen gibt, wird ein neuer MIME Typ "Pain Text" erstellt.
     * Die Methode erstellt einen neuen Request vom Typ "MESSAGE", falls es einein derartigen Request noch nicht geben sollte. Diser wird dann f&uuml;r den weiteren Nachrichtenaustusch
     * im Rahmen der aktuellen Sitzung weiterverwendet.https://docs.oracle.com/cd/E29542_01/apirefs.1111/e17883/toc.htm?javax/servlet/sip/SipServletMessage.html
     * Falls die eingehende Sitzung keine g&uuml;ltige IP Adresse lieftert, wird der Message-send Proze&szlig; mit einer Fehlermeldung "404" beendet.
     * @param request aktiver Request (Objekt implementiert Interface @see <a href="https://docs.oracle.com/cd/E29542_01/apirefs.1111/e17883/toc.htm?javax/servlet/sip/SipServletMessage.html">package javax.servlet.sip.)</a>
     * @throws ServletException generisches Fehlerobjekt der Klasse Exception, wird geworfen wenn ein genereller
     * Fehler in einem Servlet auftritt. @see <a href="https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/ServletException.html">Servlett Exception</a>
     * @throws IOException generisches Fehlerobjekt der Klasse Exceptions, wird bei einem generellen Input/ Output Fehler
     * geworfen.
     */
    @Override
    protected void doMessage(SipServletRequest request) throws ServletException,
            IOException {
        // das Attribut, Property "lastRequest" der aktuellen Session wird mit dem eingehendem Request als letzter Request gesetzt.
        // so wir sichergestellt, da&szlig; der aktuelle Request immer auch der letzte Request ist, d.h. der lastRequest immer am aktuellen Stand bleibt.
        request.getSession().setAttribute("lastRequest", request);
        // Auf der logger Ebene wird der Request ausgegeben, die Methode <code>toString</code>formatiert den Request entsprechend.
        if(logger.isInfoEnabled()) {
            logger.info("###################LOGGER: Message wird gesendet:\n################" + request.toString());
            // das gleiche passiert auf der Ebene der Konsole, auch hier wird der request mittes der <code>toString</code>Methode formatiert ausgegeben.
            System.out.println("###############CONSOLE: Message wird gesendet\n###################" + request.toString());
        }
        // SipSession ist ein Object das das Protokoll <SipSession> implementiert. Dieses korrespondiert mit den SPI Dialogen.
        // Die SipSession wird dem Klassen-Property <code>sessions>/code> entnommen. (Hashmap key->sessions, value->sessions)
        // mit getSession erh&auml;lt man genau jene Session, die zur aktuellen Sitzung geh&ouml;rt.
        SipSession sipSession = sessions.get(request.getSession());

        // ein ServletRequest mit Namen "message" wird deklariert.
        SipServletRequest message = null;
        // Falls die sipSession nicht im Property gespeichert ist, d.h. es gibt noch keine, wird eine neue erstellt.
        if(sipSession == null) {
            // ein SipServeltRequest "outRecquest" wird erstellt mittels der Methode <code>createRequest</code>. Diese erh&auml;lt folgede Parameter:
            // <li>SipApplicationSession: Die Methode <code>getApplicationSession</code> returniert die Applikations Session
            // zu der die Nachricht geh&ouml;rt. Falls es noch keine gibt, wird eine neue erstellt. </li>
            // <li>Method: request erh&auml;lt die Methode "Primitive - Message".</li>
            // <li>URI from: es wird die Absender URI des requestes verwendet. d.h. die eigene.</li>
            // <li>URI to: es wird die Destinations URI des requestes verwendet. d.h. jene mit dem die Verbindung besteht.
            SipServletRequest outRequest = sipFactory.createRequest(request.getApplicationSession(),
                    "MESSAGE", request.getFrom().getURI(), request.getTo().getURI());
            // Ein String "user" wir angelegt und mit dem Usernamen der Destinations URI bef&uuml;llt, das dem aktuellen request entnommen ist.
            String user = ((SipURI) request.getTo().getURI()).getUser();
            // Die Adresse des users wird gesucht. Dazu wird das Klassenproperty "registeredUsersToIp" verwendet, das als key den String User hat
            // und als value die zugeh&ouml;rige IP Adresse.
            Address adressIncomingCall = registeredUsersToIp.get(user);
            // Falls es zu dem user key (String) keine g&uuml;ltige IP Adresse gibt, wird eine Statusnachricht "Status code (404)" "Not found" verschickt,
            // und abgebrochen.
            if(adressIncomingCall == null) {
                request.createResponse(SipServletResponse.SC_NOT_FOUND).send();
                return;
            }
            // Der neuerstellte outrequest bekommt als request URI die nun gefundene Destinations IP
            outRequest.setRequestURI(adressIncomingCall.getURI());
            // Der deklarierte request "message" bekommt die Adresse des outrequests zugewiesen und zeigt damit auf dieses Objekt. wird deshalb so gemacht,
            // da message ja eine bereits bestehende Nachricht sein k&ouml;nnte, die weiterverwendet wird. In diesem Fall mu&szlig;te sie erst neu erstellt werden.
            // outRequest fungierte als eine Art Proxy bei der Erstellung.
            message = outRequest;
            // der Property Hashmap "sessions" wird jetzt als neuer key die request-sessions gesetzt und als value die neuerstellte outRequest-session.
            // Somit ist diese Session jederzeit durch den aktuellen request findbar.
            sessions.put(request.getSession(), outRequest.getSession());
            // dem Property Hashmap "session" wird das gleiche nochmal gesetzt, diesmal aber in umgekehrter Reihenfolge.
            // Es ist bei Hashmap wesentlich leichter nach key zu suchen, als nach value, so kann sowohl nach dem request, also auch nach dem outReqest gesucht werden.
            sessions.put(outRequest.getSession(), request.getSession());
        } else {
            // Es gibt bereits eine Message im Property gespeichert.
            // es wird nur ein neuer request mit der Methode "Message erstellt"
            message = sipSession.createRequest("MESSAGE");
        }
        // falls der request noch keinen Content-K&ouml;rper besitzt (in Form eines MIME typs)
        if(request.getContent() != null) {
            // wird der Content typ des Requests verwendet.
            String contentType = request.getContentType();
            // Falls es diesen nicht gibt, oder dieser leer ist
            if(contentType == null || contentType.isEmpty()) {
                // wird ein neuer vom typ "text" erzeugt.
                contentType = "text/plain;charset=UTF-8";
            }
            // die message bekommt den content typ des gefundenden contents
            message.setContent(request.getContent(), contentType);

        }
        // Testweise wird die message auf der Konsole ausgegebene.
        if(logger.isInfoEnabled()) {
            logger.info("###################LOGGER: Message wird gesendet:\n################" + message.toString());
            System.out.println("###############CONSOLE: Mesage wird gesendet\n###################" + message.toString());
        }
        // und wird letztendlich verschickt.
        message.send();
    }

}
