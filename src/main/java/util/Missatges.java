package util;

/**
 * Missatges d'error i d'informació del joc. Cada missatge és una constant perquè
 * no es repeteixi el mateix literal a mètodes diferents.
 */
public final class Missatges {
    private Missatges() {
    }

    // Comprovació comuna (partida, torn, estat, daus)
    public static final String PARTIDA_NO_EXISTEIX = "La partida %d no existeix.";
    public static final String JUGADOR_NO_EXISTEIX = "El jugador %d no existeix.";
    public static final String PARTIDA_FINALITZADA = "La partida ja ha finalitzat.";
    public static final String PARTIDA_NO_EN_CURS = "La partida no està en curs.";
    public static final String NO_ES_EL_SEU_TORN = "No és el teu torn.";
    public static final String NOMES_EN_CURS = "Aquesta acció no es pot fer durant la preparació.";
    public static final String DAUS_NO_TIRATS = "Primer has de tirar els daus.";
    public static final String DAUS_JA_TIRATS = "Ja has tirat els daus aquest torn.";

    // Construccions
    public static final String ARESTA_NO_TROBADA = "L'aresta no existeix en aquesta partida.";
    public static final String ARESTA_OCUPADA = "L'aresta ja té una carretera.";
    public static final String CARRETERA_NO_CONNECTADA = "La carretera ha d'estar connectada a una construcció o carretera teva.";
    public static final String CARRETERA_NO_TOCA_POBLAT = "La carretera ha de tocar el poblat que acabes de col·locar.";
    public static final String CARRETERA_JA_COL_LOCADA = "Ja has col·locat la carretera d'aquest torn.";
    public static final String CARRETERA_SENSE_RECURSOS = "Recursos insuficients (1 FUSTA i 1 MAO).";
    public static final String CARRETERA_LIMIT = "Has arribat al límit de %d carreteres.";

    public static final String VERTEX_NO_TROBAT = "El vèrtex no existeix en aquesta partida.";
    public static final String VERTEX_OCUPAT = "El vèrtex ja està ocupat.";
    public static final String POBLAT_MASSA_PROP = "Massa a prop d'una altra construcció.";
    public static final String POBLAT_NO_CONNECTAT = "El poblat ha d'estar connectat a una carretera teva.";
    public static final String POBLAT_SENSE_RECURSOS = "Recursos insuficients (1 FUSTA, 1 MAO, 1 LLANA i 1 BLAT).";
    public static final String POBLAT_LIMIT = "Has arribat al límit de %d poblats.";
    public static final String POBLAT_JA_COL_LOCAT = "Ja has col·locat el poblat d'aquest torn.";
    public static final String POBLAT_PRIMER = "Primer has de col·locar un poblat.";

    public static final String CIUTAT_SENSE_POBLAT = "Al vèrtex no hi ha cap poblat teu.";
    public static final String CIUTAT_SENSE_RECURSOS = "Recursos insuficients (2 BLAT i 3 MINERAL).";
    public static final String CIUTAT_LIMIT = "Has arribat al límit de %d ciutats.";

    // Banca
    public static final String BANCA_RECURS_NUL = "Cal indicar el recurs que dónes i el que reps.";
    public static final String BANCA_RECURSOS_IGUALS = "El recurs que dónes i el que reps no poden ser iguals.";
    public static final String BANCA_SENSE_RECURSOS = "Cal tenir 4 recursos iguals per comerciar amb la banca.";

    // PassarTorn
    public static final String TORN_FALTA_POBLAT = "No pots passar el torn: falta col·locar el poblat.";
    public static final String TORN_FALTA_CARRETERA = "No pots passar el torn: falta col·locar la carretera.";

    // Informació
    public static final String INFO_CARRETERA = "Carretera construïda a l'aresta %d.";
    public static final String INFO_POBLAT = "Poblat construït al vèrtex %d.";
    public static final String INFO_CIUTAT = "Poblat millorat a ciutat al vèrtex %d.";
    public static final String INFO_COMERCI = "%s ha canviat 4 %s per 1 %s.";
    public static final String INFO_TORN = "Ara juga %s (ronda %d).";
    public static final String INFO_PREPARACIO_ACABADA = "Preparació acabada: comença la partida. Juga %s.";
    public static final String INFO_GUANYADOR = "%s ha guanyat la partida amb %d punts!";
    public static final String INFO_PRODUCCIO = "%s rep %d de %s.";
    public static final String INFO_TIRADA = "Daus: %d + %d = %d.";
    public static final String INFO_SENSE_PRODUCCIO = "Ha sortit un 7: no hi ha producció.";
}
