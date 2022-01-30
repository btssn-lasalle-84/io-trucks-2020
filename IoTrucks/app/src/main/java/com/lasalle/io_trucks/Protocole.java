package com.lasalle.io_trucks;

public class Protocole
{
    /**
     * Général
     */
    public static final String EN_TETE = "$iotruck";
    public static final String DELIMITEUR_CHAMP = ";";
    public static final String DELIMITEUR_FIN = "\r\n";
    /**
     * Trame de commande CMD
     */
    public static final int NB_PARAMETRES_CMD = 2;
    public static final String TRAME_CMD_CHAMP1_TRIANGLE = "T";
    public static final String TRAME_CMD_CHAMP1_GYRO = "G";
    public static final String TRAME_CMD_CHAMP1_ECLAIRAGE = "E";
    public static final String TRAME_CMD_CHAMP1_HAYON = "H";
    public static final String ON  = "1";
    public static final String OFF = "0";
    /**
     * Trame de requête
     */
    public static final int NB_PARAMETRES_REQUETE = 1;
    public static final String TRAME_REQUETE_STATE1 = "S1"; // triangle / gyrophares / éclairage de confort
    public static final String TRAME_REQUETE_STATE2 = "S2"; // hayon /  niveau de charge
    public static final String LEVE  = "1";
    public static final String BAISSE = "0";
    /**
     * Etat de la charge
     */
    public static final String CHARGE_NORMAL = "0";
    public static final String CHARGE_ATTENTION = "1";
    public static final String CHARGE_SURCHARGE = "2";
    /**
     * Trame de service
     */
    public static final String TRAME_SERVICE = "A"; // Alive / Acquittement
    public static final int NB_PARAMETRES_SERVICE = 1;
}
