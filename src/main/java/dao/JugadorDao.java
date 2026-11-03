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
import models.abstracts.Carretera;
import models.abstracts.Ciutat;
import models.abstracts.Poblat;
import models.enums.EstatPartida;
import models.enums.TipusCarta;
import models.enums.TipusRecurs;
import org.hibernate.Session;
import util.Missatges;

import java.util.List;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

public class JugadorDao extends GenericDao<Jugador, Integer> implements IJugadorDao {

    private static final int MAX_CARRETERES = 15;
    private static final int MAX_POBLATS = 5;
    private static final int MAX_CIUTATS = 4;
    private static final int COST_CIUTAT_BLAT = 2;
    private static final int COST_CIUTAT_MINERAL = 3;
    private static final int RECURSOS_BANCA = 4;

    public Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public void MostrarRecursos(int idJugador) {
        Session session = getSession();
        try {
            session.beginTransaction();
            Jugador jugador = session.get(Jugador.class, idJugador);
            if (jugador == null) {
                System.out.println(String.format(Missatges.JUGADOR_NO_EXISTEIX, idJugador));
            } else {
                System.out.println("Recursos de " + jugador.getNom() + ":");
                for (RecursJugador r : jugador.getRecursos()) {
                    System.out.println("  " + r.getTipus() + ": " + r.getQuantitat());
                }
            }
            session.getTransaction().commit();
        } catch (RuntimeException e) {
            handleException(session, e);
        }
    }

    @Override
    public void ConstruirCarretera(int idJugador, int idAresta) {
        Session session = getSession();
        try {
            session.beginTransaction();
            Jugador jugador = session.get(Jugador.class, idJugador);
            if (jugador == null) {
                ReglesJoc.rebutjar(session, String.format(Missatges.JUGADOR_NO_EXISTEIX, idJugador));
                return;
            }
            Partida partida = jugador.getPartida();
            String error = ReglesJoc.validarAccio(partida, jugador, true);
            Aresta aresta = trobarAresta(partida, idAresta);
            boolean preparacio = partida.getEstat() == EstatPartida.PREPARACIO;
            if (error == null) {
                error = comprovarCarretera(session, jugador, partida, aresta, preparacio);
            }
            if (error != null) {
                ReglesJoc.rebutjar(session, error);
                return;
            }

            if (!preparacio) {
                ReglesJoc.descomptar(jugador, TipusRecurs.FUSTA, 1);
                ReglesJoc.descomptar(jugador, TipusRecurs.MAO, 1);
            }

            Carretera carretera = new Carretera();
            carretera.setJugador(jugador);
            carretera.setAresta(aresta);
            session.persist(carretera);

            session.getTransaction().commit();
            System.out.println(String.format(Missatges.INFO_CARRETERA, idAresta));
        } catch (RuntimeException e) {
            handleException(session, e);
        }
    }

    // Motiu pel qual no es pot construir la carretera, o null si es pot
    private String comprovarCarretera(Session session, Jugador jugador, Partida partida, Aresta aresta, boolean preparacio) {
        if (aresta == null) return Missatges.ARESTA_NO_TROBADA;
        if (aresta.getCarretera() != null) return Missatges.ARESTA_OCUPADA;

        int carreteres = ReglesJoc.comptarCarreteres(jugador);
        if (carreteres >= MAX_CARRETERES) return String.format(Missatges.CARRETERA_LIMIT, MAX_CARRETERES);

        if (preparacio) {
            // Una carretera per poblat, i ha de tocar l'últim poblat col·locat
            int poblats = ReglesJoc.comptarPoblats(jugador);
            if (poblats < partida.getRonda()) return Missatges.POBLAT_PRIMER;
            if (carreteres >= partida.getRonda()) return Missatges.CARRETERA_JA_COL_LOCADA;
            Poblat ultim = ReglesJoc.ultimPoblat(jugador);
            if (!toca(aresta, ultim.getVertex())) return Missatges.CARRETERA_NO_TOCA_POBLAT;
            return null;
        }

        if (!connecta(session, jugador, aresta.getVertexA()) && !connecta(session, jugador, aresta.getVertexB())) {
            return Missatges.CARRETERA_NO_CONNECTADA;
        }
        if (ReglesJoc.quantitat(jugador, TipusRecurs.FUSTA) < 1 || ReglesJoc.quantitat(jugador, TipusRecurs.MAO) < 1) {
            return Missatges.CARRETERA_SENSE_RECURSOS;
        }
        return null;
    }

