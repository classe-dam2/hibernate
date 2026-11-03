import dao.JugadorDao;
import dao.PartidaDao;
import models.Aresta;
import models.Hexagon;
import models.HexagonVertex;
import models.Jugador;
import models.Partida;
import models.RecursJugador;
import models.Vertex;
import models.abstracts.Carretera;
import models.abstracts.Assentament;
import models.abstracts.Ciutat;
import models.abstracts.Poblat;
import models.enums.EstatPartida;
import models.enums.TipusRecurs;
import org.hibernate.Session;
import util.SessionManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/*
 * Bateria de proves manuals de la part base. S'executa amb la BD buida i mostra per consola
 * cada cas (rebutjat o correcte). Els ids de vèrtexs i arestes de la primera partida
 * coincideixen amb el seu codi del CSV.
 */
public class Main {
 
    // Vèrtex i carretera de cada jugador (per ordre de torn) a la ronda 1 i a la ronda 2 de la preparació
    private static final int[] POBLAT_RONDA_1 = {54, 31, 17};
    private static final int[] CARRETERA_RONDA_1 = {71, 37, 19};
    private static final int[] POBLAT_RONDA_2 = {36, 15, 13};
    private static final int[] CARRETERA_RONDA_2 = {46, 33, 13};

    // Extensió del jugador d'ordre 1: carretera 62 (vèrtexs 46-45) i poblat al vèrtex 45
    private static final int ARESTA_EXTRA = 62;
    private static final int VERTEX_EXTRA = 45;

    private static final PartidaDao partidaDao = new PartidaDao();
    private static final JugadorDao jugadorDao = new JugadorDao();
    private static int idPartida;

    public static void main(String[] args) {
        try {
            provarIniciarPartida();
            provarLectura();
            provarPreparacio();
            provarTornNormal();
            provarRecursosIConstruccions();
            provarProduccioDeCiutat();

            provarVictoria();
            provarLimits();
        } catch (Exception e) {
            System.err.println("Error inesperat: " + e.getMessage());
            e.printStackTrace();
        } finally {
            SessionManager.tancar();
        }
    }

    private static void provarIniciarPartida() {
        titol("1. IniciarPartida amb 1, 5 i 3 jugadors");
        for (List<String> noms : List.of(List.of("Anna"), List.of("A", "B", "C", "D", "E"))) {
            try {
                partidaDao.iniciarPartida(noms);
                System.out.println("ERROR: s'ha acceptat " + noms.size() + " jugadors");
            } catch (IllegalArgumentException e) {
                System.out.println("Rebutjat (" + noms.size() + " jugadors): " + e.getMessage());
            }
        }
        idPartida = partidaDao.iniciarPartida(List.of("Anna", "Pau", "Marta"));
        System.out.println("Partida creada amb id " + idPartida);
    }

    private static void provarLectura() {
        titol("2. MostrarTauler, MostrarPartida, MostrarRecursos");
        partidaDao.mostrarTauler(idPartida);
        partidaDao.MostrarPartida(idPartida);
        jugadorDao.MostrarRecursos(idJugadorOrdre(1));
        System.out.println("-- ids inexistents:");
        partidaDao.mostrarTauler(9999);
        partidaDao.MostrarPartida(9999);
        jugadorDao.MostrarRecursos(9999);
        System.out.println("Punts jugador 9999: " + jugadorDao.CalcularPunts(9999));
    }

