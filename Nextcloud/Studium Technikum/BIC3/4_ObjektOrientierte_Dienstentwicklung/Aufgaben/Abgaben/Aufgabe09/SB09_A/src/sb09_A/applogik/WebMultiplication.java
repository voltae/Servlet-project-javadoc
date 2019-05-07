package sb09_A.applogik;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "Multiplication")
public class Multiplication extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        MultiplicationModel multi = new MultiplicationModel();
        // liefert die Parameter des HTTP - Request vom Client an den Server
        String f1 = request.getParameter("factor1");
        int factor1 = Integer.parseInt(f1);

        String f2 = request.getParameter("factor2");
        int factor2 = Integer.parseInt(f2);

        String f3 = request.getParameter("factor3");
        int factor3 = Integer.parseInt(f3);

// Multiplikation der 3 Zeilen, zuerst die ersten beiden und das Ergebnis mit dem 3 Faktor
        int result = multi.multiply(factor1, factor2);
        result = multi.multiply(result, factor3);
        response.setContentType("text/html");
        PrintWriter out = response.getWriter(); out.print("<h1>Ergebnisseite</h1>");
        out.print("Das Produkt aus :" + factor1 + " x " + factor2 + " x " + factor3 + " = <b>"+result+"</b><br>");
        out.print("=========================================");

    }
}