    private boolean toca(Aresta aresta, Vertex v) {
        return Vertex.mateix(aresta.getVertexA(), v) || Vertex.mateix(aresta.getVertexB(), v);
    }

    // El vèrtex connecta si hi ha una construcció pròpia o, si és lliure, una carretera pròpia que hi acaba
    private boolean connecta(Session session, Jugador jugador, Vertex v) {
        Assentament ass = v.getAssentament();
        if (ass != null) return ass.getJugador().getId().equals(jugador.getId());
        return teCarreteraPropia(jugador, v);
    }

    private boolean teCarreteraPropia(Jugador jugador, Vertex v) {
        for (Aresta a : jugador.getPartida().getArestas()) {
            if (toca(a, v) && a.getCarretera() != null
                    && a.getCarretera().getJugador().getId().equals(jugador.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void ConstruirPoblat(int idJugador, int idVertex) {
        Session session = getSession();
        try {
            session.beginTransaction();
            Jugador jugador = session.get(Jugador.class, idJugador);
            if (jugador == null) {
                ReglesJoc.rebutjar(session, String.format(Missatges.JUGADOR_NO_EXISTEIX, idJugador));
                return;
            }
            Partida partida = jugador.getPartida();
            String error = ReglesJoc.validarAccio(partida, jugador, true);
            Vertex vertex = trobarVertex(partida, idVertex);
            boolean preparacio = partida.getEstat() == EstatPartida.PREPARACIO;
            int poblatsAbans = ReglesJoc.comptarPoblats(jugador);
            if (error == null) {
                error = comprovarPoblat(jugador, partida, vertex, preparacio, poblatsAbans);
            }
            if (error != null) {
                ReglesJoc.rebutjar(session, error);
                return;
            }

            if (!preparacio) {
                ReglesJoc.descomptar(jugador, TipusRecurs.FUSTA, 1);
                ReglesJoc.descomptar(jugador, TipusRecurs.MAO, 1);
                ReglesJoc.descomptar(jugador, TipusRecurs.LLANA, 1);
                ReglesJoc.descomptar(jugador, TipusRecurs.BLAT, 1);
            }

            Poblat poblat = new Poblat();
            poblat.setJugador(jugador);
            poblat.setVertex(vertex);
            session.persist(poblat);

            // Segon poblat de la PREPARACIO: 1 recurs per cada hexàgon no desert que toca
            if (preparacio && poblatsAbans == 1) {
                repartirRecursosInicials(session, jugador, partida, vertex);
            }
            ReglesJoc.comprovarVictoria(session, partida, jugador);

            session.getTransaction().commit();
            System.out.println(String.format(Missatges.INFO_POBLAT, idVertex));
        } catch (RuntimeException e) {
            handleException(session, e);
        }
    }

    // Motiu pel qual no es pot construir el poblat, o null si es pot
    private String comprovarPoblat(Jugador jugador, Partida partida, Vertex vertex, boolean preparacio, int poblats) {
        if (vertex == null) return Missatges.VERTEX_NO_TROBAT;
        if (preparacio && poblats >= partida.getRonda()) return Missatges.POBLAT_JA_COL_LOCAT;
        if (vertex.getAssentament() != null) return Missatges.VERTEX_OCUPAT;
        if (teVeiOcupat(partida, vertex)) return Missatges.POBLAT_MASSA_PROP;
        if (poblats >= MAX_POBLATS) return String.format(Missatges.POBLAT_LIMIT, MAX_POBLATS);

        if (!preparacio) {
            if (!teCarreteraPropia(jugador, vertex)) return Missatges.POBLAT_NO_CONNECTAT;
            if (ReglesJoc.quantitat(jugador, TipusRecurs.FUSTA) < 1 || ReglesJoc.quantitat(jugador, TipusRecurs.MAO) < 1
                    || ReglesJoc.quantitat(jugador, TipusRecurs.LLANA) < 1
                    || ReglesJoc.quantitat(jugador, TipusRecurs.BLAT) < 1) {
                return Missatges.POBLAT_SENSE_RECURSOS;
            }
        }
        return null;
    }

    // Regla de la distància: cap vèrtex veí (l'altre extrem de les seves arestes) pot tenir construcció
    private boolean teVeiOcupat(Partida partida, Vertex vertex) {
        for (Aresta a : partida.getArestas()) {
            if (!toca(a, vertex)) continue;
            Vertex vei = Vertex.mateix(a.getVertexA(), vertex) ? a.getVertexB() : a.getVertexA();
            if (vei.getAssentament() != null) return true;
        }
        return false;
    }

    private void repartirRecursosInicials(Session session, Jugador jugador, Partida partida, Vertex vertex) {
        for (Hexagon h : partida.getHexagons()) {
            for (HexagonVertex hv : h.getHexagonVertexs()) {
                if (Vertex.mateix(hv.getVertex(), vertex)) {
                    TipusRecurs tipus = ReglesJoc.recursDe(h.getTerreny());
                    if (tipus != null) {
                        ReglesJoc.afegir(session, jugador, tipus, 1);
                    }
                }
            }
        }
    }

    @Override
    public void MillorarACiutat(int idJugador, int idVertex) {
        Session session = getSession();
        try {
            session.beginTransaction();
            Jugador jugador = session.get(Jugador.class, idJugador);
            if (jugador == null) {
                ReglesJoc.rebutjar(session, String.format(Missatges.JUGADOR_NO_EXISTEIX, idJugador));
                return;
            }
            Partida partida = jugador.getPartida();
            String error = ReglesJoc.validarAccio(partida, jugador, false);
            Vertex vertex = trobarVertex(partida, idVertex);
            if (error == null) {
                error = comprovarCiutat(session, jugador, vertex);
            }
            if (error != null) {
                ReglesJoc.rebutjar(session, error);
                return;
            }

            ReglesJoc.descomptar(jugador, TipusRecurs.BLAT, COST_CIUTAT_BLAT);
            ReglesJoc.descomptar(jugador, TipusRecurs.MINERAL, COST_CIUTAT_MINERAL);

            // Elimina el poblat i fa flush abans de crear la ciutat: el vertex_id és unique
            session.remove(vertex.getAssentament());
            vertex.setAssentament(null);
            session.flush();

            Ciutat ciutat = new Ciutat();
            ciutat.setJugador(jugador);
            ciutat.setVertex(vertex);
            session.persist(ciutat);
            ReglesJoc.comprovarVictoria(session, partida, jugador);

            session.getTransaction().commit();
            System.out.println(String.format(Missatges.INFO_CIUTAT, idVertex));
        } catch (RuntimeException e) {
            handleException(session, e);
        }
    }

    // Motiu pel qual no es pot millorar a ciutat, o null si es pot
    private String comprovarCiutat(Session session, Jugador jugador, Vertex vertex) {
        if (vertex == null) return Missatges.VERTEX_NO_TROBAT;
        Assentament ass = vertex.getAssentament();
        if (!(ass instanceof Poblat) || !ass.getJugador().getId().equals(jugador.getId())) {
            return Missatges.CIUTAT_SENSE_POBLAT;
        }
        if (ReglesJoc.quantitat(jugador, TipusRecurs.BLAT) < COST_CIUTAT_BLAT
                || ReglesJoc.quantitat(jugador, TipusRecurs.MINERAL) < COST_CIUTAT_MINERAL) {
            return Missatges.CIUTAT_SENSE_RECURSOS;
        }
        if (ReglesJoc.comptarCiutats(jugador) >= MAX_CIUTATS) {
            return String.format(Missatges.CIUTAT_LIMIT, MAX_CIUTATS);
        }
        return null;
    }

    private Aresta trobarAresta(Partida partida, int idAresta) {
        for (Aresta a : partida.getArestas()) {
            if (a.getId() == idAresta) return a;
        }
        return null;
    }

    private Vertex trobarVertex(Partida partida, int idVertex) {
        for (Vertex v : partida.getVertexs()) {
            if (v.getId() == idVertex) return v;
        }
        return null;
    }

    @Override
    public void ComerciarBanca(int idJugador, TipusRecurs dona, TipusRecurs rep) {
        Session session = getSession();
        try {
            session.beginTransaction();
            Jugador jugador = session.get(Jugador.class, idJugador);
            if (jugador == null) {
                ReglesJoc.rebutjar(session, String.format(Missatges.JUGADOR_NO_EXISTEIX, idJugador));
                return;
            }
            String error = ReglesJoc.validarAccio(jugador.getPartida(), jugador, false);
            if (error == null) {
                error = comprovarComerc(jugador, dona, rep);
            }
            if (error != null) {
                ReglesJoc.rebutjar(session, error);
                return;
            }

            ReglesJoc.descomptar(jugador, dona, RECURSOS_BANCA);
            ReglesJoc.afegir(session, jugador, rep, 1);

            session.getTransaction().commit();
            System.out.println(String.format(Missatges.INFO_COMERCI, jugador.getNom(), dona, rep));
        } catch (RuntimeException e) {
            handleException(session, e);
        }
    }

    // Motiu pel qual no es pot comerciar, o null si es pot
    private String comprovarComerc(Jugador jugador, TipusRecurs dona, TipusRecurs rep) {
        if (dona == null || rep == null) return Missatges.BANCA_RECURS_NUL;
        if (dona == rep) return Missatges.BANCA_RECURSOS_IGUALS;
        if (ReglesJoc.quantitat(jugador, dona) < RECURSOS_BANCA) return Missatges.BANCA_SENSE_RECURSOS;
        return null;
    }

    @Override
    public void MoureLladre(int idJugador, int idHexagon, Integer idJugadorVictima) {
        Session session = getSession();
        try {
            session.beginTransaction();
            Jugador jugador = session.get(Jugador.class, idJugador);
            if (jugador == null) {
                ReglesJoc.rebutjar(session, String.format(Missatges.JUGADOR_NO_EXISTEIX, idJugador));
                return;
            }
            Partida partida = jugador.getPartida();
            Hexagon desti = session.get(Hexagon.class, idHexagon);
            if (partida.getEstat() != EstatPartida.EN_CURS || !ReglesJoc.esElSeuTorn(jugador, partida)
                    || !partida.isLladrePendent() || desti == null
                    || !desti.getPartida().getId().equals(partida.getId())
                    || (partida.getLladre() != null && partida.getLladre().getId().equals(desti.getId()))) {
                ReglesJoc.rebutjar(session, "Moviment del lladre no vàlid");
                return;
            }
            List<Jugador> victimes = session.createQuery(
                    "select distinct a.jugador from HexagonVertex hv join hv.vertex.assentament a "
                            + "where hv.hexagon = :hexagon and a.jugador <> :jugador", Jugador.class)
                    .setParameter("hexagon", desti).setParameter("jugador", jugador).getResultList();
            victimes.removeIf(v -> ReglesJoc.totalRecursos(v) == 0);
            Jugador victima = victimes.stream().filter(v -> v.getId().equals(idJugadorVictima)).findFirst().orElse(null);
            if ((idJugadorVictima == null && !victimes.isEmpty()) || (idJugadorVictima != null && victima == null)) {
                ReglesJoc.rebutjar(session, "Víctima del lladre no vàlida");
                return;
            }
            if (victima != null) {
                int index = ThreadLocalRandom.current().nextInt(ReglesJoc.totalRecursos(victima));
                for (RecursJugador r : victima.getRecursos()) {
                    if (index < r.getQuantitat()) {
                        ReglesJoc.descomptar(victima, r.getTipus(), 1);
                        ReglesJoc.afegir(session, jugador, r.getTipus(), 1);
                        break;
                    }
                    index -= r.getQuantitat();
                }
            }
            partida.setLladre(desti);
            partida.setLladrePendent(false);
            session.getTransaction().commit();
        } catch (RuntimeException e) {
            handleException(session, e);
        }
    }

    @Override
    public void ComprarCarta(int idJugador) {
        Session session = getSession();
        try {
            session.beginTransaction();
            Jugador jugador = session.get(Jugador.class, idJugador);
            if (jugador == null) {
                ReglesJoc.rebutjar(session, String.format(Missatges.JUGADOR_NO_EXISTEIX, idJugador));
                return;
            }
            Partida partida = jugador.getPartida();
            String error = ReglesJoc.validarAccio(partida, jugador, false);
            if (error != null || ReglesJoc.quantitat(jugador, TipusRecurs.BLAT) < 1
                    || ReglesJoc.quantitat(jugador, TipusRecurs.LLANA) < 1
                    || ReglesJoc.quantitat(jugador, TipusRecurs.MINERAL) < 1) {
                ReglesJoc.rebutjar(session, error != null ? error : "Recursos insuficients");
                return;
            }
            List<CartaDesenvolupament> pila = session.createQuery(
                    "from CartaDesenvolupament c where c.partida = :partida and c.jugador is null",
                    CartaDesenvolupament.class).setParameter("partida", partida).getResultList();
            if (pila.isEmpty()) {
                ReglesJoc.rebutjar(session, "La pila de cartes és buida");
                return;
            }
            CartaDesenvolupament carta = pila.get(ThreadLocalRandom.current().nextInt(pila.size()));
            for (TipusRecurs tipus : List.of(TipusRecurs.BLAT, TipusRecurs.LLANA, TipusRecurs.MINERAL)) {
                ReglesJoc.descomptar(jugador, tipus, 1);
            }
            carta.setJugador(jugador);
            carta.setRondaCompra(partida.getRonda());
            jugador.getCartes().add(carta);
            ReglesJoc.comprovarVictoria(session, partida, jugador);
            session.getTransaction().commit();
        } catch (RuntimeException e) {
            handleException(session, e);
        }
    }

    @Override
    public void JugarCarta(int idJugador, int idCarta, TipusRecurs recurs) {
        Session session = getSession();
        try {
            session.beginTransaction();
            Jugador jugador = session.get(Jugador.class, idJugador);
            if (jugador == null) {
                ReglesJoc.rebutjar(session, String.format(Missatges.JUGADOR_NO_EXISTEIX, idJugador));
                return;
            }
            Partida partida = jugador.getPartida();
            String error = ReglesJoc.validarAccio(partida, jugador, false);
            CartaDesenvolupament carta = session.get(CartaDesenvolupament.class, idCarta);
            if (error != null || carta == null || carta.getJugador() == null
                    || !carta.getJugador().getId().equals(jugador.getId()) || carta.isUsada()
                    || carta.getRondaCompra() >= partida.getRonda() || carta.getTipus() == TipusCarta.PUNT_VICTORIA
                    || (carta.getTipus() == TipusCarta.MONOPOLI && recurs == null)) {
                ReglesJoc.rebutjar(session, error != null ? error : "Carta no jugable");
                return;
            }
            carta.setUsada(true);
            if (carta.getTipus() == TipusCarta.CAVALLER) {
                jugador.setCavallersJugats(jugador.getCavallersJugats() + 1);
                partida.setLladrePendent(true);
                ReglesJoc.comprovarVictoria(session, partida, jugador);
            } else if (carta.getTipus() == TipusCarta.MONOPOLI) {
                for (Jugador altre : partida.getJugadors()) {
                    if (altre.getId().equals(jugador.getId())) continue;
                    int quantitat = ReglesJoc.quantitat(altre, recurs);
                    if (quantitat > 0) {
                        ReglesJoc.descomptar(altre, recurs, quantitat);
                        ReglesJoc.afegir(session, jugador, recurs, quantitat);
                    }
                }
            }
            session.getTransaction().commit();
        } catch (RuntimeException e) {
            handleException(session, e);
        }
    }

    @Override
    public List<Vertex> VertexsDisponibles(int idJugador) {
        Session session = getSession();
        try {
            session.beginTransaction();
            Jugador jugador = session.get(Jugador.class, idJugador);
            if (jugador == null) {
                session.getTransaction().commit();
                return Collections.emptyList();
            }
            boolean preparacio = jugador.getPartida().getEstat() == EstatPartida.PREPARACIO;
            var query = session.createQuery("""
                    select v from Vertex v
                    where v.partida = :partida and v.assentament is null
                      and not exists (
                        select a.id from Aresta a
                        where (a.vertexA = v and a.vertexB.assentament is not null)
                           or (a.vertexB = v and a.vertexA.assentament is not null)
                      )
                      and (:preparacio = true or exists (
                        select a.id from Aresta a
                        where (a.vertexA = v or a.vertexB = v) and a.carretera.jugador = :jugador
                      ))
                    order by v.id
                    """, Vertex.class)
                    .setParameter("partida", jugador.getPartida())
                    .setParameter("preparacio", preparacio)
                    .setParameter("jugador", jugador);
            List<Vertex> resultat = query.getResultList();
            session.getTransaction().commit();
            return resultat;
        } catch (RuntimeException e) {
            handleException(session, e);
            return Collections.emptyList();
        }
    }

    @Override
    public int CalcularPunts(int idJugador) {
        Session session = getSession();
        try {
            session.beginTransaction();
            Jugador jugador = session.get(Jugador.class, idJugador);
            if (jugador == null) {
                System.out.println(String.format(Missatges.JUGADOR_NO_EXISTEIX, idJugador));
                session.getTransaction().commit();
                return 0;
            }
            int punts = ReglesJoc.calcularPunts(jugador);
            session.getTransaction().commit();
            return punts;
        } catch (RuntimeException e) {
            handleException(session, e);
            return 0;
        }
    }
}