    private static void provarPreparacio() {
        titol("3. Preparació: casos rebutjats");
        int fora = idJugadorOrdre(2);
        int actiu = idActiu();
        System.out.println("-- poblat fora de torn:");
        jugadorDao.ConstruirPoblat(fora, POBLAT_RONDA_1[1]);
        System.out.println("-- carretera abans del poblat:");
        jugadorDao.ConstruirCarretera(actiu, CARRETERA_RONDA_1[0]);
        System.out.println("-- passar torn sense poblat:");
        passarTorn();
        System.out.println("-- tirar daus en preparació:");
        tirarDaus();
        System.out.println("-- millorar a ciutat en preparació:");
        jugadorDao.MillorarACiutat(actiu, POBLAT_RONDA_1[0]);
        System.out.println("-- poblat en un vèrtex inexistent:");
        jugadorDao.ConstruirPoblat(actiu, 9999);

        titol("4. Preparació completa (ordre de serp)");
        // Ronda 1: ordres 1, 2, 3
        for (int ordre = 1; ordre <= 3; ordre++) {
            if (ordre == 2) {
                System.out.println("-- poblat massa a prop (al vèrtex 46, veí del " + POBLAT_RONDA_1[0] + "):");
                jugadorDao.ConstruirPoblat(idActiu(), 46);
            }
            torn(ordre, POBLAT_RONDA_1[ordre - 1], CARRETERA_RONDA_1[ordre - 1]);
            if (ordre == 1) {
                System.out.println("-- segon poblat en el mateix torn:");
                jugadorDao.ConstruirPoblat(idActiu(), POBLAT_RONDA_2[0]);
                System.out.println("-- segona carretera en el mateix torn:");
                jugadorDao.ConstruirCarretera(idActiu(), CARRETERA_RONDA_2[0]);
            }
            passarTorn();
        }
        // Ronda 2: ordres 3, 2, 1
        for (int ordre = 3; ordre >= 1; ordre--) {
            torn(ordre, POBLAT_RONDA_2[ordre - 1], CARRETERA_RONDA_2[ordre - 1]);
            passarTorn();
        }
        partidaDao.MostrarPartida(idPartida);
        for (int ordre = 1; ordre <= 3; ordre++) {
            jugadorDao.MostrarRecursos(idJugadorOrdre(ordre));
        }
        partidaDao.mostrarTauler(idPartida);
    }

    // El jugador actiu ha de ser el d'aquest ordre; construeix el poblat i la carretera
    private static void torn(int ordreEsperat, int vertex, int aresta) {
        Jugador actiu = partidaDao.get(idPartida).getJugadorActiu();
        System.out.println("Torn de " + actiu.getNom() + " (ordre " + actiu.getOrdreTorn() + ", esperat " + ordreEsperat + ")");
        jugadorDao.ConstruirPoblat(actiu.getId(), vertex);
        if (ordreEsperat == 3 && vertex == POBLAT_RONDA_1[2]) {
            System.out.println("-- carretera que no toca el poblat nou (aresta 1):");
            jugadorDao.ConstruirCarretera(actiu.getId(), 1);
        }
        jugadorDao.ConstruirCarretera(actiu.getId(), aresta);
    }

    private static void provarTornNormal() {
        titol("5. Torn normal: casos rebutjats abans de tirar");
        int actiu = idActiu();
        System.out.println("-- construir carretera, poblat i ciutat abans de tirar:");
        jugadorDao.ConstruirCarretera(actiu, ARESTA_EXTRA);
        jugadorDao.ConstruirPoblat(actiu, VERTEX_EXTRA);
        jugadorDao.MillorarACiutat(actiu, POBLAT_RONDA_1[0]);
        System.out.println("-- comerciar abans de tirar:");
        jugadorDao.ComerciarBanca(actiu, TipusRecurs.BLAT, TipusRecurs.MAO);
        System.out.println("-- passar torn sense tirar:");
        passarTorn();

        titol("6. Una ronda completa (la ronda ha de pujar a 2)");
        for (int i = 0; i < 3; i++) {
            tirarDaus();
            if (i == 0) {
                System.out.println("-- tirar daus dos cops:");
                tirarDaus();
            }
            passarTorn();
        }
        partidaDao.MostrarPartida(idPartida);
    }

