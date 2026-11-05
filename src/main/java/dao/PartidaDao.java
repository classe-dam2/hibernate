package dao;

import models.Aresta;
import models.CartaDesenvolupament;
import models.Hexagon;
import models.HexagonVertex;
import models.Jugador;
import models.Partida;
import models.RecursJugador;
import models.Vertex;
import models.abstracts.Assentament;
import models.abstracts.Ciutat;
import models.enums.ColorJugador;
import models.enums.EstatPartida;
import models.enums.TipusCarta;
import models.enums.TipusRecurs;
import models.enums.TipusTerreny;
import org.hibernate.Session;

import util.Missatges;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PartidaDao extends GenericDao<Partida, Integer> implements IPartidaDao {
    private static final String CARPETA_CSV = "/dades/";
    private static final int MIN_JUGADORS = 2;
    private static final int MAX_JUGADORS = 4;
    private static final int PUNTS_PER_GUANYAR = 10;
    private static final int TIRADA_SENSE_PRODUCCIO = 7;
    private static final Random RANDOM = new Random();

    // Distribució estàndard de Catan: 19 hexàgons i 18 fitxes de número (el desert no en porta)
    private static final List<TipusTerreny> TERRENYS = new ArrayList<>();

    private static final List<Integer> NUMEROS = List.of(2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12);


    public PartidaDao() {

        if (TERRENYS.isEmpty()) {
            TERRENYS.addAll(Collections.nCopies(4, TipusTerreny.BOSC));
            TERRENYS.addAll(Collections.nCopies(4, TipusTerreny.PASTURES));
            TERRENYS.addAll(Collections.nCopies(4, TipusTerreny.CAMPS));
            TERRENYS.addAll(Collections.nCopies(3, TipusTerreny.TURONS));
            TERRENYS.addAll(Collections.nCopies(3, TipusTerreny.MUNTANYES));
            TERRENYS.add(TipusTerreny.DESERT);
        }
    }

    @Override
    public int iniciarPartida(List<String> nomsJugadors) {
        if (nomsJugadors == null || nomsJugadors.size() < MIN_JUGADORS || nomsJugadors.size() > MAX_JUGADORS) {
            throw new IllegalArgumentException("Calen entre " + MIN_JUGADORS + " i " + MAX_JUGADORS + " jugadors");
        }

        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();

            Partida partida = new Partida();
            partida.setEstat(EstatPartida.PREPARACIO);
            partida.setDataInici(LocalDateTime.now());
            partida.setRonda(1);
            partida.setUltimaTirada(0);
            partida.setPuntsPerGuanyar(PUNTS_PER_GUANYAR);
            session.persist(partida);

            crearJugadors(session, partida, nomsJugadors);
            carregarTauler(session, partida);
            crearPila(session, partida);

            session.getTransaction().commit();
            return partida.getId();
        } catch (RuntimeException e) {
            // Qualsevol error -> rollback de TOTA la partida
            handleException(session, e);
            return -1;
        }
    }

    private void crearPila(Session session, Partida partida) {
        for (TipusCarta tipus : TipusCarta.values()) {
            int quantitat = switch (tipus) {
                case CAVALLER -> 14;
                case MONOPOLI -> 2;
                case PUNT_VICTORIA -> 5;
            };
            for (int i = 0; i < quantitat; i++) {
                CartaDesenvolupament carta = new CartaDesenvolupament();
                carta.setPartida(partida);
                carta.setTipus(tipus);
                carta.setRondaCompra(0);
                session.persist(carta);
            }
        }
    }

    // Ordre de torn aleatori; el color depèn de l'ordre (1r -> VERMELL, 2n -> BLAU...)
    private void crearJugadors(Session session, Partida partida, List<String> noms) {
        List<String> barrejats = new ArrayList<>(noms);
        Collections.shuffle(barrejats);

        for (int i = 0; i < barrejats.size(); i++) {
            Jugador j = new Jugador();
            j.setNom(barrejats.get(i));
            j.setOrdreTorn(i + 1);
            j.setColor(ColorJugador.values()[i]);
            j.setCavallersJugats(0);
            j.setPartida(partida);
            session.persist(j);
            for (TipusRecurs tipus : TipusRecurs.values()) {
                RecursJugador r = new RecursJugador();
                r.setJugador(j);
                r.setTipus(tipus);
                r.setQuantitat(0);
                session.persist(r);
                j.getRecursos().add(r);
            }
            if (i == 0) {
                partida.setJugadorActiu(j);
            }
        }
    }

    private void carregarTauler(Session session, Partida partida) {
        Map<String, Vertex> vertexs = carregarVertexs(session, partida);
        carregarArestes(session, partida, vertexs);
        carregarHexagons(session, partida, vertexs);
    }

    private Map<String, Vertex> carregarVertexs(Session session, Partida partida) {
        Map<String, Vertex> vertexs = new HashMap<>();
        for (String[] c : llegirCsv("vertexs.csv")) {
            Vertex v = new Vertex();
            v.setCodi(Integer.parseInt(c[0]));
            v.setPartida(partida);
            session.persist(v);
            vertexs.put(c[0], v);
        }
        return vertexs;
    }

    private void carregarArestes(Session session, Partida partida, Map<String, Vertex> vertexs) {
        for (String[] c : llegirCsv("arestes.csv")) {
            Aresta a = new Aresta();
            a.setCodi(Integer.parseInt(c[0]));
            a.setPartida(partida);
            a.setVertexA(vertexs.get(c[1]));
            a.setVertexB(vertexs.get(c[2]));
            session.persist(a);
        }
    }

    // Terreny i número a l'atzar; el lladre comença al desert (que no té número).
    private void carregarHexagons(Session session, Partida partida, Map<String, Vertex> vertexs) {
        List<TipusTerreny> terrenys = new ArrayList<>(TERRENYS);
        List<Integer> numeros = new ArrayList<>(NUMEROS);
        Collections.shuffle(terrenys);
        Collections.shuffle(numeros);

        Map<String, Hexagon> hexagons = new HashMap<>();
        List<String[]> files = llegirCsv("hexagons.csv");
        for (int i = 0; i < files.size(); i++) {
            String[] c = files.get(i);
            TipusTerreny terreny = terrenys.get(i);

            Hexagon h = new Hexagon();
            h.setCodi(Integer.parseInt(c[0]));
            h.setQ(Integer.parseInt(c[1]));
            h.setR(Integer.parseInt(c[2]));
            h.setPartida(partida);
            h.setTerreny(terreny);
            h.setNumero(terreny == TipusTerreny.DESERT ? null : numeros.remove(numeros.size() - 1));
            session.persist(h);
            hexagons.put(c[0], h);

            if (terreny == TipusTerreny.DESERT) {
                partida.setLladre(h);
            }
        }

        for (String[] c : llegirCsv("hexagon_vertex.csv")) {
            HexagonVertex hv = new HexagonVertex();
            hv.setPosicio(Integer.parseInt(c[1]));
            hv.setHexagon(hexagons.get(c[0]));
            hv.setVertex(vertexs.get(c[2]));
            session.persist(hv);
        }
    }

    /*
     * CSV de resources/dades/: UTF-8, separador coma, primera línia = capçalera.
     * split(",", -1): el -1 conserva les columnes buides del final.
     */
    private List<String[]> llegirCsv(String fitxer) {
        InputStream is = PartidaDao.class.getResourceAsStream(CARPETA_CSV + fitxer);
        if (is == null) {
            throw new IllegalStateException("No es troba el fitxer " + CARPETA_CSV + fitxer);
        }
        List<String[]> files = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            br.readLine(); // capçalera
            String linia;
            while ((linia = br.readLine()) != null) {
                if (linia.isBlank()) {
                    continue;
                }
                String[] camps = linia.split(",", -1);
                for (int i = 0; i < camps.length; i++) {
                    camps[i] = camps[i].trim();
                }
                files.add(camps);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Error llegint " + fitxer, e);
        }
        return files;
    }

    @Override
    public void mostrarTauler(int idPartida) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Partida partida = session.get(Partida.class, idPartida);
            if (partida == null) {
                System.out.println(String.format(Missatges.PARTIDA_NO_EXISTEIX, idPartida));
            } else {
                for (Hexagon h : partida.getHexagons()) {
                    String numero = h.getNumero() == null ? "sense número" : "número " + h.getNumero();
                    System.out.println("Hexàgon " + h.getCodi() + ": " + h.getTerreny() + ", " + numero
                            + (esElLladre(partida, h) ? " [LLADRE]" : ""));
                    for (HexagonVertex hv : h.getHexagonVertexs()) {
                        Assentament a = hv.getVertex().getAssentament();
                        if (a != null) {
                            System.out.println("    " + a.getClass().getSimpleName() + " al vèrtex " + hv.getVertex().getCodi()
                                    + " de " + a.getJugador().getNom() + " (" + a.getJugador().getColor() + ")");
                        }
                    }
                }
            }
            session.getTransaction().commit();
        } catch (RuntimeException e) {
            handleException(session, e);
        }
    }

    @Override
    public void MostrarPartida(int idPartida) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Partida partida = session.get(Partida.class, idPartida);
            if (partida == null) {
                System.out.println(String.format(Missatges.PARTIDA_NO_EXISTEIX, idPartida));
            } else {
                System.out.println("===== PARTIDA " + idPartida + " =====");
                System.out.println("Estat: " + partida.getEstat());
                System.out.println("Ronda: " + partida.getRonda());
                Jugador actiu = partida.getJugadorActiu();
                System.out.println("Jugador actiu: " + (actiu == null ? "cap" : actiu.getNom()));
                System.out.println("Última tirada: " + partida.getUltimaTirada());
                List<Jugador> jugadors = session.createNamedQuery("Jugador.perPartida", Jugador.class)
                        .setParameter("idPartida", idPartida).getResultList();
                for (Jugador j : jugadors) {
                    System.out.println("Jugador " + j.getOrdreTorn() + ": " + j.getNom() + " (" + j.getColor() + ")"
                            + " - punts: " + ReglesJoc.calcularPunts(j)
                            + ", poblats: " + ReglesJoc.comptarPoblats(j)
                            + ", ciutats: " + ReglesJoc.comptarCiutats(j)
                            + ", carreteres: " + ReglesJoc.comptarCarreteres(j)
                            + ", recursos: " + ReglesJoc.totalRecursos(j));
                }
            }
            session.getTransaction().commit();
        } catch (RuntimeException e) {
            handleException(session, e);
        }
    }

    @Override
    public void TirarDaus(int idPartida) {
        tirar(idPartida, RANDOM.nextInt(6) + 1, RANDOM.nextInt(6) + 1);
    }

    private void tirar(int idPartida, int dau1, int dau2) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Partida partida = session.get(Partida.class, idPartida);
            String error = validarTirada(partida, idPartida);
            if (error != null) {
                ReglesJoc.rebutjar(session, error);
                return;
            }

            int tirada = dau1 + dau2;
            partida.setUltimaTirada(tirada);
            partida.setDausTirats(true);
            System.out.println(String.format(Missatges.INFO_TIRADA, dau1, dau2, tirada));

            if (tirada == TIRADA_SENSE_PRODUCCIO) {
                descartarRecursos(partida);
                partida.setLladrePendent(true);
                System.out.println(Missatges.INFO_SENSE_PRODUCCIO);
            } else {
                produir(session, partida, tirada);
            }
            session.getTransaction().commit();
        } catch (RuntimeException e) {
            handleException(session, e);
        }
    }

    // Motiu pel qual no es poden tirar els daus, o null si es pot
    private String validarTirada(Partida partida, int idPartida) {
        if (partida == null) return String.format(Missatges.PARTIDA_NO_EXISTEIX, idPartida);
        if (partida.getEstat() == EstatPartida.FINALITZADA) return Missatges.PARTIDA_FINALITZADA;
        if (partida.getEstat() != EstatPartida.EN_CURS) return Missatges.PARTIDA_NO_EN_CURS;
        if (partida.isLladrePendent()) return "Cal moure el lladre abans de tirar";
        if (partida.isDausTirats()) return Missatges.DAUS_JA_TIRATS;
        return null;
    }

    // Cada jugador amb més de set cartes descarta la meitat, arrodonida cap avall.
    private void descartarRecursos(Partida partida) {
        for (Jugador jugador : partida.getJugadors()) {
            int total = ReglesJoc.totalRecursos(jugador);
            if (total <= 7) continue;

            int perDescartar = total / 2;
            for (int i = 0; i < perDescartar; i++) {
                int index = RANDOM.nextInt(total);
                for (RecursJugador recurs : jugador.getRecursos()) {
                    if (index < recurs.getQuantitat()) {
                        ReglesJoc.descomptar(jugador, recurs.getTipus(), 1);
                        total--;
                        break;
                    }
                    index -= recurs.getQuantitat();
                }
            }
        }
    }

    // Cada assentament adjacent a un hexàgon amb el número tirat rep 1 recurs (2 si és ciutat).
    private void produir(Session session, Partida partida, int tirada) {
        for (Hexagon h : partida.getHexagons()) {
            if (h.getNumero() == null || h.getNumero() != tirada || esElLladre(partida, h)) continue;
            TipusRecurs recurs = ReglesJoc.recursDe(h.getTerreny());
            for (HexagonVertex hv : h.getHexagonVertexs()) {
                Assentament a = hv.getVertex().getAssentament();
                if (a == null) continue;
                int quantitat = a instanceof Ciutat ? 2 : 1;
                ReglesJoc.afegir(session, a.getJugador(), recurs, quantitat);
                System.out.println(String.format(Missatges.INFO_PRODUCCIO, a.getJugador().getNom(), quantitat, recurs));
            }
        }
    }

    private boolean esElLladre(Partida partida, Hexagon h) {
        return partida.getLladre() != null && partida.getLladre().getId().equals(h.getId());
    }

    @Override
    public void PassarTorn(int idPartida) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Partida partida = session.get(Partida.class, idPartida);
            String error = validarPassarTorn(session, partida, idPartida);
            if (error != null) {
                ReglesJoc.rebutjar(session, error);
                return;
            }

            boolean eraPreparacio = partida.getEstat() == EstatPartida.PREPARACIO;
            if (eraPreparacio) {
                avancarPreparacio(partida);
            } else {
                avancarTorn(partida);
            }
            partida.setDausTirats(false);

            session.getTransaction().commit();
            if (eraPreparacio && partida.getEstat() == EstatPartida.EN_CURS) {
                System.out.println(String.format(Missatges.INFO_PREPARACIO_ACABADA, partida.getJugadorActiu().getNom()));
            } else {
                System.out.println(String.format(Missatges.INFO_TORN, partida.getJugadorActiu().getNom(), partida.getRonda()));
            }
        } catch (RuntimeException e) {
            handleException(session, e);
        }
    }

    // Motiu pel qual no es pot passar el torn, o null si es pot
    private String validarPassarTorn(Session session, Partida partida, int idPartida) {
        if (partida == null) return String.format(Missatges.PARTIDA_NO_EXISTEIX, idPartida);
        if (partida.getEstat() == EstatPartida.FINALITZADA) return Missatges.PARTIDA_FINALITZADA;
        if (partida.isLladrePendent()) return "Cal moure el lladre abans de passar el torn";
        Jugador actiu = partida.getJugadorActiu();
        if (partida.getEstat() == EstatPartida.PREPARACIO) {
            if (ReglesJoc.comptarPoblats(actiu) < partida.getRonda()) return Missatges.TORN_FALTA_POBLAT;
            if (ReglesJoc.comptarCarreteres(actiu) < partida.getRonda()) return Missatges.TORN_FALTA_CARRETERA;
            return null;
        }
        return partida.isDausTirats() ? null : Missatges.DAUS_NO_TIRATS;
    }

    /*
     * Ordre de serp: la ronda 1 va de l'ordre 1 al darrer, el darrer jugador repeteix a la ronda 2,
     * que va cap enrere fins a l'ordre 1; després comença EN_CURS a la ronda 1.
     */
    private void avancarPreparacio(Partida partida) {
        List<Jugador> jugadors = partida.getJugadors();
        int ordre = partida.getJugadorActiu().getOrdreTorn();
        if (partida.getRonda() == 1) {
            if (ordre < jugadors.size()) {
                partida.setJugadorActiu(jugadors.get(ordre));
            } else {
                partida.setRonda(2);
            }
        } else if (ordre > 1) {
            partida.setJugadorActiu(jugadors.get(ordre - 2));
        } else {
            partida.setEstat(EstatPartida.EN_CURS);
            partida.setRonda(1);
        }
    }

    // La ronda puja només quan el torn torna al jugador d'ordre 1
    private void avancarTorn(Partida partida) {
        List<Jugador> jugadors = partida.getJugadors();
        Jugador seguent = jugadors.get(partida.getJugadorActiu().getOrdreTorn() % jugadors.size());
        if (seguent.getOrdreTorn() == 1) {
            partida.setRonda(partida.getRonda() + 1);
        }
        partida.setJugadorActiu(seguent);
    }

    @Override
    public void mostrarPartidesPerEstat(EstatPartida estat) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            List<Partida> partides = session.createNamedQuery("Partida.perEstat", Partida.class)
                    .setParameter("estat", estat).getResultList();
            System.out.println("Partides " + estat + ": " + partides.size());
            for (Partida p : partides) {
                System.out.println("  Partida " + p.getId() + " - ronda " + p.getRonda());
            }
            session.getTransaction().commit();
        } catch (RuntimeException e) {
            handleException(session, e);
        }
    }

    @Override
    public Jugador ExercitMesGran(int idPartida) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Partida partida = session.get(Partida.class, idPartida);
            Jugador resultat = partida == null ? null : ReglesJoc.exercitMesGran(partida);
            session.getTransaction().commit();
            return resultat;
        } catch (RuntimeException e) {
            handleException(session, e);
            return null;
        }
    }

    @Override
    public void ComprovarVictoria(int idPartida) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            Partida partida = session.get(Partida.class, idPartida);
            if (partida == null) {
                ReglesJoc.rebutjar(session, String.format(Missatges.PARTIDA_NO_EXISTEIX, idPartida));
                return;
            }
            for (Jugador j : partida.getJugadors()) {
                if (ReglesJoc.comprovarVictoria(session, partida, j)) break;
            }
            session.getTransaction().commit();
        } catch (RuntimeException e) {
            handleException(session, e);
        }
    }
}
