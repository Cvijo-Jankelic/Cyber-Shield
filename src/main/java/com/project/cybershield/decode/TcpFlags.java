package com.project.cybershield.decode;


 /*
 Ovo je preslik pravog tcp header paketa
  */

public record TcpFlags(
        boolean syn, // zelim zapoceti mrezu, prvi paket u tcp handshakeu
        boolean ack, // potvrduje da je paket primljen svaki paket nakon uspostave veze
        boolean fin, // zavrsavam konekciju sve uredno
        boolean rst, // prekini konekciju odmah port zatvoren, greska, aplikacija abortira vezu
        boolean psh, // push proslijedi odma podatke aplikaciji (ssh, telnet)
        boolean urg // ovaj paket sadrzi hitne podatke
) {

    // UDP/ICMP nemaju TCP flags pa je zato ova metoda koristena
    public static TcpFlags none(){
        return new TcpFlags(false, false, false, false, false, false);
    }

}