    // Parteix d'un jugador amb els recursos posats a l'SQL per tenir els casos controlats
    private static void provarRecursosIConstruccions() {
        titol("7. Sense recursos, comerç amb 3 recursos i poblat sense recursos");
        int actiu = idActiu();
        tirarDaus();
        ficarRecursos(actiu, 0, 3);
        System.out.println("Recursos abans dels rebutjos:");
        jugadorDao.MostrarRecursos(actiu);
        System.out.println("-- carretera, ciutat i comerç (3 LLANA) sense recursos suficients:");
        jugadorDao.ConstruirCarretera(actiu, ARESTA_EXTRA);
        jugadorDao.MillorarACiutat(actiu, POBLAT_RONDA_2[0]);
        jugadorDao.ComerciarBanca(actiu, TipusRecurs.LLANA, TipusRecurs.FUSTA);
        System.out.println("Recursos després dels rebutjos (han de ser iguals):");
        jugadorDao.MostrarRecursos(actiu);

        System.out.println("-- carretera amb 1 FUSTA i 1 MAO, i després poblat sense recursos:");
        ficarRecursos(actiu, 0, 3);
        ficarRecurs(actiu, TipusRecurs.FUSTA, 1);
        ficarRecurs(actiu, TipusRecurs.MAO, 1);
        jugadorDao.ConstruirCarretera(actiu, ARESTA_EXTRA);
        jugadorDao.ConstruirPoblat(actiu, VERTEX_EXTRA);
        jugadorDao.MostrarRecursos(actiu);

        ficarRecursos(actiu, 10, 10);
        System.out.println("-- carretera que no connecta amb res meu (aresta 1):");
        jugadorDao.ConstruirCarretera(actiu, 1);
        System.out.println("-- millorar a ciutat el poblat del vèrtex " + POBLAT_RONDA_2[0] + ":");
        jugadorDao.MillorarACiutat(actiu, POBLAT_RONDA_2[0]);
        System.out.println("-- vèrtexs disponibles actuals: "
                + jugadorDao.VertexsDisponibles(actiu).stream().map(Vertex::getId).toList());
        System.out.println("-- exèrcit més gran actual: " + partidaDao.ExercitMesGran(idPartida));
    }


    // Es passa de torn fins que surt el número d'un hexàgon que toca la ciutat, i es mira què rep el propietari
    private static void provarProduccioDeCiutat() {
        titol("8. Una ciutat produeix 2 recursos");
        int propietari = idActiu();
        int numero = numeroAdjacent(POBLAT_RONDA_2[0]);
        System.out.println("Es tira fins que surti un " + numero + " (hexàgon adjacent a la ciutat del vèrtex " + POBLAT_RONDA_2[0] + ")");
        passarTorn();
        boolean numeroTrobat = false;
        for (int i = 0; i < 500; i++) {
            String abans = recursos(propietari);
            tirarDaus();
            if (ultimaTirada() == numero) {
                System.out.println("Recursos abans: " + abans);
                System.out.println("Recursos després (el recurs de l'hexàgon puja 2 per la ciutat): " + recursos(propietari));
                numeroTrobat = true;
                break;
            }
            passarTorn();
        }
        if (!numeroTrobat) {
            System.out.println("No ha sortit el número " + numero + " després de 500 tirades.");
        }
        // Es torna al propietari amb els daus ja tirats
        for (int torns = 0; idActiu() != propietari && torns < 3; torns++) {
            passarTorn();
            tirarDaus();
        }
        if (idActiu() != propietari) {
            throw new IllegalStateException("No s'ha pogut tornar al torn del propietari " + propietari);
        }
        if (!dausTirats()) tirarDaus();
    }

    private static void tirarDaus() {
        moureLladrePendent();
        partidaDao.TirarDaus(idPartida);
        moureLladrePendent();
    }

    private static void passarTorn() {
        moureLladrePendent();
        partidaDao.PassarTorn(idPartida);
    }

    /*
     * Després d'un 7 (o d'una carta de cavaller), mou el lladre abans de qualsevol altra acció.
     * Prefereix un hexàgon amb una víctima vàlida; si no n'hi ha, el mou a un hexàgon sense
     * víctimes elegibles i passa null al DAO, tal com exigeix la regla.
     */
    private static void moureLladrePendent() {
        Session session = sessio();
        Partida partida = session.get(Partida.class, idPartida);
        if (partida == null || !partida.isLladrePendent()) {
            session.getTransaction().commit();
            return;
        }

        int idJugador = partida.getJugadorActiu().getId();
        Hexagon desti = null;
        Integer idVictima = null;
        for (Hexagon hexagon : partida.getHexagons()) {
            if (partida.getLladre() != null && partida.getLladre().getId().equals(hexagon.getId())) continue;
            Integer victimaHexagon = null;
            for (HexagonVertex hv : hexagon.getHexagonVertexs()) {
                Assentament assentament = hv.getVertex().getAssentament();
                if (assentament == null) continue;
                Jugador jugador = assentament.getJugador();
                if (jugador.getId().equals(idJugador)) continue;
                int recursosVictima = jugador.getRecursos().stream().mapToInt(RecursJugador::getQuantitat).sum();
                if (recursosVictima > 0) {
                    victimaHexagon = jugador.getId();
                    break;
                }
            }
            if (victimaHexagon != null) {
                desti = hexagon;
                idVictima = victimaHexagon;
                break;
            }
            if (desti == null) desti = hexagon;
        }
        if (desti == null) {
            session.getTransaction().rollback();
            throw new IllegalStateException("No hi ha cap hexàgon vàlid per moure el lladre");
        }
        session.getTransaction().commit();

        if (idVictima == null) {
            System.out.println("Set: el lladre es mou a l'hexàgon " + desti.getCodi()
                    + "; no hi ha cap víctima elegible.");
        } else {
            System.out.println("Set: el lladre es mou a l'hexàgon " + desti.getCodi()
                    + " i roba a " + idVictima + ".");
        }
        jugadorDao.MoureLladre(idJugador, desti.getId(), idVictima);

        session = sessio();
        boolean continuaPendent = session.get(Partida.class, idPartida).isLladrePendent();
        session.getTransaction().commit();
        if (continuaPendent) {
            throw new IllegalStateException("El moviment del lladre no s'ha pogut completar");
        }
    }

    private static boolean dausTirats() {
        Session session = sessio();
        boolean tirats = session.get(Partida.class, idPartida).isDausTirats();
        session.getTransaction().commit();
        return tirats;
    }

    private static void provarVictoria() {
        titol("9. Construir, millorar, comerciar i guanyar");
        int actiu = idActiu();
        ficarRecursos(actiu, 10, 10);
        baixarPunts(idPartida, 5);

        System.out.println("-- poblat massa a prop (vèrtex 46):");
        jugadorDao.ConstruirPoblat(actiu, 46);
        System.out.println("-- millorar un vèrtex que no és un poblat propi:");
        jugadorDao.MillorarACiutat(actiu, POBLAT_RONDA_1[1]);
        System.out.println("-- comerciar amb recursos iguals:");
        jugadorDao.ComerciarBanca(actiu, TipusRecurs.BLAT, TipusRecurs.BLAT);
        System.out.println("-- comerciar correctament:");
        jugadorDao.ComerciarBanca(actiu, TipusRecurs.BLAT, TipusRecurs.MAO);
        jugadorDao.MostrarRecursos(actiu);
        System.out.println("-- carretera repetida i poblat en vèrtex ocupat:");
        jugadorDao.ConstruirCarretera(actiu, ARESTA_EXTRA);
        jugadorDao.ConstruirPoblat(actiu, POBLAT_RONDA_1[0]);
        System.out.println("Punts abans de millorar: " + jugadorDao.CalcularPunts(actiu));
        jugadorDao.MillorarACiutat(actiu, POBLAT_RONDA_1[0]);
        System.out.println("Punts després de millorar: " + jugadorDao.CalcularPunts(actiu));
        jugadorDao.ConstruirPoblat(actiu, VERTEX_EXTRA);
        System.out.println("Punts finals: " + jugadorDao.CalcularPunts(actiu));
        partidaDao.MostrarPartida(idPartida);
        partidaDao.mostrarTauler(idPartida);

        System.out.println("-- accions amb la partida finalitzada:");
        tirarDaus();
        passarTorn();
        jugadorDao.ConstruirCarretera(actiu, CARRETERA_RONDA_1[0]);
        jugadorDao.ComerciarBanca(actiu, TipusRecurs.FUSTA, TipusRecurs.LLANA);
        partidaDao.ComprovarVictoria(idPartida);
    }

    /*
     * Límits (15 carreteres, 5 poblats, 4 ciutats): s'insereixen les construccions directament a la BD,
     * sense passar per les regles, en una partida nova posada en EN_CURS i amb els daus tirats.
     */
    private static void provarLimits() {
        titol("10. Límits: carretera 16, poblat 6 i ciutat 5");
        idPartida = partidaDao.iniciarPartida(List.of("Anna", "Pau", "Marta"));
        baixarPunts(idPartida, 100);
        Session session = sessio();
        session.createMutationQuery("update Partida p set p.estat = :estat, p.dausTirats = true where p.id = :id")
                .setParameter("estat", EstatPartida.EN_CURS)
                .setParameter("id", idPartida)
                .executeUpdate();
        Partida partida = session.get(Partida.class, idPartida);
        Jugador jugador = partida.getJugadorActiu();
        List<Vertex> lliures = verticesSeparats(partida);
        Vertex vertexSenseRecursos = lliures.get(11);
        List<Aresta> arestes = new ArrayList<>(partida.getArestas());
        Aresta tocaVertex = arestes.stream().filter(a -> toca(a, vertexSenseRecursos)).findFirst().orElseThrow();
        crearCarretera(session, jugador, tocaVertex);
        session.getTransaction().commit();
        int idJugador = jugador.getId();

        System.out.println("-- poblat connectat a una carretera però sense recursos:");
        jugadorDao.ConstruirPoblat(idJugador, vertexSenseRecursos.getId());

        session = sessio();
        partida = session.get(Partida.class, idPartida);
        jugador = session.get(Jugador.class, idJugador);
        lliures = verticesSeparats(partida);
        arestes = new ArrayList<>(partida.getArestas());
        int carreteres = 1;
        for (Aresta a : arestes) {
            if (carreteres == 15) break;
            if (a.getCarretera() == null) {
                crearCarretera(session, jugador, a);
                carreteres++;
            }
        }
        // 4 poblats + el que després es millora = 5 poblats, el màxim
        for (int i = 0; i < 4; i++) crearPoblat(session, jugador, lliures.get(i));
        for (int i = 5; i < 9; i++) crearCiutat(session, jugador, lliures.get(i));
        crearPoblat(session, jugador, lliures.get(9));
        int poblatPerMillorar = lliures.get(9).getId();
        int vertexLliure = lliures.get(10).getId();
        int arestaLliure = arestes.stream().filter(a -> a.getCarretera() == null).findFirst().orElseThrow().getId();
        session.getTransaction().commit();
        ficarRecursos(idJugador, 10, 10);

        System.out.println("-- la carretera núm. 16 (ja n'hi ha 15):");
        jugadorDao.ConstruirCarretera(idJugador, arestaLliure);
        System.out.println("-- el poblat núm. 6 (ja n'hi ha 5 i recursos de sobra):");
        jugadorDao.ConstruirPoblat(idJugador, vertexLliure);
        System.out.println("-- la ciutat núm. 5 (ja n'hi ha 4):");
        jugadorDao.MillorarACiutat(idJugador, poblatPerMillorar);
        partidaDao.MostrarPartida(idPartida);

        titol("11. Consultes amb nom (Partida.perEstat)");
        partidaDao.mostrarPartidesPerEstat(EstatPartida.FINALITZADA);
        partidaDao.mostrarPartidesPerEstat(EstatPartida.EN_CURS);
    }

    // Vèrtexs en ordre de codi que no són veïns entre ells (així la regla de la distància no molesta)
    private static List<Vertex> verticesSeparats(Partida partida) {
        List<Vertex> ordenats = new ArrayList<>(partida.getVertexs());
        ordenats.sort(Comparator.comparing(Vertex::getCodi));
        List<Vertex> separats = new ArrayList<>();
        for (Vertex v : ordenats) {
            boolean veiTriat = false;
            for (Aresta a : partida.getArestas()) {
                for (Vertex triat : separats) {
                    if (toca(a, v) && toca(a, triat)) veiTriat = true;
                }
            }
            if (!veiTriat) separats.add(v);
        }
        return separats;
    }

    private static boolean toca(Aresta aresta, Vertex v) {
        return Vertex.mateix(aresta.getVertexA(), v) || Vertex.mateix(aresta.getVertexB(), v);
    }

    private static void crearCarretera(Session session, Jugador jugador, Aresta aresta) {
        Carretera c = new Carretera();
        c.setJugador(jugador);
        c.setAresta(aresta);
        session.persist(c);
    }

    private static void crearPoblat(Session session, Jugador jugador, Vertex vertex) {
        Poblat p = new Poblat();
        p.setJugador(jugador);
        p.setVertex(vertex);
        session.persist(p);
    }

    private static void crearCiutat(Session session, Jugador jugador, Vertex vertex) {
        Ciutat c = new Ciutat();
        c.setJugador(jugador);
        c.setVertex(vertex);
        session.persist(c);
    }

    // SQL directe: el mateix valor a tots els recursos del jugador, i el de LLANA a part
    private static void ficarRecursos(int idJugador, int quantitat, int llana) {
        Session session = sessio();
        session.createMutationQuery("update RecursJugador r set r.quantitat = :q where r.jugador.id = :id")
                .setParameter("q", quantitat)
                .setParameter("id", idJugador)
                .executeUpdate();
        session.getTransaction().commit();
        ficarRecurs(idJugador, TipusRecurs.LLANA, llana);
    }

    private static void ficarRecurs(int idJugador, TipusRecurs tipus, int quantitat) {
        Session session = sessio();
        session.createMutationQuery("update RecursJugador r set r.quantitat = :q where r.jugador.id = :id and r.tipus = :t")
                .setParameter("q", quantitat)
                .setParameter("id", idJugador)
                .setParameter("t", tipus)
                .executeUpdate();
        session.getTransaction().commit();
    }

    private static void baixarPunts(int idPartida, int punts) {
        Session session = sessio();
        session.createMutationQuery("update Partida p set p.puntsPerGuanyar = :punts where p.id = :id")
                .setParameter("punts", punts)
                .setParameter("id", idPartida)
                .executeUpdate();
        session.getTransaction().commit();
    }

    // Número d'un hexàgon amb número que toca el vèrtex
    private static int numeroAdjacent(int idVertex) {
        Session session = sessio();
        Partida partida = session.get(Partida.class, idPartida);
        for (Hexagon h : partida.getHexagons()) {
            for (HexagonVertex hv : h.getHexagonVertexs()) {
                if (hv.getVertex().getId() == idVertex && h.getNumero() != null) {
                    session.getTransaction().commit();
                    return h.getNumero();
                }
            }
        }
        throw new IllegalStateException("El vèrtex " + idVertex + " no toca cap hexàgon amb número");
    }

    private static int ultimaTirada() {
        Session session = sessio();
        int tirada = session.get(Partida.class, idPartida).getUltimaTirada();
        session.getTransaction().commit();
        return tirada;
    }

    private static String recursos(int idJugador) {
        Session session = sessio();
        StringBuilder text = new StringBuilder();
        for (RecursJugador r : session.get(Jugador.class, idJugador).getRecursos()) {
            text.append(r.getTipus()).append('=').append(r.getQuantitat()).append(' ');
        }
        session.getTransaction().commit();
        return text.toString().trim();
    }

    private static Session sessio() {
        Session session = SessionManager.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        return session;
    }

    private static int idActiu() {
        return partidaDao.get(idPartida).getJugadorActiu().getId();
    }

    private static int idJugadorOrdre(int ordre) {
        for (Jugador j : jugadorDao.list()) {
            if (j.getPartida().getId() == idPartida && j.getOrdreTorn() == ordre) {
                return j.getId();
            }
        }
        throw new IllegalStateException("No hi ha cap jugador amb ordre " + ordre);
    }

    private static void titol(String text) {
        System.out.println();
        System.out.println("===== " + text + " =====");
    }
}
